package io.github.brakan1337.tarifs.moteur;

import java.math.BigDecimal;

/**
 * Une ligne de la grille : une zone et une tranche de poids.
 * La borne basse est exclue et la borne haute incluse : 10 kg tombe dans « 0 à 10 kg ».
 */
public record Tranche(int zone, BigDecimal poidsMinKg, BigDecimal poidsMaxKg, ModeTarif mode, BigDecimal prix) {

    private static final BigDecimal CENT_KG = new BigDecimal("100");

    public boolean contient(int zoneDemandee, BigDecimal poidsKg) {
        return zone == zoneDemandee
                && poidsKg.compareTo(poidsMinKg) > 0
                && poidsKg.compareTo(poidsMaxKg) <= 0;
    }

    /** Prix du transport avant minimum de perception, sans arrondi. */
    public BigDecimal prixPour(BigDecimal poidsTaxableKg) {
        return switch (mode) {
            case FORFAIT -> prix;
            case AU_100KG -> prix.multiply(poidsTaxableKg).divide(CENT_KG);
        };
    }

    public String libelle() {
        return "zone " + zone + ", " + poidsMinKg.stripTrailingZeros().toPlainString()
                + " à " + poidsMaxKg.stripTrailingZeros().toPlainString() + " kg";
    }
}
