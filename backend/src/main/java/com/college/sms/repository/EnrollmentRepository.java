package com.college.sms.repository;

import com.college.sms.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByStudentId(Long studentId);

    boolean existsByStudentId(Long studentId);

    boolean existsByStudentIdAndAcademicYearIdAndSemesterId(Long studentId, Long academicYearId, Long semesterId);

    boolean existsByCourseId(Long courseId);

    boolean existsByAcademicYearId(Long academicYearId);

    boolean existsBySemesterId(Long semesterId);
}
