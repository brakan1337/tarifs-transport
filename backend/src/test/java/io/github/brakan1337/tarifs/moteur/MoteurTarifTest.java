package io.github.brakan1337.tarifs.moteur;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MoteurTarifTest {

    private static BigDecimal d(String valeur) {
        return new BigDecimal(valeur);
    }

    /** Une petite grille lisible, sur une seule zone. */
    private static final Bareme BAREME = new Bareme(
            "Grille de test",
            List.of(
                    new Tranche(1, d("0"), d("10"), ModeTarif.FORFAIT, d("12.50")),
                    new Tranche(1, d("10"), d("30"), ModeTarif.FORFAIT, d("18.00")),
                    new Tranche(1, d("30"), d("100"), ModeTarif.FORFAIT, d("35.00")),
                    new Tranche(1, d("100"), d("1000"), ModeTarif.AU_100KG, d("36.00")),
                    new Tranche(1, d("1000"), d("3000"), ModeTarif.AU_100KG, d("30.00"))),
            d("15.00"),
            d("25.00"),
            d("12.00"),
            d("20"));

    private final MoteurTarif moteur = new MoteurTarif();

    private Calcul calculer(String poids, String volume, boolean hayon, boolean rdv, String gazole) {
        return moteur.calculer(BAREME, new Expedition(1, d(poids), d(volume), hayon, rdv), d(gazole));
    }

    @Nested
    @DisplayName("Poids taxable")
    class PoidsTaxable {

        @Test
        void le_poids_reel_compte_quand_le_colis_est_dense() {
            assertThat(MoteurTarif.poidsTaxable(d("80"), d("0.1"))).isEqualByComparingTo("80");
        }

        @Test
        void un_colis_encombrant_paie_son_volume_a_250_kg_le_m3() {
            assertThat(MoteurTarif.poidsTaxable(d("50"), d("0.6"))).isEqualByComparingTo("150");
        }

        @Test
        void le_poids_est_arrondi_au_kilo_superieur() {
            assertThat(MoteurTarif.poidsTaxable(d("10.2"), d("0"))).isEqualByComparingTo("11");
        }
    }

    @Nested
    @DisplayName("Choix de la tranche")
    class ChoixTranche {

        @ParameterizedTest(name = "{0} kg tombe dans la tranche qui finit à {1} kg")
        @CsvSource({"10, 10", "10.2, 30", "30, 30", "31, 100", "100, 100"})
        void la_borne_haute_est_incluse(String poids, String finDeTranche) {
            Calcul calcul = calculer(poids, "0", false, false, "0");
            assertThat(calcul.tranche().poidsMaxKg()).isEqualByComparingTo(finDeTranche);
        }

        @Test
        void au_dela_de_100_kg_le_prix_est_proportionnel_au_poids() {
            Calcul calcul = calculer("250", "0", false, false, "0");
            // 36 € les 100 kg × 2,5
            assertThat(calcul.lignes().getFirst().montant()).isEqualByComparingTo("90.00");
        }

        @Test
        void le_poids_de_bascule_evite_qu_un_envoi_plus_leger_coute_plus_cher() {
            // 900 kg à 36 € = 324 € ; 1000 kg dans la tranche suivante à 30 € = 300 €
            Calcul calcul = calculer("900", "0", false, false, "0");
            assertThat(calcul.lignes().getFirst().montant()).isEqualByComparingTo("300.00");
            assertThat(calcul.lignes().getFirst().libelle()).contains("poids de bascule 1000 kg");
        }

        @Test
        void le_poids_de_bascule_ne_s_applique_pas_quand_il_coute_plus_cher() {
            Calcul calcul = calculer("500", "0", false, false, "0");
            assertThat(calcul.lignes().getFirst().montant()).isEqualByComparingTo("180.00");
            assertThat(calcul.lignes().getFirst().libelle()).doesNotContain("bascule");
        }

        @Test
        void un_poids_hors_grille_est_refuse() {
            assertThatThrownBy(() -> calculer("3200", "0", false, false, "0"))
                    .isInstanceOf(PoidsHorsGrilleException.class)
                    .hasMessageContaining("3200 kg en zone 1");
        }

        @Test
        void une_zone_absente_de_la_grille_est_refusee() {
            Expedition ailleurs = new Expedition(4, d("20"), d("0"), false, false);
            assertThatThrownBy(() -> moteur.calculer(BAREME, ailleurs, d("0")))
                    .isInstanceOf(PoidsHorsGrilleException.class);
        }
    }

    @Nested
    @DisplayName("Montants")
    class Montants {

        @Test
        void le_minimum_de_perception_remplace_un_prix_trop_bas() {
            Calcul calcul = calculer("5", "0", false, false, "0");
            assertThat(calcul.lignes().getFirst().libelle()).contains("minimum de perception");
            assertThat(calcul.lignes().getFirst().montant()).isEqualByComparingTo("15.00");
        }

        @Test
        void la_surcharge_gazole_ne_porte_que_sur_le_transport() {
            Calcul calcul = calculer("250", "0", true, true, "18.5");
            // 90,00 × 18,5 % = 16,65 ; hayon et rendez-vous ne sont pas surchargés
            assertThat(calcul.lignes()).extracting(LigneCalcul::montant)
                    .usingElementComparator(BigDecimal::compareTo)
                    .containsExactly(d("90.00"), d("16.65"), d("25.00"), d("12.00"));
        }

        @Test
        void sans_surcharge_gazole_la_ligne_n_apparait_pas() {
            Calcul calcul = calculer("20", "0", false, false, "0");
            assertThat(calcul.lignes()).hasSize(1);
        }

        @Test
        void le_total_est_la_somme_des_lignes_arrondies() {
            Calcul calcul = calculer("250", "0", true, true, "18.5");
            assertThat(calcul.totalHt()).isEqualByComparingTo("143.65");
            assertThat(calcul.tva()).isEqualByComparingTo("28.73");
            assertThat(calcul.totalTtc()).isEqualByComparingTo("172.38");
        }

        @Test
        void chaque_montant_a_exactement_deux_decimales() {
            Calcul calcul = calculer("137", "0", false, false, "17.33");
            assertThat(calcul.lignes()).allSatisfy(ligne -> assertThat(ligne.montant().scale()).isEqualTo(2));
            assertThat(calcul.totalTtc().scale()).isEqualTo(2);
        }
    }
}
