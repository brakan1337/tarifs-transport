package io.github.brakan1337.tarifs.devis;

import io.github.brakan1337.tarifs.donnees.Devis;
import io.github.brakan1337.tarifs.donnees.DevisRepository;
import io.github.brakan1337.tarifs.donnees.Grille;
import io.github.brakan1337.tarifs.donnees.GrilleRepository;
import io.github.brakan1337.tarifs.donnees.IndiceGazole;
import io.github.brakan1337.tarifs.donnees.IndiceGazoleRepository;
import io.github.brakan1337.tarifs.donnees.ZoneDepartement;
import io.github.brakan1337.tarifs.donnees.ZoneDepartementRepository;
import io.github.brakan1337.tarifs.moteur.Calcul;
import io.github.brakan1337.tarifs.moteur.Expedition;
import io.github.brakan1337.tarifs.moteur.MoteurTarif;
import io.github.brakan1337.tarifs.web.DemandeImpossibleException;
import io.github.brakan1337.tarifs.web.IntrouvableException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Rassemble ce dont le moteur a besoin (zone, grille, taux gazole), lance le calcul
 * et enregistre le devis.
 */
@Service
public class DevisService {

    private static final DateTimeFormatter JOUR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ZoneDepartementRepository zones;
    private final GrilleRepository grilles;
    private final IndiceGazoleRepository indices;
    private final DevisRepository devisRepository;
    private final MoteurTarif moteur;
    private final Clock horloge;

    public DevisService(ZoneDepartementRepository zones, GrilleRepository grilles, IndiceGazoleRepository indices,
            DevisRepository devisRepository, MoteurTarif moteur, Clock horloge) {
        this.zones = zones;
        this.grilles = grilles;
        this.indices = indices;
        this.devisRepository = devisRepository;
        this.moteur = moteur;
        this.horloge = horloge;
    }

    /** Calcule un prix sans rien enregistrer, pour l'affichage en direct pendant la saisie. */
    @Transactional(readOnly = true)
    public DevisVue simuler(DemandeDevis demande) {
        Preparation p = preparer(demande);
        return DevisVue.simulation(demande, p.zone(), p.grille(), p.tauxGazole(), p.calcul());
    }

    @Transactional
    public DevisVue enregistrer(DemandeDevis demande) {
        Preparation p = preparer(demande);
        Calcul calcul = p.calcul();
        Devis devis = new Devis(
                LocalDateTime.now(horloge), p.zone(), demande.poidsKg(), demande.volumeM3(),
                demande.dateEnlevement(), demande.hayon(), demande.rendezVous(), p.grille(), p.zone().getZone(),
                calcul.poidsTaxableKg(), p.tauxGazole(), calcul.totalHt(), calcul.tva(), calcul.totalTtc());
        calcul.lignes().forEach(ligne -> devis.ajouterLigne(ligne.libelle(), ligne.montant()));
        devisRepository.save(devis);
        devis.numeroter();
        return DevisVue.de(devis);
    }

    @Transactional(readOnly = true)
    public DevisVue trouver(String numero) {
        return devisRepository.findByNumero(numero)
                .map(DevisVue::de)
                .orElseThrow(() -> new IntrouvableException("Aucun devis ne porte le numéro " + numero + "."));
    }

    @Transactional(readOnly = true)
    public List<DevisResume> derniers() {
        return devisRepository.findTop20ByOrderByCreeLeDesc().stream().map(DevisResume::de).toList();
    }

    private Preparation preparer(DemandeDevis demande) {
        LocalDate date = demande.dateEnlevement();
        ZoneDepartement zone = zones.findById(demande.departement())
                .orElseThrow(() -> new DemandeImpossibleException(
                        "Le département " + demande.departement() + " n'est pas desservi."));
        Grille grille = grilles.enVigueur(date)
                .orElseThrow(() -> new DemandeImpossibleException(
                        "Aucune grille tarifaire ne couvre le " + date.format(JOUR) + "."));
        IndiceGazole indice = indices.findFirstByMoisLessThanEqualOrderByMoisDesc(date.withDayOfMonth(1))
                .orElseThrow(() -> new DemandeImpossibleException(
                        "Aucun taux de surcharge gazole n'est publié pour le " + date.format(JOUR) + "."));

        Expedition expedition = new Expedition(
                zone.getZone(), demande.poidsKg(), demande.volumeM3(), demande.hayon(), demande.rendezVous());
        Calcul calcul = moteur.calculer(grille.versBareme(), expedition, indice.getTaux());
        return new Preparation(zone, grille, indice.getTaux(), calcul);
    }

    private record Preparation(ZoneDepartement zone, Grille grille, BigDecimal tauxGazole, Calcul calcul) {
    }
}
