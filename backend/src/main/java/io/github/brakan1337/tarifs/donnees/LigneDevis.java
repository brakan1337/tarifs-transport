package io.github.brakan1337.tarifs.donnees;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "devis_ligne")
public class LigneDevis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Devis devis;

    private int ordre;
    private String libelle;
    private BigDecimal montant;

    protected LigneDevis() {
    }

    LigneDevis(Devis devis, int ordre, String libelle, BigDecimal montant) {
        this.devis = devis;
        this.ordre = ordre;
        this.libelle = libelle;
        this.montant = montant;
    }

    public String getLibelle() {
        return libelle;
    }

    public BigDecimal getMontant() {
        return montant;
    }
}
