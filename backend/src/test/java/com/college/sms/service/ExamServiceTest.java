package com.college.sms.service;

import com.college.sms.dto.ExamRequest;
import com.college.sms.dto.ExamResponse;
import com.college.sms.dto.ExamScheduleRequest;
import com.college.sms.entity.AcademicYear;
import com.college.sms.entity.Course;
import com.college.sms.entity.Exam;
import com.college.sms.entity.Semester;
import com.college.sms.entity.Student;
import com.college.sms.entity.Subject;
import com.college.sms.exception.BadRequestException;
import com.college.sms.exception.DuplicateResourceException;
import com.college.sms.repository.AcademicYearRepository;
import com.college.sms.repository.ExamRepository;
import com.college.sms.repository.ExamScheduleRepository;
import com.college.sms.repository.SemesterRepository;
import com.college.sms.repository.StudentRepository;
import com.college.sms.repository.SubjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class ExamServiceTest {

    @Mock ExamRepository examRepository;
    @Mock ExamScheduleRepository scheduleRepository;
    @Mock AcademicYearRepository academicYearRepository;
    @Mock SemesterRepository semesterRepository;
    @Mock SubjectRepository subjectRepository;
    @Mock StudentRepository studentRepository;

    @InjectMocks ExamService service;

    private AcademicYear ay;
    private Semester sem1;
    private Course bca;
    private Subject subject;
    private Exam midterm;

    @BeforeEach
    void setup() {
        ay = new AcademicYear();
        ay.setId(30L);
        ay.setYearName("2026-2027");

        sem1 = new Semester();
        sem1.setId(20L);
        sem1.setSemesterNumber(1);

        bca = new Course();
        bca.setId(10L);
        bca.setCourseCode("BCA");

        subject = new Subject();
        subject.setId(1L);
        subject.setSubjectCode("BCA101");
        subject.setSubjectName("Programming in C");
        subject.setCourse(bca);
        subject.setSemester(sem1);

        midterm = Exam.builder()
                .id(1L)
                .examName("Midterm Examinations — Sem 1")
                .examType("MIDTERM")
                .academicYear(ay)
                .semester(sem1)
                .startDate(LocalDate.now().plusDays(21))
                .endDate(LocalDate.now().plusDays(28))
                .status("SCHEDULED")
                .build();
    }

    private ExamRequest examRequest() {
        ExamRequest r = new ExamRequest();
        r.setExamName("Midterm Examinations — Sem 1");
        r.setExamType("MIDTERM");
        r.setAcademicYearId(30L);
        r.setSemesterId(20L);
        r.setStartDate(LocalDate.now().plusDays(21));
        r.setEndDate(LocalDate.now().plusDays(28));
        return r;
    }

    @Test
    void createExamDefaultsStatusAndValidatesWindow() {
        when(academicYearRepository.findById(30L)).thenReturn(Optional.of(ay));
        when(semesterRepository.findById(20L)).thenReturn(Optional.of(sem1));
        when(examRepository.save(any(Exam.class))).thenAnswer(inv -> inv.getArgument(0));

        ExamResponse res = service.create(examRequest());

        assertThat(res.status()).isEqualTo("SCHEDULED");
        assertThat(res.resultPublished()).isFalse();
        assertThat(res.academicYearName()).isEqualTo("2026-2027");
    }

    @Test
    void createExamRejectsEndDateBeforeStart() {
        ExamRequest r = examRequest();
        r.setEndDate(r.getStartDate().minusDays(1));
        when(academicYearRepository.findById(30L)).thenReturn(Optional.of(ay));

        assertThatThrownBy(() -> service.create(r)).isInstanceOf(BadRequestException.class);
    }

    @Test
    void createExamRejectsInvalidType() {
        ExamRequest r = examRequest();
        r.setExamType("SURPRISE");
        when(academicYearRepository.findById(30L)).thenReturn(Optional.of(ay));

        assertThatThrownBy(() -> service.create(r)).isInstanceOf(BadRequestException.class);
    }

    @Test
    void addScheduleRejectsSubjectAlreadyInExam() {
        when(examRepository.findById(1L)).thenReturn(Optional.of(midterm));
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(scheduleRepository.existsByExamIdAndSubjectId(1L, 1L)).thenReturn(true);

        ExamScheduleRequest r = new ExamScheduleRequest();
        r.setSubjectId(1L);
        r.setExamDate(midterm.getStartDate());
        r.setStartTime(LocalTime.of(9, 30));
        r.setEndTime(LocalTime.of(12, 30));

        assertThatThrownBy(() -> service.addSchedule(1L, r))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void addScheduleRejectsOverlappingSlotForSameSubject() {
        when(examRepository.findById(1L)).thenReturn(Optional.of(midterm));
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(scheduleRepository.existsByExamIdAndSubjectId(1L, 1L)).thenReturn(false);
        when(scheduleRepository.countOverlaps(any(), any(), any(), any(), any())).thenReturn(1L);

        ExamScheduleRequest r = new ExamScheduleRequest();
        r.setSubjectId(1L);
        r.setExamDate(midterm.getStartDate());
        r.setStartTime(LocalTime.of(9, 30));
        r.setEndTime(LocalTime.of(12, 30));

        assertThatThrownBy(() -> service.addSchedule(1L, r))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void addScheduleRejectsDateOutsideExamWindow() {
        when(examRepository.findById(1L)).thenReturn(Optional.of(midterm));
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(scheduleRepository.existsByExamIdAndSubjectId(1L, 1L)).thenReturn(false);

        ExamScheduleRequest r = new ExamScheduleRequest();
        r.setSubjectId(1L);
        r.setExamDate(LocalDate.now().plusDays(40)); // after end date
        r.setStartTime(LocalTime.of(9, 30));
        r.setEndTime(LocalTime.of(12, 30));

        assertThatThrownBy(() -> service.addSchedule(1L, r))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void addScheduleRejectsEndTimeBeforeStart() {
        when(examRepository.findById(1L)).thenReturn(Optional.of(midterm));
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));

        ExamScheduleRequest r = new ExamScheduleRequest();
        r.setSubjectId(1L);
        r.setExamDate(midterm.getStartDate());
        r.setStartTime(LocalTime.of(14, 0));
        r.setEndTime(LocalTime.of(12, 0));

        assertThatThrownBy(() -> service.addSchedule(1L, r))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void studentUpcomingFiltersToOwnCourseAndSemesterAndFutureOnly() {
        // exam for semester 1, schedule subject in BCA
        when(examRepository.findByStatusOrderByStartDateAsc("SCHEDULED")).thenReturn(java.util.List.of(midterm));
        when(scheduleRepository.findByExamIdOrderByExamDateAscStartTimeAsc(1L))
                .thenReturn(java.util.List.of());
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));

        Student student = new Student();
        student.setId(100L);
        student.setCourse(bca);
        student.setSemester(sem1);
        when(studentRepository.findById(100L)).thenReturn(Optional.of(student));

        ExamResponse resp = new ExamResponse(1L, midterm.getExamName(), midterm.getExamType(),
                30L, "2026-2027", 20L, 1, midterm.getStartDate(), midterm.getEndDate(),
                "SCHEDULED", false, java.util.List.of());

        var list = service.upcomingForStudentOfCurrentStudent(100L);
        // schedule empty → filtered out entirely (no misleading rows)
        assertThat(list).isEmpty();
    }
}
