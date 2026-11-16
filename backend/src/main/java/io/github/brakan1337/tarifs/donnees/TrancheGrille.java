package io.github.brakan1337.tarifs.donnees;

import io.github.brakan1337.tarifs.moteur.ModeTarif;
import io.github.brakan1337.tarifs.moteur.Tranche;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "tranche")
public class TrancheGrille {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Grille grille;

    private int zone;
    private BigDecimal poidsMin;
    private BigDecimal poidsMax;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private ModeTarif mode;

    private BigDecimal prix;

    protected TrancheGrille() {
    }

    Tranche versTranche() {
        return new Tranche(zone, poidsMin, poidsMax, mode, prix);
    }

    public int getZone() {
        return zone;
    }

    public BigDecimal getPoidsMin() {
        return poidsMin;
    }

    public BigDecimal getPoidsMax() {
        return poidsMax;
    }

    public ModeTarif getMode() {
        return mode;
    }

    public BigDecimal getPrix() {
        return prix;
    }
}
