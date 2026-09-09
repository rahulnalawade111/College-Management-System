package com.college.sms.service;

import com.college.sms.dto.AttendanceEntryRequest;
import com.college.sms.dto.AttendanceResponse;
import com.college.sms.dto.BulkAttendanceRequest;
import com.college.sms.entity.Attendance;
import com.college.sms.entity.Student;
import com.college.sms.entity.Subject;
import com.college.sms.exception.BadRequestException;
import com.college.sms.exception.DuplicateResourceException;
import com.college.sms.exception.ResourceNotFoundException;
import com.college.sms.repository.AttendanceRepository;
import com.college.sms.repository.StudentRepository;
import com.college.sms.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private static final Set<String> VALID_STATUSES = Set.of("PRESENT", "ABSENT", "LATE", "EXCUSED");

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;

    /** Bulk mark attendance for a subject+date. Any existing row for a student/subject/date → 409. */
    @Transactional
    public List<AttendanceResponse> bulkMark(BulkAttendanceRequest request) {
        if (request.attendanceDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Attendance date cannot be in the future");
        }
        Subject subject = getSubject(request.subjectId());

        // validate entries first
        Set<Long> seen = new HashSet<>();
        for (AttendanceEntryRequest entry : request.entries()) {
            if (!VALID_STATUSES.contains(entry.status())) {
                throw new BadRequestException("Invalid attendance status: " + entry.status());
            }
            if (!seen.add(entry.studentId())) {
                throw new BadRequestException("Duplicate student in request: " + entry.studentId());
            }
            if (!studentRepository.existsById(entry.studentId())) {
                throw new ResourceNotFoundException("Student not found with id " + entry.studentId());
            }
        }

        List<Attendance> toSave = new ArrayList<>();
        for (AttendanceEntryRequest entry : request.entries()) {
            if (attendanceRepository.existsByStudentIdAndSubjectIdAndAttendanceDate(
                    entry.studentId(), request.subjectId(), request.attendanceDate())) {
                throw new DuplicateResourceException(
                        "Attendance already recorded for student " + entry.studentId()
                                + " in " + subject.getSubjectCode() + " on " + request.attendanceDate()
                                + ". Edit the existing record instead.");
            }
            Student student = studentRepository.findById(entry.studentId()).orElseThrow();
            toSave.add(Attendance.builder()
                    .student(student)
                    .subject(subject)
                    .facultyId(subject.getFacultyId())
                    .attendanceDate(request.attendanceDate())
                    .status(entry.status())
                    .remarks(request.remarks())
                    .build());
        }
        List<Attendance> saved = attendanceRepository.saveAll(toSave);
        return saved.stream().map(this::toResponse).toList();
    }

    /** Attendance already saved for a subject+date (for pre-filling the marking form). */
    @Transactional(readOnly = true)
    public List<AttendanceResponse> findBySubjectAndDate(Long subjectId, LocalDate date) {
        return attendanceRepository.findBySubjectIdAndAttendanceDate(subjectId, date)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponse> findByStudent(Long studentId) {
        getStudent(studentId);
        return attendanceRepository.findByStudentIdOrderByAttendanceDateDesc(studentId)
                .stream().map(this::toResponse).toList();
    }

    /** Per-subject attendance summary for one student. EXCUSED does not count against the %. */
    @Transactional(readOnly = true)
    public List<AttendanceResponse.SubjectSummary> subjectSummary(Long studentId) {
        getStudent(studentId);
        List<Object[]> rows = attendanceRepository.subjectSummaryByStudentId(studentId);
        List<AttendanceResponse.SubjectSummary> result = new ArrayList<>();
        for (Object[] r : rows) {
            Long subjectId = (Long) r[0];
            String subjectName = (String) r[1];
            String subjectCode = (String) r[2];
            long total = (Long) r[3];
            long present = (Long) r[4];
            long late = (Long) r[5];
            long absent = (Long) r[6];
            long excused = (Long) r[7];
            long counted = total - excused;
            double pct = counted == 0 ? 0 : round2((present + late) * 100.0 / counted);
            result.add(new AttendanceResponse.SubjectSummary(
                    subjectId, subjectCode, subjectName, total, present, late, absent, excused, pct));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public AttendanceResponse.OverallSummary overallSummary(Long studentId) {
        getStudent(studentId);
        List<Object[]> rows = attendanceRepository.overallByStudentId(studentId);
        Object[] r = rows.isEmpty() ? new Object[]{0L, 0L} : rows.get(0);
        if (r == null || r[0] == null) {
            return new AttendanceResponse.OverallSummary(0, 0, 0);
        }
        long total = ((Number) r[0]).longValue();
        long present = r[1] == null ? 0 : ((Number) r[1]).longValue();
        double pct = total == 0 ? 0 : round2(present * 100.0 / total);
        return new AttendanceResponse.OverallSummary(total, present, pct);
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private Subject getSubject(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id " + id));
    }

    private Student getStudent(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + id));
    }

    private AttendanceResponse toResponse(Attendance a) {
        return new AttendanceResponse(a.getId(), a.getStudent().getId(),
                a.getStudent().getFirstName() + " " + a.getStudent().getLastName(),
                a.getStudent().getStudentId(), a.getSubject().getId(), a.getSubject().getSubjectName(),
                a.getFacultyId() == null ? null : a.getFacultyId().toString(),
                a.getAttendanceDate().toString(), a.getStatus(), a.getRemarks());
    }
}
