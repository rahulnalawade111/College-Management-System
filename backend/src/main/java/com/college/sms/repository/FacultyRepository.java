package com.college.sms.repository;

import com.college.sms.entity.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, Long>, JpaSpecificationExecutor<Faculty> {

    Optional<Faculty> findByEmployeeIdIgnoreCase(String employeeId);

    Optional<Faculty> findByEmailIgnoreCase(String email);

    /** Login-linked faculty: users.faculty_id points at the faculty row. */
    @Query("select f from Faculty f where f.id = (select u.facultyId from User u where u.id = :userId)")
    Optional<Faculty> findByUserId(@Param("userId") Long userId);

    boolean existsByEmployeeIdIgnoreCase(String employeeId);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmployeeIdIgnoreCaseAndIdNot(String employeeId, Long id);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByDepartmentId(Long departmentId);

    long countByDepartmentId(Long departmentId);

    List<Faculty> findTop3ByCreatedAtGreaterThanEqualOrderByCreatedAtDesc(Instant since);
}
