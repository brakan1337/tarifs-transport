package io.github.brakan1337.tarifs.moteur;

import java.math.BigDecimal;

/** Ce que le client veut envoyer. */
public record Expedition(int zone, BigDecimal poidsKg, BigDecimal volumeM3, boolean hayon, boolean rendezVous) {
}
