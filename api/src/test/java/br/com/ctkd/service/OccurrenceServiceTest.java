package br.com.ctkd.service;

import br.com.ctkd.domain.Occurrence;
import br.com.ctkd.domain.StatusOccurrence;
import br.com.ctkd.dto.request.OccurrenceQueryRequest;
import br.com.ctkd.exceptions.NotFoundException;
import br.com.ctkd.exceptions.OccurrenceStatusException;
import br.com.ctkd.repository.OccurrenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class OccurrenceServiceTest {

    @Mock
    private OccurrenceRepository repository;
    @InjectMocks
    private OccurrenceService underTest;

    private Occurrence occurrence;

    @BeforeEach
    void setup() {
        occurrence = new Occurrence();
        occurrence.setOccurrenceDate(LocalDate.of(2024, 3, 10));
        occurrence.setStatus(StatusOccurrence.ACTIVE);
    }

    @Test
    void shouldGetOccurrenceById() {
        var uuid = UUID.randomUUID();

        given(repository.findById(uuid)).willReturn(Optional.of(occurrence));

        var result = underTest.getOccurrenceById(uuid.toString());

        then(result.getOccurrenceDate()).isEqualTo(LocalDate.of(2024, 3, 10));
        then(result.getStatus()).isEqualTo(StatusOccurrence.ACTIVE);

        verify(repository).findById(uuid);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void shouldThrowWhenOccurrenceNotFound() {
        var uuid = UUID.randomUUID();

        given(repository.findById(uuid)).willReturn(Optional.empty());

        assertThatThrownBy(() -> underTest.getOccurrenceById(uuid.toString()))
                .isInstanceOf(NotFoundException.class);

        verify(repository).findById(uuid);
    }

    @Test
    void shouldGetAllOccurrences() {
        given(repository.findAll()).willReturn(List.of(occurrence));

        var result = underTest.getAllOccurrence();

        then(result).hasSize(1);
        then(result.getFirst().getOccurrenceDate()).isEqualTo(LocalDate.of(2024, 3, 10));

        verify(repository).findAll();
        verifyNoMoreInteractions(repository);
    }

    @Test
    void shouldPageOccurrences() {
        var pageable = PageRequest.of(0, 10);
        var page = new PageImpl<>(List.of(occurrence), pageable, 1);

        given(repository.findAll(pageable)).willReturn(page);

        var result = underTest.pageOccurrences(0, 10);

        then(result.getContent()).hasSize(1);
        then(result.getTotalElements()).isEqualTo(1);
        then(result.getNumber()).isEqualTo(0);
        then(result.getSize()).isEqualTo(10);

        verify(repository).findAll(pageable);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void shouldInsertOccurrence() {
        given(repository.save(occurrence)).willReturn(occurrence);

        var result = underTest.insertOccurrence(occurrence);

        then(result.getOccurrenceDate()).isEqualTo(LocalDate.of(2024, 3, 10));

        verify(repository).save(occurrence);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void shouldCloseOccurrence() {
        var uuid = UUID.randomUUID();
        var captor = ArgumentCaptor.forClass(Occurrence.class);

        given(repository.findById(uuid)).willReturn(Optional.of(occurrence));
        given(repository.save(any(Occurrence.class))).willReturn(occurrence);

        underTest.closeOccurrence(uuid.toString());

        verify(repository).save(captor.capture());

        then(captor.getValue().getStatus()).isEqualTo(StatusOccurrence.FINISHED);
    }

    @Test
    void shouldThrowWhenClosingAlreadyFinishedOccurrence() {
        var uuid = UUID.randomUUID();
        occurrence.setStatus(StatusOccurrence.FINISHED);

        given(repository.findById(uuid)).willReturn(Optional.of(occurrence));

        assertThatThrownBy(() -> underTest.closeOccurrence(uuid.toString()))
                .isInstanceOf(OccurrenceStatusException.class);
    }

    @Test
    void shouldSearchOccurrences() {
        var request = OccurrenceQueryRequest.builder()
                .clientName("João")
                .city("São Paulo")
                .sortBy("occurrenceDate")
                .sortDirection("ASC")
                .page(0)
                .size(10)
                .build();

        var pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "occurrenceDate"));
        var page = new PageImpl<>(List.of(occurrence), pageable, 1);

        given(repository.findAll(any(Specification.class), eq(pageable))).willReturn(page);

        var result = underTest.search(request);

        then(result.getContent()).hasSize(1);
        then(result.getTotalElements()).isEqualTo(1);

        verify(repository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void shouldSearchWithDefaultPaginationWhenNotProvided() {
        var request = OccurrenceQueryRequest.builder()
                .clientName("Maria")
                .sortDirection("DESC")
                .build();

        var pageable = PageRequest.of(0, 15, Sort.by(Sort.Direction.DESC, "occurrenceDate"));
        var page = new PageImpl<>(List.of(occurrence), pageable, 1);

        given(repository.findAll(any(Specification.class), eq(pageable))).willReturn(page);

        var result = underTest.search(request);

        then(result.getContent()).hasSize(1);

        verify(repository).findAll(any(Specification.class), eq(pageable));
    }
}
