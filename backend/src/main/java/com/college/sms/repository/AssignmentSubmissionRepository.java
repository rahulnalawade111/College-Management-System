package com.college.sms.repository;

import com.college.sms.entity.AssignmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {

    Optional<AssignmentSubmission> findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);

    List<AssignmentSubmission> findByAssignmentIdOrderBySubmittedAtAsc(Long assignmentId);

    List<AssignmentSubmission> findByStudentIdOrderBySubmittedAtDesc(Long studentId);

    boolean existsByAssignmentId(Long assignmentId);
}
