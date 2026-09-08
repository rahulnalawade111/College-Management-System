package com.college.sms.service;

import com.college.sms.dto.SemesterRequest;
import com.college.sms.entity.AcademicYear;
import com.college.sms.exception.DuplicateResourceException;
import com.college.sms.exception.ResourceNotFoundException;
import com.college.sms.repository.AcademicYearRepository;
import com.college.sms.repository.SemesterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SemesterServiceTest {

    @Mock
    private SemesterRepository semesterRepository;
    @Mock
    private AcademicYearRepository academicYearRepository;

    private SemesterService service;

    @BeforeEach
    void setUp() {
        service = new SemesterService(semesterRepository, academicYearRepository);
    }

    @Test
    void createPersistsSemesterWithDefaultName() {
        AcademicYear year = AcademicYear.builder().id(1L).yearName("2026-2027").build();
        when(academicYearRepository.findById(1L)).thenReturn(Optional.of(year));
        when(semesterRepository.existsByAcademicYearIdAndSemesterNumber(1L, 7)).thenReturn(false);
        when(semesterRepository.save(any())).thenAnswer(inv -> {
            var s = (com.college.sms.entity.Semester) inv.getArgument(0);
            s.setId(13L);
            return s;
        });

        var response = service.create(new SemesterRequest(7, null, 1L));

        assertThat(response.id()).isEqualTo(13L);
        assertThat(response.semesterName()).isEqualTo("Semester 7");
        assertThat(response.academicYearName()).isEqualTo("2026-2027");
    }

    @Test
    void createDuplicateSemesterNumberThrows() {
        when(academicYearRepository.findById(1L)).thenReturn(Optional.of(
                AcademicYear.builder().id(1L).yearName("2026-2027").build()));
        when(semesterRepository.existsByAcademicYearIdAndSemesterNumber(1L, 1)).thenReturn(true);

        assertThatThrownBy(() -> service.create(new SemesterRequest(1, "Semester 1", 1L)))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void createWithMissingYearThrowsNotFound() {
        when(academicYearRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(new SemesterRequest(1, "Semester 1", 99L)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Academic year not found");
    }
}
