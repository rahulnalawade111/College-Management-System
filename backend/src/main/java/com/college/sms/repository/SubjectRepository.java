package com.college.sms.repository;

import com.college.sms.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long>, JpaSpecificationExecutor<Subject> {

    boolean existsBySubjectCodeIgnoreCase(String subjectCode);

    boolean existsBySubjectCodeIgnoreCaseAndIdNot(String subjectCode, Long id);

    List<Subject> findByCourseIdAndSemesterId(Long courseId, Long semesterId);

    List<Subject> findByFacultyId(Long facultyId);

    boolean existsBySemesterId(Long semesterId);

    boolean existsByCourseId(Long courseId);

    boolean existsByDepartmentId(Long departmentId);
}
