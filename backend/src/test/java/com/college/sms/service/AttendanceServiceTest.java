package com.college.sms.service;

import com.college.sms.dto.AttendanceEntryRequest;
import com.college.sms.dto.AttendanceResponse;
import com.college.sms.dto.BulkAttendanceRequest;
import com.college.sms.entity.Student;
import com.college.sms.entity.Subject;
import com.college.sms.exception.BadRequestException;
import com.college.sms.exception.DuplicateResourceException;
import com.college.sms.exception.ResourceNotFoundException;
import com.college.sms.repository.AttendanceRepository;
import com.college.sms.repository.StudentRepository;
import com.college.sms.repository.SubjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock private AttendanceRepository attendanceRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private SubjectRepository subjectRepository;

    private AttendanceService service;

    private final Subject subject = Subject.builder().id(3L).subjectCode("BCA101")
            .subjectName("Programming in C").facultyId(1L).build();
    private final Student student = Student.builder().id(7L).studentId("ABC2026CS001")
            .firstName("Aarav").lastName("Shah").email("a@college.edu").build();

    @BeforeEach
    void setUp() {
        service = new AttendanceService(attendanceRepository, studentRepository, subjectRepository);
    }

    private BulkAttendanceRequest request(LocalDate date, AttendanceEntryRequest... entries) {
        return new BulkAttendanceRequest(3L, date, null, List.of(entries));
    }

    @Test
    void bulkMarkSavesAllEntries() {
        when(subjectRepository.findById(3L)).thenReturn(Optional.of(subject));
        when(studentRepository.existsById(7L)).thenReturn(true);
        when(studentRepository.findById(7L)).thenReturn(Optional.of(student));
        when(attendanceRepository.existsByStudentIdAndSubjectIdAndAttendanceDate(7L, 3L, LocalDate.of(2026, 9, 1)))
                .thenReturn(false);
        when(attendanceRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<AttendanceResponse> saved = service.bulkMark(request(
                LocalDate.of(2026, 9, 1), new AttendanceEntryRequest(7L, "PRESENT")));

        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).status()).isEqualTo("PRESENT");
        assertThat(saved.get(0).subjectName()).isEqualTo("Programming in C");
    }

    @Test
    void bulkMarkRejectsDuplicateRow() {
        when(subjectRepository.findById(3L)).thenReturn(Optional.of(subject));
        when(studentRepository.existsById(7L)).thenReturn(true);
        when(attendanceRepository.existsByStudentIdAndSubjectIdAndAttendanceDate(7L, 3L, LocalDate.of(2026, 9, 1)))
                .thenReturn(true);

        assertThatThrownBy(() -> service.bulkMark(request(
                LocalDate.of(2026, 9, 1), new AttendanceEntryRequest(7L, "PRESENT"))))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already recorded");
    }

    @Test
    void bulkMarkRejectsFutureDate() {
        assertThatThrownBy(() -> service.bulkMark(request(LocalDate.now().plusDays(1),
                new AttendanceEntryRequest(7L, "PRESENT"))))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("future");
    }

    @Test
    void bulkMarkRejectsInvalidStatus() {
        when(subjectRepository.findById(3L)).thenReturn(Optional.of(subject));
        assertThatThrownBy(() -> service.bulkMark(request(LocalDate.now(),
                new AttendanceEntryRequest(7L, "MAYBE"))))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("status");
    }

    @Test
    void bulkMarkRejectsDuplicateStudentInRequest() {
        when(subjectRepository.findById(3L)).thenReturn(Optional.of(subject));
        when(studentRepository.existsById(7L)).thenReturn(true);
        assertThatThrownBy(() -> service.bulkMark(request(LocalDate.now(),
                new AttendanceEntryRequest(7L, "PRESENT"),
                new AttendanceEntryRequest(7L, "ABSENT"))))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Duplicate student");
    }

    @Test
    void subjectSummaryComputesPercentExcludingExcused() {
        when(studentRepository.findById(7L)).thenReturn(Optional.of(student));
        // 10 total, 8 present, 1 late, 1 absent, 0 excused → counted 10 → 90%
        Object[] row = new Object[]{3L, "Programming in C", "BCA101", 10L, 8L, 1L, 1L, 0L};
        when(attendanceRepository.subjectSummaryByStudentId(7L)).thenReturn(List.<Object[]>of(row));

        List<AttendanceResponse.SubjectSummary> summary = service.subjectSummary(7L);

        assertThat(summary).hasSize(1);
        assertThat(summary.get(0).percentage()).isEqualTo(90.0);
        assertThat(summary.get(0).present()).isEqualTo(8);
    }

    @Test
    void subjectSummaryExcusedNotCounted() {
        when(studentRepository.findById(7L)).thenReturn(Optional.of(student));
        // 10 total, 5 present, 3 excused → counted 7 → 71.43%
        Object[] row = new Object[]{3L, "Programming in C", "BCA101", 10L, 5L, 0L, 2L, 3L};
        when(attendanceRepository.subjectSummaryByStudentId(7L)).thenReturn(List.<Object[]>of(row));

        List<AttendanceResponse.SubjectSummary> summary = service.subjectSummary(7L);

        assertThat(summary.get(0).percentage()).isEqualTo(71.43);
    }

    @Test
    void findByStudentThrowsWhenMissing() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findByStudent(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void overallSummaryHandlesNoData() {
        when(studentRepository.findById(7L)).thenReturn(Optional.of(student));
        when(attendanceRepository.overallByStudentId(7L)).thenReturn(new Object[]{null, null});

        AttendanceResponse.OverallSummary summary = service.overallSummary(7L);
        assertThat(summary.percentage()).isEqualTo(0);
        assertThat(summary.total()).isEqualTo(0);
    }
}
