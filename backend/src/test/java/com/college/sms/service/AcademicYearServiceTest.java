package com.college.sms.service;

import com.college.sms.dto.AcademicYearRequest;
import com.college.sms.exception.DuplicateResourceException;
import com.college.sms.exception.ResourceNotFoundException;
import com.college.sms.repository.AcademicYearRepository;
import com.college.sms.repository.SemesterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcademicYearServiceTest {

    @Mock
    private AcademicYearRepository repository;
    @Mock
    private SemesterRepository semesterRepository;

    private AcademicYearService service;

    @BeforeEach
    void setUp() {
        service = new AcademicYearService(repository, semesterRepository);
    }

    private com.college.sms.entity.AcademicYear year(long id, String name, boolean active) {
        return com.college.sms.entity.AcademicYear.builder()
                .id(id).yearName(name).startDate(LocalDate.of(2026, 6, 1))
                .endDate(LocalDate.of(2027, 4, 30)).active(active).build();
    }

    @Test
    void createPersistsYear() {
        when(repository.existsByYearName("2028-2029")).thenReturn(false);
        when(repository.save(any())).thenAnswer(inv -> {
            var y = (com.college.sms.entity.AcademicYear) inv.getArgument(0);
            y.setId(3L);
            return y;
        });

        var response = service.create(new AcademicYearRequest("2028-2029",
                LocalDate.of(2028, 6, 1), LocalDate.of(2029, 4, 30), false));

        assertThat(response.id()).isEqualTo(3L);
        assertThat(response.yearName()).isEqualTo("2028-2029");
    }

    @Test
    void createDuplicateYearThrows() {
        when(repository.existsByYearName("2026-2027")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new AcademicYearRequest("2026-2027",
                LocalDate.of(2026, 6, 1), LocalDate.of(2027, 4, 30), false)))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void activateDeactivatesAllOthers() {
        when(repository.findAll()).thenReturn(List.of(year(1L, "2026-2027", true), year(2L, "2027-2028", false)));
        when(repository.findById(2L)).thenReturn(Optional.of(year(2L, "2027-2028", false)));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var response = service.setActive(2L);

        assertThat(response.active()).isTrue();
        verify(repository).save(org.mockito.Mockito.argThat(y -> y.getId().equals(1L) && !y.isActive()));
    }

    @Test
    void deleteYearWithSemestersIsRefused() {
        when(repository.findById(1L)).thenReturn(Optional.of(year(1L, "2026-2027", true)));
        when(semesterRepository.existsByAcademicYearId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(com.college.sms.exception.BadRequestException.class)
                .hasMessageContaining("semesters reference");
    }

    @Test
    void findActiveWhenNoneConfiguredThrows() {
        when(repository.findByActiveTrue()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findActive())
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
