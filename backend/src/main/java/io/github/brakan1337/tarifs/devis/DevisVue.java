package io.github.brakan1337.tarifs.devis;

import io.github.brakan1337.tarifs.donnees.Devis;
import io.github.brakan1337.tarifs.donnees.Grille;
import io.github.brakan1337.tarifs.donnees.ZoneDepartement;
import io.github.brakan1337.tarifs.moteur.Calcul;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Un devis tel que l'API le renvoie. Une simulation n'a ni numéro ni date de création. */
public record DevisVue(
        String numero,
        LocalDateTime creeLe,
        String departement,
        String nomDepartement,
        int zone,
        String grille,
        BigDecimal poidsKg,
        BigDecimal volumeM3,
        BigDecimal poidsTaxableKg,
        LocalDate dateEnlevement,
        boolean hayon,
        boolean rendezVous,
        BigDecimal tauxGazole,
        List<Ligne> lignes,
        BigDecimal totalHt,
        BigDecimal tva,
        BigDecimal totalTtc) {

    public record Ligne(String libelle, BigDecimal montant) {
    }

    static DevisVue simulation(DemandeDevis demande, ZoneDepartement zone, Grille grille,
            BigDecimal tauxGazole, Calcul calcul) {
        return new DevisVue(
                null, null,
                zone.getDepartement(), zone.getNom(), zone.getZone(), grille.getNom(),
                demande.poidsKg(), demande.volumeM3(), calcul.poidsTaxableKg(), demande.dateEnlevement(),
                demande.hayon(), demande.rendezVous(), tauxGazole,
                calcul.lignes().stream().map(l -> new Ligne(l.libelle(), l.montant())).toList(),
                calcul.totalHt(), calcul.tva(), calcul.totalTtc());
    }

    static DevisVue de(Devis devis) {
        return new DevisVue(
                devis.getNumero(), devis.getCreeLe(),
                devis.getDepartement().getDepartement(), devis.getDepartement().getNom(), devis.getZone(),
                devis.getGrille().getNom(),
                devis.getPoidsKg(), devis.getVolumeM3(), devis.getPoidsTaxable(), devis.getDateEnlevement(),
                devis.isHayon(), devis.isRendezVous(), devis.getTauxGazole(),
                devis.getLignes().stream().map(l -> new Ligne(l.getLibelle(), l.getMontant())).toList(),
                devis.getTotalHt(), devis.getTva(), devis.getTotalTtc());
    }
}
