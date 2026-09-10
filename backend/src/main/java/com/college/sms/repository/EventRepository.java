package com.college.sms.repository;

import com.college.sms.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByStatusIgnoreCaseAndEventDateGreaterThanEqualOrderByEventDateAsc(String status, LocalDate date);

    List<Event> findByStatusIgnoreCaseOrderByEventDateDesc(String status);
}
