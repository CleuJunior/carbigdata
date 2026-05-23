package br.com.ctkd.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
@Entity(name = "clients")
public class Client extends BaseEntity {

    @Column(name = "client_name", nullable = false)
    private String name;
    @Column(name = "birthdate", nullable = false)
    private LocalDate birthdate;
    @Column(name = "cpf_number", nullable = false)
    private String cpf;
}
