package io.github.brakan1337.tarifs.devis;

import io.github.brakan1337.tarifs.donnees.Devis;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Une ligne de l'historique des devis. */
public record DevisResume(
        String numero,
        LocalDateTime creeLe,
        String departement,
        String nomDepartement,
        BigDecimal poidsKg,
        LocalDate dateEnlevement,
        BigDecimal totalTtc) {

    static DevisResume de(Devis devis) {
        return new DevisResume(
                devis.getNumero(), devis.getCreeLe(),
                devis.getDepartement().getDepartement(), devis.getDepartement().getNom(),
                devis.getPoidsKg(), devis.getDateEnlevement(), devis.getTotalTtc());
    }
}
