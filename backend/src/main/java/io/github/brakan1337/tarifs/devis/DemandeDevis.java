package io.github.brakan1337.tarifs.devis;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Ce que le commercial saisit pour obtenir un prix. */
public record DemandeDevis(
        @NotBlank(message = "Le département d'arrivée est obligatoire.")
        @Pattern(regexp = "\\d{2}|2[AB]", message = "Département attendu sur deux caractères, par exemple 33 ou 2A.")
        String departement,

        @NotNull(message = "Le poids est obligatoire.")
        @DecimalMin(value = "0.1", message = "Le poids doit dépasser 0,1 kg.")
        @DecimalMax(value = "3000", message = "Au-delà de 3 000 kg, c'est un affrètement : devis sur demande.")
        @Digits(integer = 4, fraction = 2, message = "Le poids accepte deux décimales au plus.")
        BigDecimal poidsKg,

        @NotNull(message = "Le volume est obligatoire, 0 s'il est inconnu.")
        @DecimalMin(value = "0", message = "Le volume ne peut pas être négatif.")
        @DecimalMax(value = "30", message = "Au-delà de 30 m³, c'est un camion complet : devis sur demande.")
        @Digits(integer = 2, fraction = 3, message = "Le volume accepte trois décimales au plus.")
        BigDecimal volumeM3,

        @NotNull(message = "La date d'enlèvement est obligatoire.")
        LocalDate dateEnlevement,

        boolean hayon,
        boolean rendezVous) {
}
