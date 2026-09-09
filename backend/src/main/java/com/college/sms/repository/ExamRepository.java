package com.college.sms.repository;

import com.college.sms.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {

    List<Exam> findAllByOrderByStartDateAsc();

    List<Exam> findByAcademicYearIdOrderByStartDateAsc(Long academicYearId);

    List<Exam> findByStatusOrderByStartDateAsc(String status);

    List<Exam> findByResultPublishedTrue();
}
