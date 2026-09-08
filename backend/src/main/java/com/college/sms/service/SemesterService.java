package com.college.sms.service;

import com.college.sms.dto.SemesterRequest;
import com.college.sms.dto.SemesterResponse;
import com.college.sms.entity.AcademicYear;
import com.college.sms.entity.Semester;
import com.college.sms.exception.BadRequestException;
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
public class SemesterService {

    private final SemesterRepository semesterRepository;
    private final AcademicYearRepository academicYearRepository;

    @Transactional(readOnly = true)
    public List<SemesterResponse> findAll() {
        return semesterRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<SemesterResponse> findByYear(Long academicYearId) {
        return semesterRepository.findByAcademicYearIdOrderBySemesterNumber(academicYearId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public SemesterResponse create(SemesterRequest request) {
        AcademicYear year = academicYearRepository.findById(request.academicYearId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Academic year not found with id " + request.academicYearId()));
        if (semesterRepository.existsByAcademicYearIdAndSemesterNumber(
                request.academicYearId(), request.semesterNumber())) {
            throw new DuplicateResourceException(
                    "Semester " + request.semesterNumber() + " already exists for " + year.getYearName());
        }
        Semester semester = Semester.builder()
                .semesterNumber(request.semesterNumber())
                .semesterName(request.semesterName() != null ? request.semesterName()
                        : "Semester " + request.semesterNumber())
                .academicYear(year)
                .build();
        return toResponse(semesterRepository.save(semester));
    }

    @Transactional
    public SemesterResponse update(Long id, SemesterRequest request) {
        Semester semester = getSemester(id);
        if (semesterRepository.existsByAcademicYearIdAndSemesterNumber(
                request.academicYearId(), request.semesterNumber())
                && !semester.getId().equals(id)) {
            throw new DuplicateResourceException("Semester number already exists for that academic year");
        }
        AcademicYear year = academicYearRepository.findById(request.academicYearId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Academic year not found with id " + request.academicYearId()));
        semester.setSemesterNumber(request.semesterNumber());
        semester.setSemesterName(request.semesterName());
        semester.setAcademicYear(year);
        return toResponse(semesterRepository.save(semester));
    }

    @Transactional
    public void delete(Long id) {
        Semester semester = getSemester(id);
        semesterRepository.delete(semester);
    }

    private Semester getSemester(Long id) {
        return semesterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Semester not found with id " + id));
    }

    private SemesterResponse toResponse(Semester s) {
        return new SemesterResponse(s.getId(), s.getSemesterNumber(), s.getSemesterName(),
                s.getAcademicYear().getId(), s.getAcademicYear().getYearName(), s.getAcademicYear().isActive());
    }
}
