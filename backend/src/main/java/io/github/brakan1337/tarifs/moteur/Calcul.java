package io.github.brakan1337.tarifs.moteur;

import java.math.BigDecimal;
import java.util.List;

/** Le résultat détaillé : chaque ligne, puis les totaux. */
public record Calcul(
        BigDecimal poidsTaxableKg,
        Tranche tranche,
        List<LigneCalcul> lignes,
        BigDecimal totalHt,
        BigDecimal tva,
        BigDecimal totalTtc) {

    public Calcul {
        lignes = List.copyOf(lignes);
    }
}
