package com.college.sms.service;

import com.college.sms.dto.AssignmentRequest;
import com.college.sms.dto.GradeRequest;
import com.college.sms.dto.SubmissionRequest;
import com.college.sms.dto.SubmissionResponse;
import com.college.sms.entity.Assignment;
import com.college.sms.entity.AssignmentSubmission;
import com.college.sms.entity.Course;
import com.college.sms.entity.Semester;
import com.college.sms.entity.Student;
import com.college.sms.entity.Subject;
import com.college.sms.exception.BadRequestException;
import com.college.sms.exception.DuplicateResourceException;
import com.college.sms.repository.AssignmentRepository;
import com.college.sms.repository.AssignmentSubmissionRepository;
import com.college.sms.repository.StudentRepository;
import com.college.sms.repository.SubjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssignmentSubmissionServiceTest {

    @Mock AssignmentRepository assignmentRepository;
    @Mock AssignmentSubmissionRepository submissionRepository;
    @Mock SubjectRepository subjectRepository;
    @Mock StudentRepository studentRepository;

    @InjectMocks AssignmentService assignmentService;
    @InjectMocks SubmissionService submissionService;

    private Subject subject;
    private Assignment assignment;
    private Student student;

    @BeforeEach
    void setup() {
        subject = new Subject();
        subject.setId(1L);
        subject.setSubjectCode("BCA101");
        subject.setSubjectName("Programming in C");
        subject.setFacultyId(1L);

        assignment = Assignment.builder()
                .id(10L)
                .subject(subject)
                .facultyId(1L)
                .title("C Worksheet")
                .assignmentType("HOMEWORK")
                .maxMarks(20)
                .status("ACTIVE")
                .dueDate(LocalDateTime.now().plusDays(3))
                .createdAt(java.time.Instant.now())
                .build();

        Course course = new Course();
        course.setId(10L);
        Semester semester = new Semester();
        semester.setId(20L);

        student = new Student();
        student.setId(100L);
        student.setStudentId("ABC2026CS001");
        student.setFirstName("Aarav");
        student.setLastName("Shah");
        student.setCourse(course);
        student.setSemester(semester);
    }

    // ------------------------------------------------------------------
    // assignment creation
    // ------------------------------------------------------------------

    @Test
    void createAssignmentStoresFacultyFromSubjectAndDefaultsStatus() {
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(inv -> {
            Assignment a = inv.getArgument(0);
            a.setId(10L);
            a.setSubject(subject);
            a.setCreatedAt(java.time.Instant.now());
            return a;
        });

        AssignmentRequest req = new AssignmentRequest();
        req.setSubjectId(1L);
        req.setTitle("C Worksheet");
        req.setMaxMarks(20);

        var res = assignmentService.create(req, null);

        assertThat(res.status()).isEqualTo("ACTIVE");
        assertThat(res.facultyId()).isEqualTo(1L);
        assertThat(res.subjectCode()).isEqualTo("BCA101");
    }

    @Test
    void createAssignmentRejectsInvalidType() {
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        AssignmentRequest req = new AssignmentRequest();
        req.setSubjectId(1L);
        req.setTitle("X");
        req.setAssignmentType("QUIZ");

        assertThatThrownBy(() -> assignmentService.create(req, null))
                .isInstanceOf(BadRequestException.class);
    }

    // ------------------------------------------------------------------
    // submission: on-time vs late vs duplicate
    // ------------------------------------------------------------------

    @Test
    void submitBeforeDueDateRecordsSubmitted() {
        when(assignmentRepository.findById(10L)).thenReturn(Optional.of(assignment));
        when(submissionRepository.findByAssignmentIdAndStudentId(10L, 100L)).thenReturn(Optional.empty());
        when(studentRepository.findById(100L)).thenReturn(Optional.of(student));
        when(submissionRepository.save(any(AssignmentSubmission.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        SubmissionRequest req = new SubmissionRequest();
        req.setSubmissionText("My solutions");

        SubmissionResponse res = submissionService.submit(10L, req, 100L);

        assertThat(res.status()).isEqualTo("SUBMITTED");
        assertThat(res.studentName()).isEqualTo("Aarav Shah");
    }

    @Test
    void submitAfterDueDateRecordsLateAutomatically() {
        assignment.setDueDate(LocalDateTime.now().minusDays(1));
        when(assignmentRepository.findById(10L)).thenReturn(Optional.of(assignment));
        when(submissionRepository.findByAssignmentIdAndStudentId(10L, 100L)).thenReturn(Optional.empty());
        when(studentRepository.findById(100L)).thenReturn(Optional.of(student));
        when(submissionRepository.save(any(AssignmentSubmission.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        SubmissionRequest req = new SubmissionRequest();
        req.setSubmissionText("Sorry, late");

        SubmissionResponse res = submissionService.submit(10L, req, 100L);

        assertThat(res.status()).isEqualTo("LATE");
    }

    @Test
    void duplicateSubmissionRejected() {
        when(assignmentRepository.findById(10L)).thenReturn(Optional.of(assignment));
        when(submissionRepository.findByAssignmentIdAndStudentId(10L, 100L))
                .thenReturn(Optional.of(new AssignmentSubmission()));

        SubmissionRequest req = new SubmissionRequest();
        req.setSubmissionText("again");

        assertThatThrownBy(() -> submissionService.submit(10L, req, 100L))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void emptySubmissionRejected() {
        when(assignmentRepository.findById(10L)).thenReturn(Optional.of(assignment));

        SubmissionRequest req = new SubmissionRequest();

        assertThatThrownBy(() -> submissionService.submit(10L, req, 100L))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void closedAssignmentRejectsSubmissions() {
        assignment.setStatus("CLOSED");
        when(assignmentRepository.findById(10L)).thenReturn(Optional.of(assignment));

        SubmissionRequest req = new SubmissionRequest();
        req.setSubmissionText("text");

        assertThatThrownBy(() -> submissionService.submit(10L, req, 100L))
                .isInstanceOf(BadRequestException.class);
    }

    // ------------------------------------------------------------------
    // grading
    // ------------------------------------------------------------------

    @Test
    void gradeStoresMarksFeedbackAndGradedStatus() {
        AssignmentSubmission submission = AssignmentSubmission.builder()
                .id(50L)
                .assignment(assignment)
                .student(student)
                .submittedAt(LocalDateTime.now())
                .status("SUBMITTED")
                .build();
        when(submissionRepository.findById(50L)).thenReturn(Optional.of(submission));
        when(submissionRepository.save(any(AssignmentSubmission.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        GradeRequest req = new GradeRequest();
        req.setMarksObtained(new BigDecimal("17.5"));
        req.setFeedback("Good work, watch the pointer arithmetic");

        SubmissionResponse res = submissionService.grade(50L, req);

        assertThat(res.status()).isEqualTo("GRADED");
        assertThat(res.marksObtained()).isEqualByComparingTo("17.5");
        assertThat(res.feedback()).contains("pointer");
        ArgumentCaptor<AssignmentSubmission> captor = ArgumentCaptor.forClass(AssignmentSubmission.class);
        verify(submissionRepository).save(captor.capture());
        assertThat(captor.getValue().getGradedAt()).isNotNull();
    }

    @Test
    void gradeAboveMaxRejected() {
        AssignmentSubmission submission = AssignmentSubmission.builder()
                .id(50L)
                .assignment(assignment) // max 20
                .student(student)
                .submittedAt(LocalDateTime.now())
                .status("SUBMITTED")
                .build();
        when(submissionRepository.findById(50L)).thenReturn(Optional.of(submission));

        GradeRequest req = new GradeRequest();
        req.setMarksObtained(new BigDecimal("25"));

        assertThatThrownBy(() -> submissionService.grade(50L, req))
                .isInstanceOf(BadRequestException.class);
    }
}
