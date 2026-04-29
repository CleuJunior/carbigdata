package br.com.ctkd.service;

import br.com.ctkd.domain.Occurrence;
import br.com.ctkd.domain.StatusOccurrence;
import br.com.ctkd.dto.request.OccurrenceQueryRequest;
import br.com.ctkd.exceptions.NotFoundException;
import br.com.ctkd.exceptions.OccurrenceStatusException;
import br.com.ctkd.repository.OccurrenceRepository;
import br.com.ctkd.repository.specification.OccurrenceSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OccurrenceService {

    private static final String DEFAULT_SORT_FIELD = "occurrenceDate";

    private final OccurrenceRepository repository;

    public Occurrence getOccurrenceById(String id) {
        var uuid = UUID.fromString(id);

        return repository.findById(uuid)
                .orElseThrow(() -> new NotFoundException("occurrence.not.found", id));
    }

    public List<Occurrence> getAllOccurrence() {
        return repository.findAll();
    }

    public Page<Occurrence> pageOccurrences(int page, int size) {
        return repository.findAll(PageRequest.of(page, size));
    }

    @Transactional
    public Occurrence insertOccurrence(Occurrence occurrence) {
        return repository.save(occurrence);
    }

    public Occurrence closeOccurrence(String id) {
        var occurrence = getOccurrenceById(id);

        if (occurrence.getStatus() == StatusOccurrence.FINISHED) {
            throw new OccurrenceStatusException("occurrence.status.conflict");
        }

        occurrence.setStatus(StatusOccurrence.FINISHED);

        return repository.save(occurrence);
    }

    public Page<Occurrence> search(OccurrenceQueryRequest request) {
        var direction = "ASC".equalsIgnoreCase(request.sortDirection())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        var sortField = request.sortBy() != null ? request.sortBy() : DEFAULT_SORT_FIELD;
        var sort = Sort.by(direction, sortField);

        var pageNum = request.page() != null ? request.page() : 0;
        var pageSize = request.size() != null ? request.size() : 15;
        var pageable = PageRequest.of(pageNum, pageSize, sort);

        var spec = OccurrenceSpecification.withFilters(
                request.clientName(),
                request.cpf(),
                request.occurrenceDate(),
                request.city()
        );

        return repository.findAll(spec, pageable);
    }
}
