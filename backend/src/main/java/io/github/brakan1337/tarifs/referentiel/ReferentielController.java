package io.github.brakan1337.tarifs.referentiel;

import io.github.brakan1337.tarifs.donnees.Grille;
import io.github.brakan1337.tarifs.donnees.GrilleRepository;
import io.github.brakan1337.tarifs.donnees.IndiceGazoleRepository;
import io.github.brakan1337.tarifs.donnees.ZoneDepartementRepository;
import io.github.brakan1337.tarifs.moteur.ModeTarif;
import io.github.brakan1337.tarifs.web.IntrouvableException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Les données de référence en lecture seule : départements, grilles, taux gazole. */
@RestController
@RequestMapping("/api")
class ReferentielController {

    record DepartementVue(String code, String nom, int zone) {
    }

    record GrilleResume(Long id, String nom, LocalDate valideDu, LocalDate valideAu) {
    }

    record TrancheVue(int zone, BigDecimal poidsMin, BigDecimal poidsMax, ModeTarif mode, BigDecimal prix) {
    }

    record GrilleVue(String nom, LocalDate valideDu, LocalDate valideAu, BigDecimal minimumPerception,
            BigDecimal prixHayon, BigDecimal prixRendezVous, BigDecimal tauxTva, List<TrancheVue> tranches) {
    }

    record IndiceVue(LocalDate mois, BigDecimal taux) {
    }

    private final ZoneDepartementRepository zones;
    private final GrilleRepository grilles;
    private final IndiceGazoleRepository indices;
    private final Clock horloge;

    ReferentielController(ZoneDepartementRepository zones, GrilleRepository grilles,
            IndiceGazoleRepository indices, Clock horloge) {
        this.zones = zones;
        this.grilles = grilles;
        this.indices = indices;
        this.horloge = horloge;
    }

    @GetMapping("/departements")
    List<DepartementVue> departements() {
        return zones.findAllByOrderByDepartementAsc().stream()
                .map(z -> new DepartementVue(z.getDepartement(), z.getNom(), z.getZone()))
                .toList();
    }

    @GetMapping("/grilles")
    List<GrilleResume> grilles() {
        return grilles.findAllByOrderByValideDuAsc().stream()
                .map(g -> new GrilleResume(g.getId(), g.getNom(), g.getValideDu(), g.getValideAu()))
                .toList();
    }

    /** La grille qui s'applique à une date, aujourd'hui par défaut. */
    @GetMapping("/grilles/en-vigueur")
    GrilleVue enVigueur(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate jour = date != null ? date : LocalDate.now(horloge);
        Grille g = grilles.enVigueur(jour)
                .orElseThrow(() -> new IntrouvableException("Aucune grille ne s'applique le " + jour + "."));
        List<TrancheVue> tranches = g.getTranches().stream()
                .map(t -> new TrancheVue(t.getZone(), t.getPoidsMin(), t.getPoidsMax(), t.getMode(), t.getPrix()))
                .toList();
        return new GrilleVue(g.getNom(), g.getValideDu(), g.getValideAu(), g.getMinimumPerception(),
                g.getPrixHayon(), g.getPrixRendezVous(), g.getTauxTva(), tranches);
    }

    @GetMapping("/indices-gazole")
    List<IndiceVue> indicesGazole() {
        return indices.findAllByOrderByMoisAsc().stream()
                .map(i -> new IndiceVue(i.getMois(), i.getTaux()))
                .toList();
    }
}
