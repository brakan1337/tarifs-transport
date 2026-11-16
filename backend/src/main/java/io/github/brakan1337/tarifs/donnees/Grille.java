package io.github.brakan1337.tarifs.donnees;

import io.github.brakan1337.tarifs.moteur.Bareme;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "grille")
public class Grille {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nom;
    private LocalDate valideDu;
    private LocalDate valideAu;
    private BigDecimal minimumPerception;
    private BigDecimal prixHayon;
    private BigDecimal prixRendezVous;
    private BigDecimal tauxTva;

    @OneToMany(mappedBy = "grille")
    @OrderBy("zone, poidsMin")
    private List<TrancheGrille> tranches = new ArrayList<>();

    protected Grille() {
    }

    /** Traduit la grille stockée en barème que le moteur sait lire. */
    public Bareme versBareme() {
        return new Bareme(
                nom,
                tranches.stream().map(TrancheGrille::versTranche).toList(),
                minimumPerception,
                prixHayon,
                prixRendezVous,
                tauxTva);
    }

    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public LocalDate getValideDu() {
        return valideDu;
    }

    public LocalDate getValideAu() {
        return valideAu;
    }

    public BigDecimal getMinimumPerception() {
        return minimumPerception;
    }

    public BigDecimal getPrixHayon() {
        return prixHayon;
    }

    public BigDecimal getPrixRendezVous() {
        return prixRendezVous;
    }

    public BigDecimal getTauxTva() {
        return tauxTva;
    }

    public List<TrancheGrille> getTranches() {
        return tranches;
    }
}
