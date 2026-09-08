package com.college.sms.service;

import com.college.sms.dto.CourseRequest;
import com.college.sms.dto.CourseResponse;
import com.college.sms.entity.Course;
import com.college.sms.entity.CourseStatus;
import com.college.sms.entity.Department;
import com.college.sms.exception.BadRequestException;
import com.college.sms.exception.DuplicateResourceException;
import com.college.sms.exception.ResourceNotFoundException;
import com.college.sms.repository.CourseRepository;
import com.college.sms.repository.DepartmentRepository;
import com.college.sms.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;
    private final SubjectRepository subjectRepository;

    @Transactional(readOnly = true)
    public Page<CourseResponse> findAll(String search, Long departmentId, String status, Pageable pageable) {
        Specification<Course> spec = Specification.where(null);
        if (search != null && !search.isBlank()) {
            String term = "%" + search.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("courseName")), term),
                    cb.like(cb.lower(root.get("courseCode")), term)));
        }
        if (departmentId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("department").get("id"), departmentId));
        }
        if (status != null && !status.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), CourseStatus.valueOf(status)));
        }
        return courseRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public CourseResponse findById(Long id) {
        return toResponse(getCourse(id));
    }

    @Transactional
    public CourseResponse create(CourseRequest request) {
        if (courseRepository.existsByCourseCodeIgnoreCase(request.courseCode())) {
            throw new DuplicateResourceException("Course code already exists: " + request.courseCode());
        }
        Department department = getDepartment(request.departmentId());
        Course course = Course.builder()
                .courseCode(request.courseCode())
                .courseName(request.courseName())
                .description(request.description())
                .duration(request.duration())
                .degreeType(request.degreeType())
                .department(department)
                .totalSemesters(request.totalSemesters())
                .fees(request.fees())
                .status(parseStatus(request.status()))
                .build();
        return toResponse(courseRepository.save(course));
    }

    @Transactional
    public CourseResponse update(Long id, CourseRequest request) {
        Course course = getCourse(id);
        if (courseRepository.existsByCourseCodeIgnoreCaseAndIdNot(request.courseCode(), id)) {
            throw new DuplicateResourceException("Course code already exists: " + request.courseCode());
        }
        course.setCourseCode(request.courseCode());
        course.setCourseName(request.courseName());
        course.setDescription(request.description());
        course.setDuration(request.duration());
        course.setDegreeType(request.degreeType());
        course.setDepartment(getDepartment(request.departmentId()));
        course.setTotalSemesters(request.totalSemesters());
        course.setFees(request.fees());
        course.setStatus(parseStatus(request.status()));
        return toResponse(courseRepository.save(course));
    }

    @Transactional
    public void delete(Long id) {
        Course course = getCourse(id);
        if (subjectRepository.existsByCourseId(id)) {
            throw new BadRequestException(
                    "Cannot delete course " + course.getCourseCode() + " — subjects reference it.");
        }
        courseRepository.delete(course);
    }

    private Course getCourse(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id " + id));
    }

    private Department getDepartment(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id " + id));
    }

    private CourseStatus parseStatus(String status) {
        if (status == null || status.isBlank()) return CourseStatus.ACTIVE;
        try {
            return CourseStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status: " + status);
        }
    }

    private CourseResponse toResponse(Course c) {
        return new CourseResponse(c.getId(), c.getCourseCode(), c.getCourseName(), c.getDescription(),
                c.getDuration(), c.getDegreeType(),
                c.getDepartment().getId(), c.getDepartment().getDepartmentCode(),
                c.getDepartment().getDepartmentName(),
                c.getTotalSemesters(), c.getFees(), c.getStatus().name());
    }
}
