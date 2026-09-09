package com.college.sms.repository;

import com.college.sms.entity.Fee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeeRepository extends JpaRepository<Fee, Long> {

    Page<Fee> findByStudentId(Long studentId, Pageable pageable);

    List<Fee> findByStudentIdOrderByDueDateAsc(Long studentId);

    List<Fee> findByStatus(String status);

    @Query("select coalesce(sum(f.amount), 0), coalesce(sum(f.paidAmount), 0) from Fee f")
    List<Object[]> totalsAll();

    @Query("select coalesce(sum(f.amount), 0), coalesce(sum(f.paidAmount), 0) from Fee f where f.student.id = :studentId")
    List<Object[]> totalsForStudent(@Param("studentId") Long studentId);

    @Query("select f.status, count(f) from Fee f group by f.status")
    List<Object[]> countByStatus();
}
