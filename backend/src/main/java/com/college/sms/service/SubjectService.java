package com.college.sms.service;

import com.college.sms.dto.SubjectRequest;
import com.college.sms.dto.SubjectResponse;
import com.college.sms.entity.Course;
import com.college.sms.entity.Department;
import com.college.sms.entity.Semester;
import com.college.sms.entity.Subject;
import com.college.sms.exception.BadRequestException;
import com.college.sms.exception.DuplicateResourceException;
import com.college.sms.exception.ResourceNotFoundException;
import com.college.sms.repository.CourseRepository;
import com.college.sms.repository.DepartmentRepository;
import com.college.sms.repository.SemesterRepository;
import com.college.sms.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final SemesterRepository semesterRepository;
    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;

    @Transactional(readOnly = true)
    public Page<SubjectResponse> findAll(String search, Long courseId, Long semesterId,
                                         Long departmentId, Long facultyId, Pageable pageable) {
        Specification<Subject> spec = Specification.where(null);
        if (search != null && !search.isBlank()) {
            String term = "%" + search.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("subjectName")), term),
                    cb.like(cb.lower(root.get("subjectCode")), term)));
        }
        if (courseId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("course").get("id"), courseId));
        }
        if (semesterId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("semester").get("id"), semesterId));
        }
        if (departmentId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("department").get("id"), departmentId));
        }
        if (facultyId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("facultyId"), facultyId));
        }
        return subjectRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional
    public SubjectResponse create(SubjectRequest request) {
        if (subjectRepository.existsBySubjectCodeIgnoreCase(request.subjectCode())) {
            throw new DuplicateResourceException("Subject code already exists: " + request.subjectCode());
        }
        Subject subject = Subject.builder()
                .subjectCode(request.subjectCode())
                .subjectName(request.subjectName())
                .credits(request.credits())
                .semester(getSemester(request.semesterId()))
                .course(getCourse(request.courseId()))
                .department(getDepartment(request.departmentId()))
                .facultyId(request.facultyId())
                .description(request.description())
                .build();
        return toResponse(subjectRepository.save(subject));
    }

    @Transactional
    public SubjectResponse update(Long id, SubjectRequest request) {
        Subject subject = getSubject(id);
        if (subjectRepository.existsBySubjectCodeIgnoreCaseAndIdNot(request.subjectCode(), id)) {
            throw new DuplicateResourceException("Subject code already exists: " + request.subjectCode());
        }
        subject.setSubjectCode(request.subjectCode());
        subject.setSubjectName(request.subjectName());
        subject.setCredits(request.credits());
        subject.setSemester(getSemester(request.semesterId()));
        subject.setCourse(getCourse(request.courseId()));
        subject.setDepartment(getDepartment(request.departmentId()));
        subject.setFacultyId(request.facultyId());
        subject.setDescription(request.description());
        return toResponse(subjectRepository.save(subject));
    }

    @Transactional
    public void delete(Long id) {
        Subject subject = getSubject(id);
        subjectRepository.delete(subject);
    }

    private Subject getSubject(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id " + id));
    }

    private Semester getSemester(Long id) {
        return semesterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Semester not found with id " + id));
    }

    private Course getCourse(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id " + id));
    }

    private Department getDepartment(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id " + id));
    }

    private SubjectResponse toResponse(Subject s) {
        return new SubjectResponse(s.getId(), s.getSubjectCode(), s.getSubjectName(), s.getCredits(),
                s.getSemester().getId(), s.getSemester().getSemesterName(),
                s.getCourse().getId(), s.getCourse().getCourseCode(), s.getCourse().getCourseName(),
                s.getDepartment().getId(), s.getDepartment().getDepartmentName(),
                s.getFacultyId(), s.getDescription());
    }
}
