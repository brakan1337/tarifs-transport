package io.github.brakan1337.tarifs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.brakan1337.tarifs.donnees.DevisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/** L'application complète, avec la base H2 et les données de démonstration chargées par Flyway. */
@SpringBootTest
class DevisApiTest {

    @Autowired
    private WebApplicationContext contexte;

    @Autowired
    private DevisRepository devisRepository;

    private MockMvc mvc;

    @BeforeEach
    void preparer() {
        mvc = MockMvcBuilders.webAppContextSetup(contexte).build();
    }

    private ResultActions envoyer(String url, String json) throws Exception {
        return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private static String demande(String departement, String poids, String volume, String date, boolean hayon) {
        return """
                {"departement": "%s", "poidsKg": %s, "volumeM3": %s, "dateEnlevement": "%s",
                 "hayon": %s, "rendezVous": false}
                """.formatted(departement, poids, volume, date, hayon);
    }

    @Test
    void un_devis_complet_vers_la_gironde() throws Exception {
        // Zone 1, grille 2026 : 250 kg à 35,40 € les 100 kg = 88,50 €
        // gazole de novembre 2026 à 21 % = 18,585 → 18,59 € ; hayon 28 €
        envoyer("/api/devis", demande("33", "250", "0.5", "2026-11-20", true))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/devis/DV-")))
                .andExpect(jsonPath("$.numero", matchesPattern("DV-\\d{4}-\\d{5}")))
                .andExpect(jsonPath("$.zone").value(1))
                .andExpect(jsonPath("$.grille").value("Grille 2026"))
                .andExpect(jsonPath("$.poidsTaxableKg").value(250))
                .andExpect(jsonPath("$.lignes", hasSize(3)))
                .andExpect(jsonPath("$.lignes[0].montant").value(88.50))
                .andExpect(jsonPath("$.lignes[1].montant").value(18.59))
                .andExpect(jsonPath("$.lignes[2].montant").value(28.00))
                .andExpect(jsonPath("$.totalHt").value(135.09))
                .andExpect(jsonPath("$.tva").value(27.02))
                .andExpect(jsonPath("$.totalTtc").value(162.11));
    }

    @Test
    void un_enlevement_en_2027_prend_la_grille_2027() throws Exception {
        envoyer("/api/devis/simulation", demande("33", "250", "0", "2027-02-01", false))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grille").value("Grille 2027 (+3,5 %)"))
                // le taux de février 2027 n'est pas publié : on reprend le dernier connu
                .andExpect(jsonPath("$.tauxGazole").value(21.00));
    }

    @Test
    void une_simulation_n_enregistre_rien() throws Exception {
        long avant = devisRepository.count();
        envoyer("/api/devis/simulation", demande("75", "12", "0", "2026-11-20", false))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numero").doesNotExist())
                .andExpect(jsonPath("$.zone").value(3));
        assertThat(devisRepository.count()).isEqualTo(avant);
    }

    @Test
    void un_devis_enregistre_se_relit_par_son_numero() throws Exception {
        String reponse = envoyer("/api/devis", demande("64", "40", "0", "2026-11-20", false))
                .andReturn().getResponse().getContentAsString();
        String numero = reponse.replaceAll(".*\"numero\":\"([^\"]+)\".*", "$1");

        mvc.perform(get("/api/devis/" + numero))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numero").value(numero))
                .andExpect(jsonPath("$.nomDepartement").value("Pyrénées-Atlantiques"));
    }

    @Test
    void un_numero_inconnu_renvoie_404() throws Exception {
        mvc.perform(get("/api/devis/DV-1999-00001"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Introuvable"));
    }

    @Test
    void les_champs_invalides_sont_detailles_un_par_un() throws Exception {
        envoyer("/api/devis/simulation", demande("333", "-4", "0", "2026-11-20", false))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erreurs.departement").exists())
                .andExpect(jsonPath("$.erreurs.poidsKg").value("Le poids doit dépasser 0,1 kg."));
    }

    @Test
    void un_departement_non_desservi_est_refuse() throws Exception {
        envoyer("/api/devis/simulation", demande("20", "10", "0", "2026-11-20", false))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("Le département 20 n'est pas desservi."));
    }

    @Test
    void un_colis_trop_volumineux_sort_de_la_grille() throws Exception {
        // 20 m³ × 250 kg = 5 000 kg taxables, au-delà de la dernière tranche
        envoyer("/api/devis/simulation", demande("33", "100", "20", "2026-11-20", false))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.title").value("Devis impossible"));
    }

    @Test
    void une_date_sans_grille_est_refusee() throws Exception {
        envoyer("/api/devis/simulation", demande("33", "10", "0", "2025-06-01", false))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("Aucune grille tarifaire ne couvre le 01/06/2025."));
    }

    @Test
    void la_grille_en_vigueur_couvre_quatre_zones_et_sept_tranches() throws Exception {
        mvc.perform(get("/api/grilles/en-vigueur").param("date", "2026-06-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Grille 2026"))
                .andExpect(jsonPath("$.tranches", hasSize(28)));
    }

    @Test
    void les_96_departements_sont_charges() throws Exception {
        mvc.perform(get("/api/departements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(96)));
    }
}
