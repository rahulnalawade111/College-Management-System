package com.college.sms.service;

import com.college.sms.dto.ExamRequest;
import com.college.sms.dto.ExamResponse;
import com.college.sms.dto.ExamScheduleRequest;
import com.college.sms.entity.AcademicYear;
import com.college.sms.entity.Exam;
import com.college.sms.entity.ExamSchedule;
import com.college.sms.entity.Subject;
import com.college.sms.exception.BadRequestException;
import com.college.sms.exception.DuplicateResourceException;
import com.college.sms.exception.ResourceNotFoundException;
import com.college.sms.repository.AcademicYearRepository;
import com.college.sms.repository.ExamRepository;
import com.college.sms.repository.ExamScheduleRepository;
import com.college.sms.repository.SemesterRepository;
import com.college.sms.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ExamService {

    private static final List<String> TYPES = List.of("INTERNAL", "MIDTERM", "FINAL", "PRACTICAL");
    private static final List<String> STATUSES = List.of("SCHEDULED", "ONGOING", "COMPLETED", "CANCELLED");

    private final ExamRepository examRepository;
    private final ExamScheduleRepository scheduleRepository;
    private final AcademicYearRepository academicYearRepository;
    private final SemesterRepository semesterRepository;
    private final SubjectRepository subjectRepository;
    private final com.college.sms.repository.StudentRepository studentRepository;

    public ExamService(ExamRepository examRepository,
                       ExamScheduleRepository scheduleRepository,
                       AcademicYearRepository academicYearRepository,
                       SemesterRepository semesterRepository,
                       SubjectRepository subjectRepository,
                       com.college.sms.repository.StudentRepository studentRepository) {
        this.examRepository = examRepository;
        this.scheduleRepository = scheduleRepository;
        this.academicYearRepository = academicYearRepository;
        this.semesterRepository = semesterRepository;
        this.subjectRepository = subjectRepository;
        this.studentRepository = studentRepository;
    }

    // ------------------------------------------------------------------
    // exams
    // ------------------------------------------------------------------

    @Transactional
    public ExamResponse create(ExamRequest request) {
        if (!TYPES.contains(request.getExamType())) {
            throw new BadRequestException("Invalid exam type: " + request.getExamType()
                    + ". Allowed: " + TYPES);
        }
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date cannot be before start date");
        }

        AcademicYear ay = academicYearRepository.findById(request.getAcademicYearId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Academic year not found with id " + request.getAcademicYearId()));

        Exam exam = Exam.builder()
                .examName(request.getExamName())
                .examType(request.getExamType())
                .academicYear(ay)
                .semester(request.getSemesterId() != null
                        ? semesterRepository.findById(request.getSemesterId()).orElse(null) : null)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status("SCHEDULED")
                .build();
        return toResponse(examRepository.save(exam));
    }

    @Transactional
    public ExamResponse update(Long id, ExamRequest request) {
        Exam exam = getExam(id);
        if (!TYPES.contains(request.getExamType())) {
            throw new BadRequestException("Invalid exam type: " + request.getExamType());
        }
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date cannot be before start date");
        }
        if (exam.isResultPublished() && !exam.getStartDate().equals(request.getStartDate())) {
            throw new BadRequestException("Results are published for this exam — dates are locked");
        }
        exam.setExamName(request.getExamName());
        exam.setExamType(request.getExamType());
        exam.setAcademicYear(academicYearRepository.findById(request.getAcademicYearId())
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found")));
        exam.setSemester(request.getSemesterId() != null
                ? semesterRepository.findById(request.getSemesterId()).orElse(null) : null);
        exam.setStartDate(request.getStartDate());
        exam.setEndDate(request.getEndDate());
        return toResponse(examRepository.save(exam));
    }

    @Transactional
    public ExamResponse setStatus(Long id, String status) {
        if (!STATUSES.contains(status)) {
            throw new BadRequestException("Invalid status: " + status + ". Allowed: " + STATUSES);
        }
        Exam exam = getExam(id);
        exam.setStatus(status);
        return toResponse(examRepository.save(exam));
    }

    @Transactional(readOnly = true)
    public List<ExamResponse> all() {
        return examRepository.findAllByOrderByStartDateAsc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ExamResponse one(Long id) {
        return toResponse(getExam(id));
    }

    // ------------------------------------------------------------------
    // schedules
    // ------------------------------------------------------------------

    @Transactional
    public ExamResponse addSchedule(Long examId, ExamScheduleRequest request) {
        Exam exam = getExam(examId);
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subject not found with id " + request.getSubjectId()));

        if (request.getEndTime().isBefore(request.getStartTime())
                || request.getEndTime().equals(request.getStartTime())) {
            throw new BadRequestException("End time must be after start time");
        }
        if (request.getExamDate().isBefore(exam.getStartDate())
                || request.getExamDate().isAfter(exam.getEndDate())) {
            throw new BadRequestException("Exam date must fall within the exam window ("
                    + exam.getStartDate() + " to " + exam.getEndDate() + ")");
        }
        if (scheduleRepository.existsByExamIdAndSubjectId(examId, request.getSubjectId())) {
            throw new DuplicateResourceException("This subject already has a slot in this exam");
        }
        if (scheduleRepository.countOverlaps(request.getSubjectId(), request.getExamDate(),
                request.getStartTime(), request.getEndTime(), null) > 0) {
            throw new BadRequestException("Subject is already booked in an overlapping slot — no double-booking");
        }

        ExamSchedule schedule = ExamSchedule.builder()
                .exam(exam)
                .subject(subject)
                .examDate(request.getExamDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .room(request.getRoom())
                .maxMarks(request.getMaxMarks() != null ? request.getMaxMarks() : 100)
                .build();
        scheduleRepository.save(schedule);
        return toResponse(exam);
    }

    @Transactional
    public ExamResponse removeSchedule(Long examId, Long scheduleId) {
        ExamSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule entry not found"));
        if (!schedule.getExam().getId().equals(examId)) {
            throw new BadRequestException("Schedule entry does not belong to this exam");
        }
        scheduleRepository.delete(schedule);
        return toResponse(getExam(examId));
    }

    /** Student-facing: upcoming schedule for the logged-in student's course+semester. */
    @Transactional(readOnly = true)
    public List<ExamResponse> upcomingForStudentOfCurrentStudent(Long studentId) {
        com.college.sms.entity.Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + studentId));
        return upcomingForStudent(
                student.getCourse() != null ? student.getCourse().getId() : null,
                student.getSemester() != null ? student.getSemester().getSemesterNumber() : null);
    }

    /** Student-facing: upcoming schedule for their own course+semester subjects. */
    @Transactional(readOnly = true)
    public List<ExamResponse> upcomingForStudent(Long courseId, Integer semesterNumber) {
        LocalDate today = LocalDate.now();
        return examRepository.findByStatusOrderByStartDateAsc("SCHEDULED").stream()
                .filter(e -> !e.getEndDate().isBefore(today))
                .filter(e -> e.getSemester() == null
                        || e.getSemester().getSemesterNumber().equals(semesterNumber))
                .map(e -> filterScheduleByCourse(toResponse(e), courseId))
                .filter(e -> !e.schedule().isEmpty())
                .toList();
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    private Exam getExam(Long id) {
        return examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id " + id));
    }

    private ExamResponse filterScheduleByCourse(ExamResponse resp, Long courseId) {
        List<ExamResponse.ScheduleEntry> filtered = resp.schedule().stream()
                .filter(s -> subjectBelongsToCourse(s.subjectId(), courseId))
                .toList();
        return new ExamResponse(resp.id(), resp.examName(), resp.examType(), resp.academicYearId(),
                resp.academicYearName(), resp.semesterId(), resp.semesterNumber(),
                resp.startDate(), resp.endDate(), resp.status(), resp.resultPublished(), filtered);
    }

    private boolean subjectBelongsToCourse(Long subjectId, Long courseId) {
        return subjectRepository.findById(subjectId)
                .map(s -> s.getCourse() != null && s.getCourse().getId().equals(courseId))
                .orElse(false);
    }

    private ExamResponse toResponse(Exam e) {
        List<ExamResponse.ScheduleEntry> schedule =
                scheduleRepository.findByExamIdOrderByExamDateAscStartTimeAsc(e.getId()).stream()
                        .map(s -> new ExamResponse.ScheduleEntry(
                                s.getId(),
                                s.getSubject().getId(),
                                s.getSubject().getSubjectCode(),
                                s.getSubject().getSubjectName(),
                                s.getExamDate(),
                                s.getStartTime().toString(),
                                s.getEndTime().toString(),
                                s.getRoom(),
                                s.getMaxMarks()))
                        .toList();
        return new ExamResponse(
                e.getId(),
                e.getExamName(),
                e.getExamType(),
                e.getAcademicYear() != null ? e.getAcademicYear().getId() : null,
                e.getAcademicYear() != null ? e.getAcademicYear().getYearName() : null,
                e.getSemester() != null ? e.getSemester().getId() : null,
                e.getSemester() != null ? e.getSemester().getSemesterNumber() : null,
                e.getStartDate(),
                e.getEndDate(),
                e.getStatus(),
                e.isResultPublished(),
                schedule);
    }
}
