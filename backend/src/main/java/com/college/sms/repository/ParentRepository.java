package com.college.sms.repository;

import com.college.sms.entity.Parent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParentRepository extends JpaRepository<Parent, Long> {

    List<Parent> findByStudentId(Long studentId);

    @Query("select p from Parent p left join fetch p.student where p.id = :id")
    Optional<Parent> findByIdWithStudent(@Param("id") Long id);

    void deleteByStudentId(Long studentId);

    /** Login-linked parents: users.parent_id points at the parents row. */
    @Query("select p from Parent p left join fetch p.student " +
            "where p.id = (select u.parentId from User u where u.id = :userId) order by p.id")
    List<Parent> findByUserIdWithStudent(@Param("userId") Long userId);
}
