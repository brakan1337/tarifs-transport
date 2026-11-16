package io.github.brakan1337.tarifs.moteur;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/** Une grille tarifaire complète, telle qu'elle s'applique à une date donnée. */
public record Bareme(
        String nom,
        List<Tranche> tranches,
        BigDecimal minimumPerception,
        BigDecimal prixHayon,
        BigDecimal prixRendezVous,
        BigDecimal tauxTvaPct) {

    public Bareme {
        tranches = List.copyOf(tranches);
    }

    public Optional<Tranche> trancheFor(int zone, BigDecimal poidsKg) {
        return tranches.stream().filter(t -> t.contient(zone, poidsKg)).findFirst();
    }

    /** La tranche qui commence là où celle-ci s'arrête, dans la même zone. */
    public Optional<Tranche> trancheSuivante(Tranche tranche) {
        return tranches.stream()
                .filter(t -> t.zone() == tranche.zone() && t.poidsMinKg().compareTo(tranche.poidsMaxKg()) == 0)
                .findFirst();
    }
}
