package br.com.ctkd.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
@Entity(name = "occurrences")
public class Occurrence extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    @ToString.Exclude
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", nullable = false)
    @ToString.Exclude
    private Address address;

    @Column(name = "occurrence_date", nullable = false)
    private LocalDate occurrenceDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusOccurrence status;

    @OneToMany(mappedBy = "occurrence", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<PhotoOccurrence> photos = new ArrayList<>();
}
