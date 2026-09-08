package com.college.sms.repository;

import com.college.sms.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {

    @Query("select s from Student s " +
            "join fetch s.course join fetch s.department join fetch s.semester join fetch s.academicYear " +
            "where s.id = :id")
    Optional<Student> findByIdWithReferences(@Param("id") Long id);

    Optional<Student> findByStudentIdIgnoreCase(String studentId);

    Optional<Student> findByEmailIgnoreCase(String email);

    boolean existsByStudentIdIgnoreCase(String studentId);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByStudentIdIgnoreCaseAndIdNot(String studentId, Long id);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
