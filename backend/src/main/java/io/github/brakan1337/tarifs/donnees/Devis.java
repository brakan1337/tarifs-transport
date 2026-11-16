package io.github.brakan1337.tarifs.donnees;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Un devis enregistré. Ses montants sont figés : une nouvelle grille ne le modifie pas. */
@Entity
@Table(name = "devis")
public class Devis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String numero;
    private LocalDateTime creeLe;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "departement")
    private ZoneDepartement departement;

    private BigDecimal poidsKg;
    private BigDecimal volumeM3;
    private LocalDate dateEnlevement;
    private boolean hayon;
    private boolean rendezVous;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Grille grille;

    private int zone;
    private BigDecimal poidsTaxable;
    private BigDecimal tauxGazole;
    private BigDecimal totalHt;
    private BigDecimal tva;
    private BigDecimal totalTtc;

    @OneToMany(mappedBy = "devis", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordre")
    private List<LigneDevis> lignes = new ArrayList<>();

    protected Devis() {
    }

    public Devis(LocalDateTime creeLe, ZoneDepartement departement, BigDecimal poidsKg, BigDecimal volumeM3,
            LocalDate dateEnlevement, boolean hayon, boolean rendezVous, Grille grille, int zone,
            BigDecimal poidsTaxable, BigDecimal tauxGazole, BigDecimal totalHt, BigDecimal tva, BigDecimal totalTtc) {
        this.creeLe = creeLe;
        this.departement = departement;
        this.poidsKg = poidsKg;
        this.volumeM3 = volumeM3;
        this.dateEnlevement = dateEnlevement;
        this.hayon = hayon;
        this.rendezVous = rendezVous;
        this.grille = grille;
        this.zone = zone;
        this.poidsTaxable = poidsTaxable;
        this.tauxGazole = tauxGazole;
        this.totalHt = totalHt;
        this.tva = tva;
        this.totalTtc = totalTtc;
    }

    public void ajouterLigne(String libelle, BigDecimal montant) {
        lignes.add(new LigneDevis(this, lignes.size() + 1, libelle, montant));
    }

    /** Le numéro dépend de l'identifiant : il est posé juste après le premier enregistrement. */
    public void numeroter() {
        this.numero = "DV-%d-%05d".formatted(creeLe.getYear(), id);
    }

    public Long getId() {
        return id;
    }

    public String getNumero() {
        return numero;
    }

    public LocalDateTime getCreeLe() {
        return creeLe;
    }

    public ZoneDepartement getDepartement() {
        return departement;
    }

    public BigDecimal getPoidsKg() {
        return poidsKg;
    }

    public BigDecimal getVolumeM3() {
        return volumeM3;
    }

    public LocalDate getDateEnlevement() {
        return dateEnlevement;
    }

    public boolean isHayon() {
        return hayon;
    }

    public boolean isRendezVous() {
        return rendezVous;
    }

    public Grille getGrille() {
        return grille;
    }

    public int getZone() {
        return zone;
    }

    public BigDecimal getPoidsTaxable() {
        return poidsTaxable;
    }

    public BigDecimal getTauxGazole() {
        return tauxGazole;
    }

    public BigDecimal getTotalHt() {
        return totalHt;
    }

    public BigDecimal getTva() {
        return tva;
    }

    public BigDecimal getTotalTtc() {
        return totalTtc;
    }

    public List<LigneDevis> getLignes() {
        return lignes;
    }
}
