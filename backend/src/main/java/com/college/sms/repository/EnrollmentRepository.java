package com.college.sms.repository;

import com.college.sms.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    long countByCourseIdAndSemesterId(Long courseId, Long semesterId);

    @Query("select distinct e.student.id from Enrollment e " +
            "where e.course.id = :courseId and e.semester.id = :semesterId")
    List<Long> findDistinctStudentIdsByCourseIdAndSemesterId(@Param("courseId") Long courseId,
                                                             @Param("semesterId") Long semesterId);
}
