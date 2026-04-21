package br.com.ctkd.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
@Entity(name = "photos_occurrence")
public class PhotoOccurrence extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cod_ocorrencia", nullable = false)
    @ToString.Exclude
    private Occurrence occurrence;

    @Column(name = "dsc_path_bucket", nullable = false)
    private String pathBucket;

    @Column(name = "dsc_hash", nullable = false)
    private String hash;
}
