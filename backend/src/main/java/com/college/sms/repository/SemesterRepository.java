package com.college.sms.repository;

import com.college.sms.entity.AcademicYear;
import com.college.sms.entity.Semester;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SemesterRepository extends JpaRepository<Semester, Long> {

    Optional<Semester> findByAcademicYearAndSemesterNumber(AcademicYear academicYear, Integer semesterNumber);

    List<Semester> findByAcademicYearIdOrderBySemesterNumber(Long academicYearId);

    boolean existsByAcademicYearIdAndSemesterNumber(Long academicYearId, Integer semesterNumber);

    boolean existsByAcademicYearId(Long academicYearId);
}
