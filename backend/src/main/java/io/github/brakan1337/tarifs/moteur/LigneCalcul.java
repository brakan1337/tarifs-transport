package io.github.brakan1337.tarifs.moteur;

import java.math.BigDecimal;

/** Une ligne du devis, déjà arrondie au centime. */
public record LigneCalcul(String libelle, BigDecimal montant) {
}
