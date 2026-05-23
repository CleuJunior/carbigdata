package br.com.ctkd.repository.specification;

import br.com.ctkd.domain.Occurrence;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OccurrenceSpecification {

    public static Specification<Occurrence> withFilters(String clientName,
                                                        String cpf,
                                                        LocalDate occurrenceDate,
                                                        String city) {

        return Specification
                .where(notDeleted())
                .and(byName(clientName))
                .and(byCpf(cpf))
                .and(byOccurrenceDate(occurrenceDate))
                .and(byCity(city));
    }

    private static Specification<Occurrence> notDeleted() {
        return (root, query, cb) -> cb.equal(root.get("deleted"), false);
    }

    private static Specification<Occurrence> byName(String clientName) {
        return (root, query, cb) -> {
            if (clientName == null || clientName.trim().isEmpty()) {
                return null;
            }
            return cb.like(
                    cb.lower(root.get("client").get("name")),
                    "%" + clientName.toLowerCase().trim() + "%"
            );
        };
    }

    private static Specification<Occurrence> byCpf(String cpf) {
        return (root, query, cb) ->
                (cpf == null || cpf.trim().isEmpty()) ? null
                        : cb.equal(root.get("client").get("cpf"), cpf.trim());
    }

    private static Specification<Occurrence> byOccurrenceDate(LocalDate date) {
        return (root, query, cb) ->
                date == null ? null : cb.equal(root.get("occurrenceDate"), date);
    }

    private static Specification<Occurrence> byCity(String city) {
        return (root, query, cb) -> {
            if (city == null || city.trim().isEmpty()) return null;
            return cb.like(
                    cb.lower(root.get("address").get("city")),
                    "%" + city.toLowerCase().trim() + "%"
            );
        };
    }

}
