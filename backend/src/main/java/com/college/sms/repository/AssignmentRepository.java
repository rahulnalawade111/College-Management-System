package com.college.sms.repository;

import com.college.sms.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findBySubjectIdInOrderByDueDateAsc(List<Long> subjectIds);

    List<Assignment> findBySubjectIdOrderByDueDateDesc(Long subjectId);

    List<Assignment> findByFacultyIdOrderByCreatedAtDesc(Long facultyId);

    boolean existsBySubjectId(Long subjectId);
}
