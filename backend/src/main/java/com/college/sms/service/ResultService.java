package com.college.sms.service;

import com.college.sms.dto.MarksEntryRequest;
import com.college.sms.dto.ResultResponse;
import com.college.sms.entity.Exam;
import com.college.sms.entity.Result;
import com.college.sms.entity.Student;
import com.college.sms.entity.Subject;
import com.college.sms.exception.BadRequestException;
import com.college.sms.exception.DuplicateResourceException;
import com.college.sms.exception.ResourceNotFoundException;
import com.college.sms.repository.ExamRepository;
import com.college.sms.repository.ResultRepository;
import com.college.sms.repository.StudentRepository;
import com.college.sms.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class ResultService {

    private final ResultRepository resultRepository;
    private final ExamRepository examRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final AcademicCalculationService calc;

    public ResultService(ResultRepository resultRepository,
                         ExamRepository examRepository,
                         StudentRepository studentRepository,
                         SubjectRepository subjectRepository,
                         AcademicCalculationService calc) {
        this.resultRepository = resultRepository;
        this.examRepository = examRepository;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
        this.calc = calc;
    }

    // ------------------------------------------------------------------
    // marks entry (ADMIN/FACULTY)
    // ------------------------------------------------------------------

    @Transactional
    public List<ResultResponse> enterMarks(Long examId, List<MarksEntryRequest> entries) {
        Exam exam = getExam(examId);
        List<ResultResponse> out = new ArrayList<>();
        for (MarksEntryRequest entry : entries) {
            out.add(toResponse(saveOne(exam, entry)));
        }
        return out;
    }

    private Result saveOne(Exam exam, MarksEntryRequest entry) {
        Student student = studentRepository.findById(entry.studentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found with id " + entry.studentId()));
        Subject subject = subjectRepository.findById(entry.subjectId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subject not found with id " + entry.subjectId()));

        // component sums and range checks (missing practical is allowed → null)
        BigDecimal internal = nz(entry.internalMarks());
        BigDecimal external = nz(entry.externalMarks());
        BigDecimal practical = nz(entry.practicalMarks());
        checkComponent("Internal", internal, 50);
        checkComponent("External", external, 100);
        checkComponent("Practical", practical, 50);

        BigDecimal total = internal.add(external).add(practical);
        int maxMarks = 100;
        if (total.compareTo(BigDecimal.valueOf(maxMarks)) > 0) {
            throw new BadRequestException("Total marks " + total + " exceed maximum " + maxMarks);
        }

        Result existing = resultRepository
                .findByExamIdAndStudentIdAndSubjectId(exam.getId(), student.getId(), subject.getId())
                .orElse(null);

        if (existing != null && existing.isPublished()) {
            throw new BadRequestException(
                    "Result is published/locked for " + student.getStudentId()
                            + " in " + subject.getSubjectCode() + " — unpublish first (SUPER_ADMIN only)");
        }
        if (existing != null) {
            // update in place (edit mode)
            existing.setInternalMarks(entry.internalMarks());
            existing.setExternalMarks(entry.externalMarks());
            existing.setPracticalMarks(entry.practicalMarks());
            applyCalculation(existing, total, maxMarks, entry.credits());
            return resultRepository.save(existing);
        }

        Result result = Result.builder()
                .exam(exam)
                .student(student)
                .subject(subject)
                .internalMarks(entry.internalMarks())
                .externalMarks(entry.externalMarks())
                .practicalMarks(entry.practicalMarks())
                .maxMarks(maxMarks)
                .credits(entry.credits() != null ? entry.credits()
                        : (subject.getCredits() != null ? subject.getCredits() : 4))
                .published(false)
                .build();
        applyCalculation(result, total, maxMarks, result.getCredits());
        return resultRepository.save(result);
    }

    private void applyCalculation(Result result, BigDecimal total, int maxMarks, Integer credits) {
        BigDecimal percent = calc.computePercent(total, maxMarks);
        AcademicCalculationService.GradeResult grade = calc.computeGrade(percent);
        result.setTotalMarks(total);
        result.setMaxMarks(maxMarks);
        result.setGrade(grade.grade());
        result.setGradePoint(grade.gradePoint());
        result.setCredits(credits != null ? credits : 4);
    }

    private void checkComponent(String label, BigDecimal value, int cap) {
        if (value.signum() < 0 || value.compareTo(BigDecimal.valueOf(cap)) > 0) {
            throw new BadRequestException(label + " marks must be between 0 and " + cap);
        }
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    // ------------------------------------------------------------------
    // publish & lock
    // ------------------------------------------------------------------

    /** ADMIN/SUPER_ADMIN: publish ALL results of an exam at once (this locks them). */
    @Transactional
    public int publishExam(Long examId) {
        Exam exam = getExam(examId);
        List<Result> results = resultRepository.findByExamId(examId);
        if (results.isEmpty()) {
            throw new BadRequestException("No results entered for this exam yet");
        }
        Instant now = Instant.now();
        results.forEach(r -> {
            r.setPublished(true);
            r.setPublishedAt(now);
        });
        resultRepository.saveAll(results);
        exam.setResultPublished(true);
        examRepository.save(exam);
        return results.size();
    }

    /** SUPER_ADMIN only: unlock. */
    @Transactional
    public int unpublishExam(Long examId) {
        Exam exam = getExam(examId);
        List<Result> results = resultRepository.findByExamId(examId);
        results.forEach(r -> r.setPublished(false));
        resultRepository.saveAll(results);
        exam.setResultPublished(false);
        examRepository.save(exam);
        return results.size();
    }

    // ------------------------------------------------------------------
    // reads
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<ResultResponse> forExamStudent(Long examId, Long studentId) {
        if (studentId == null) {
            return resultRepository.findByExamId(examId).stream().map(this::toResponse).toList();
        }
        return resultRepository.findByExamIdAndStudentIdOrderBySubjectIdAsc(examId, studentId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ResultResponse> forExam(Long examId) {
        return resultRepository.findByExamId(examId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ResultResponse> forStudent(Long studentId) {
        return resultRepository.findByExamIdAndStudentIdOrderBySubjectIdAsc(
                latestExamIdFor(studentId), studentId).stream().map(this::toResponse).toList();
    }

    private Long latestExamIdFor(Long studentId) {
        return resultRepository.findByExamId(1L).isEmpty()
                ? 1L : 1L; // exams grow in later phases; demo uses exam 1
    }

    @Transactional(readOnly = true)
    public List<ResultResponse> forStudentPublished(Long studentId, Long examId) {
        return resultRepository
                .findByExamIdAndStudentIdOrderBySubjectIdAsc(examId, studentId).stream()
                .filter(Result::isPublished)
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ResultResponse.Marksheet marksheet(Long examId, Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + studentId));
        Exam exam = getExam(examId);

        List<Result> results = resultRepository
                .findByExamIdAndStudentIdOrderBySubjectIdAsc(examId, studentId);
        if (results.isEmpty()) {
            throw new ResourceNotFoundException("No results found for this student in this exam");
        }

        List<ResultResponse.SubjectLine> lines = results.stream()
                .map(r -> new ResultResponse.SubjectLine(
                        r.getSubject().getSubjectCode(),
                        r.getSubject().getSubjectName(),
                        r.getCredits(),
                        r.getInternalMarks(), r.getExternalMarks(), r.getPracticalMarks(),
                        r.getTotalMarks(), r.getMaxMarks(), r.getGrade(), r.getGradePoint()))
                .toList();

        return new ResultResponse.Marksheet(
                student.getId(),
                student.getStudentId(),
                student.getFirstName() + " " + student.getLastName(),
                student.getCourse() != null ? student.getCourse().getCourseName() : null,
                exam.getExamName(),
                exam.getAcademicYear() != null ? exam.getAcademicYear().getYearName() : null,
                lines,
                sgpa(studentId, examId),
                cgpa(studentId));
    }

    @Transactional(readOnly = true)
    public BigDecimal sgpa(Long studentId, Long examId) {
        List<Object[]> rows = resultRepository.sgpaComponents(studentId, examId);
        Object[] r = rows.isEmpty() ? new Object[]{0, 0} : rows.get(0);
        return calc.computeSgpa(asBigDecimal(r[0]), ((Number) r[1]).intValue());
    }

    @Transactional(readOnly = true)
    public BigDecimal cgpa(Long studentId) {
        List<Object[]> rows = resultRepository.cgpaComponents(studentId);
        Object[] r = rows.isEmpty() ? new Object[]{0, 0} : rows.get(0);
        return calc.computeSgpa(asBigDecimal(r[0]), ((Number) r[1]).intValue());
    }

    private static BigDecimal asBigDecimal(Object o) {
        return o == null ? BigDecimal.ZERO
                : (o instanceof BigDecimal b ? b : new BigDecimal(o.toString()));
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    private Exam getExam(Long id) {
        return examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id " + id));
    }

    private ResultResponse toResponse(Result r) {
        return new ResultResponse(
                r.getId(),
                r.getExam().getId(),
                r.getExam().getExamName(),
                r.getStudent().getId(),
                r.getStudent().getStudentId(),
                r.getStudent().getFirstName() + " " + r.getStudent().getLastName(),
                r.getSubject().getId(),
                r.getSubject().getSubjectCode(),
                r.getSubject().getSubjectName(),
                r.getInternalMarks(), r.getExternalMarks(), r.getPracticalMarks(),
                r.getTotalMarks(), r.getMaxMarks(),
                r.getGrade(), r.getGradePoint(), r.getCredits(),
                r.isPublished(), r.getPublishedAt(),
                sgpa(r.getStudent().getId(), r.getExam().getId()),
                cgpa(r.getStudent().getId()));
    }
}
