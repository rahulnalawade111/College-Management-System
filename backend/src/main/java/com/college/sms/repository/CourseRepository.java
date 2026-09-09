package com.college.sms.repository;

import com.college.sms.entity.Course;
import com.college.sms.entity.CourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CourseRepository extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {

    long countByStatus(CourseStatus status);

    boolean existsByCourseCodeIgnoreCase(String courseCode);

    boolean existsByCourseCodeIgnoreCaseAndIdNot(String courseCode, Long id);

    Page<Course> findByCourseNameContainingIgnoreCaseOrCourseCodeContainingIgnoreCase(String name, String code, Pageable pageable);

    boolean existsByDepartmentId(Long departmentId);
}
