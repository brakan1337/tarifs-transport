package io.github.brakan1337.tarifs.donnees;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "indice_gazole")
public class IndiceGazole {

    @Id
    private LocalDate mois;
    private BigDecimal taux;

    protected IndiceGazole() {
    }

    public LocalDate getMois() {
        return mois;
    }

    public BigDecimal getTaux() {
        return taux;
    }
}
