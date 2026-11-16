package io.github.brakan1337.tarifs.moteur;

import java.math.BigDecimal;

/** Aucune tranche ne couvre ce poids : au-delà, le transporteur fait un devis d'affrètement à la main. */
public class PoidsHorsGrilleException extends RuntimeException {

    public PoidsHorsGrilleException(int zone, BigDecimal poidsKg) {
        super("Aucune tranche de la grille ne couvre " + poidsKg.toPlainString()
                + " kg en zone " + zone + " : devis d'affrètement sur demande.");
    }
}
