package com.college.sms.service;

import com.college.sms.dto.AcademicYearRequest;
import com.college.sms.dto.AcademicYearResponse;
import com.college.sms.entity.AcademicYear;
import com.college.sms.exception.DuplicateResourceException;
import com.college.sms.exception.ResourceNotFoundException;
import com.college.sms.repository.AcademicYearRepository;
import com.college.sms.repository.SemesterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AcademicYearService {

    private final AcademicYearRepository academicYearRepository;
    private final SemesterRepository semesterRepository;

    @Transactional(readOnly = true)
    public List<AcademicYearResponse> findAll() {
        return academicYearRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AcademicYearResponse findActive() {
        return academicYearRepository.findByActiveTrue()
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("No active academic year configured"));
    }

    @Transactional
    public AcademicYearResponse create(AcademicYearRequest request) {
        if (academicYearRepository.existsByYearName(request.yearName())) {
            throw new DuplicateResourceException("Academic year already exists: " + request.yearName());
        }
        AcademicYear year = AcademicYear.builder()
                .yearName(request.yearName())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .active(request.active())
                .build();
        return toResponse(academicYearRepository.save(year));
    }

    @Transactional
    public AcademicYearResponse update(Long id, AcademicYearRequest request) {
        AcademicYear year = getYear(id);
        year.setYearName(request.yearName());
        year.setStartDate(request.startDate());
        year.setEndDate(request.endDate());
        year.setActive(request.active());
        return toResponse(academicYearRepository.save(year));
    }

    /** Setting a year active deactivates all others (single active year invariant). */
    @Transactional
    public AcademicYearResponse setActive(Long id) {
        academicYearRepository.findAll().forEach(y -> {
            if (y.isActive()) {
                y.setActive(false);
                academicYearRepository.save(y);
            }
        });
        AcademicYear year = getYear(id);
        year.setActive(true);
        return toResponse(academicYearRepository.save(year));
    }

    @Transactional
    public void delete(Long id) {
        AcademicYear year = getYear(id);
        if (semesterRepository.existsByAcademicYearId(id)) {
            throw new com.college.sms.exception.BadRequestException(
                    "Cannot delete academic year " + year.getYearName() + " — semesters reference it.");
        }
        academicYearRepository.delete(year);
    }

    private AcademicYear getYear(Long id) {
        return academicYearRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with id " + id));
    }

    private AcademicYearResponse toResponse(AcademicYear y) {
        return new AcademicYearResponse(y.getId(), y.getYearName(), y.getStartDate(), y.getEndDate(), y.isActive());
    }
}
