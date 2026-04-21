package br.com.ctkd.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.NONE;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
@Entity(name = "addresses")
public class Address extends BaseEntity {

    @Column(name = "street_name", nullable = false)
    private String streetName;

    @Column(nullable = false)
    private String neighborhood;

    @Column(name = "zip_code", nullable = false)
    private String zipCode;

    @Column(name = "city_name", nullable = false)
    private String city;

    @Column(name = "state_name", nullable = false)
    private String state;

    @OneToMany(mappedBy = "address")
    @ToString.Exclude
    @Setter(NONE)
    private List<Occurrence> occurrences = new ArrayList<>();

    public List<Occurrence> getOccurrences() {
        return new ArrayList<>(occurrences);
    }

    public void addOccurrence(Occurrence... occurrences) {
        this.occurrences.addAll(List.of(occurrences));
    }
}
