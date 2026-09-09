package com.college.sms.service;

import com.college.sms.dto.MarksEntryRequest;
import com.college.sms.dto.ResultResponse;
import com.college.sms.entity.*;
import com.college.sms.exception.BadRequestException;
import com.college.sms.exception.ResourceNotFoundException;
import com.college.sms.repository.ExamRepository;
import com.college.sms.repository.ResultRepository;
import com.college.sms.repository.StudentRepository;
import com.college.sms.repository.SubjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ResultServiceTest {

    @Mock private ResultRepository resultRepository;
    @Mock private ExamRepository examRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private SubjectRepository subjectRepository;
    @Mock private AcademicCalculationService calc;

    @InjectMocks private ResultService resultService;

    private Exam exam;
    private Student student;
    private Subject subject;

    @BeforeEach
    void setUp() {
        Course course = Course.builder().id(1L).courseName("BCA").build();
        AcademicYear year = AcademicYear.builder().id(1L).yearName("2025-2026").build();
        exam = Exam.builder().id(1L).examName("Midterm").academicYear(year).resultPublished(false).build();
        student = Student.builder().id(1L).studentId("STU001").firstName("Aarav").lastName("Shah")
                .course(course).build();
        subject = Subject.builder().id(1L).subjectCode("BSC101").subjectName("Math").credits(4).build();

        lenient().when(examRepository.findById(1L)).thenReturn(Optional.of(exam));
        lenient().when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        lenient().when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        lenient().when(resultRepository.findByExamIdAndStudentIdAndSubjectId(1L, 1L, 1L))
                .thenReturn(Optional.empty());
        lenient().when(resultRepository.findByExamIdAndStudentIdOrderBySubjectIdAsc(1L, 1L))
                .thenReturn(List.of());
        lenient().when(resultRepository.save(any(Result.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        lenient().when(resultRepository.findByExamId(1L)).thenReturn(List.of());
        lenient().when(resultRepository.sgpaComponents(anyLong(), anyLong()))
                .thenReturn(List.<Object[]>of(new Object[]{new BigDecimal("32.0"), 4}));
        lenient().when(resultRepository.cgpaComponents(anyLong()))
                .thenReturn(List.<Object[]>of(new Object[]{new BigDecimal("32.0"), 4}));
        lenient().when(calc.computeSgpa(any(), org.mockito.ArgumentMatchers.anyInt()))
                .thenReturn(new BigDecimal("8.00"));
    }

    private MarksEntryRequest entry(BigDecimal internal, BigDecimal external, BigDecimal practical) {
        return new MarksEntryRequest(1L, 1L, internal, external, practical, 4);
    }

    @Test
    void enterMarks_savesNewResult() {
        lenient().when(calc.computePercent(any(), org.mockito.ArgumentMatchers.anyInt())).thenReturn(new BigDecimal("75"));
        lenient().when(calc.computeGrade(any())).thenReturn(new AcademicCalculationService.GradeResult("B", new BigDecimal("7.0")));

        List<ResultResponse> out = resultService.enterMarks(1L, List.of(entry(new BigDecimal(25), new BigDecimal(50), null)));
        assertThat(out).hasSize(1);
        assertThat(out.get(0).totalMarks()).isEqualByComparingTo("75");
        assertThat(out.get(0).grade()).isEqualTo("B");
    }

    @Test
    void enterMarks_rejectsOverMaxTotal() {
        assertThatThrownBy(() -> resultService.enterMarks(1L, List.of(entry(new BigDecimal(50), new BigDecimal(100), null))))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("exceed maximum");
    }

    @Test
    void enterMarks_rejectsNegativeInternal() {
        assertThatThrownBy(() -> resultService.enterMarks(1L, List.of(entry(new BigDecimal(-5), new BigDecimal(40), null))))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Internal");
    }

    @Test
    void enterMarks_rejectsExternalOverCap() {
        assertThatThrownBy(() -> resultService.enterMarks(1L, List.of(entry(new BigDecimal(25), new BigDecimal(150), null))))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("External");
    }

    @Test
    void enterMarks_updatesExistingUnpublished() {
        Result existing = Result.builder().id(5L).exam(exam).student(student).subject(subject)
                .totalMarks(new BigDecimal("60")).maxMarks(100).grade("C").published(false).build();
        when(resultRepository.findByExamIdAndStudentIdAndSubjectId(1L, 1L, 1L))
                .thenReturn(Optional.of(existing));
        when(calc.computePercent(any(), org.mockito.ArgumentMatchers.anyInt())).thenReturn(new BigDecimal("80"));
        when(calc.computeGrade(any())).thenReturn(new AcademicCalculationService.GradeResult("A", new BigDecimal("8.0")));

        List<ResultResponse> out = resultService.enterMarks(1L, List.of(entry(new BigDecimal(30), new BigDecimal(50), null)));
        assertThat(out).hasSize(1);
        assertThat(out.get(0).id()).isEqualTo(5L);
        assertThat(out.get(0).grade()).isEqualTo("A");
    }

    @Test
    void enterMarks_rejectsPublishedResult() {
        Result locked = Result.builder().id(5L).exam(exam).student(student).subject(subject)
                .published(true).build();
        when(resultRepository.findByExamIdAndStudentIdAndSubjectId(1L, 1L, 1L))
                .thenReturn(Optional.of(locked));

        assertThatThrownBy(() -> resultService.enterMarks(1L, List.of(entry(new BigDecimal(25), new BigDecimal(50), null))))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("published/locked");
    }

    @Test
    void publishExam_locksAllAndSetsFlag() {
        Result r1 = Result.builder().id(1L).exam(exam).student(student).subject(subject).published(false).build();
        Result r2 = Result.builder().id(2L).exam(exam).student(student).subject(subject).published(false).build();
        when(resultRepository.findByExamId(1L)).thenReturn(List.of(r1, r2));

        int count = resultService.publishExam(1L);
        assertThat(count).isEqualTo(2);
        assertThat(r1.isPublished()).isTrue();
        assertThat(r2.isPublished()).isTrue();
        assertThat(exam.isResultPublished()).isTrue();
        assertThat(r1.getPublishedAt()).isNotNull();
    }

    @Test
    void publishExam_rejectsEmptyResults() {
        assertThatThrownBy(() -> resultService.publishExam(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("No results");
    }

    @Test
    void unpublishExam_unlocksAll() {
        Result r1 = Result.builder().id(1L).exam(exam).student(student).subject(subject).published(true).build();
        when(resultRepository.findByExamId(1L)).thenReturn(List.of(r1));

        int count = resultService.unpublishExam(1L);
        assertThat(count).isEqualTo(1);
        assertThat(r1.isPublished()).isFalse();
        assertThat(exam.isResultPublished()).isFalse();
    }

    @Test
    void marksheet_throwsWhenNoResults() {
        assertThatThrownBy(() -> resultService.marksheet(1L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No results");
    }

    @Test
    void marksheet_buildsSubjectLinesAndGpa() {
        Subject s2 = Subject.builder().id(2L).subjectCode("CS102").subjectName("Prog").credits(3).build();
        lenient().when(subjectRepository.findById(2L)).thenReturn(Optional.of(s2));
        Result r1 = Result.builder().id(1L).exam(exam).student(student).subject(subject)
                .internalMarks(new BigDecimal("25")).externalMarks(new BigDecimal("55"))
                .totalMarks(new BigDecimal("80")).maxMarks(100).grade("A")
                .gradePoint(new BigDecimal("8.0")).credits(4).published(true).build();
        when(resultRepository.findByExamIdAndStudentIdOrderBySubjectIdAsc(1L, 1L))
                .thenReturn(List.of(r1));

        ResultResponse.Marksheet ms = resultService.marksheet(1L, 1L);
        assertThat(ms.studentCode()).isEqualTo("STU001");
        assertThat(ms.studentName()).isEqualTo("Aarav Shah");
        assertThat(ms.courseName()).isEqualTo("BCA");
        assertThat(ms.examName()).isEqualTo("Midterm");
        assertThat(ms.academicYearName()).isEqualTo("2025-2026");
        assertThat(ms.subjects()).hasSize(1);
        assertThat(ms.subjects().get(0).subjectCode()).isEqualTo("BSC101");
        assertThat(ms.sgpa()).isEqualByComparingTo("8.00");
        assertThat(ms.cgpa()).isEqualByComparingTo("8.00");
    }

    @Test
    void missingStudent_throws() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> resultService.enterMarks(1L,
                List.of(new MarksEntryRequest(99L, 1L, BigDecimal.TEN, BigDecimal.TEN, null, 4))))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }
}
