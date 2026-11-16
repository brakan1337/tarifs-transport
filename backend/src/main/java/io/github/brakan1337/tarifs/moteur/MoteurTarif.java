package io.github.brakan1337.tarifs.moteur;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Calcule le prix d'une expédition à partir d'une grille.
 *
 * <p>Aucune dépendance à Spring ni à la base : le calcul se teste seul.
 * Tous les montants sont en {@link BigDecimal}. Chaque ligne est arrondie au centime,
 * puis le total est la somme des lignes arrondies : le client retrouve toujours
 * le total en additionnant ce qu'il lit.
 *
 * <p>Ordre des règles : poids taxable, tranche, poids de bascule, minimum de perception,
 * surcharge gazole, options, TVA.
 */
public final class MoteurTarif {

    /** En messagerie, 1 m³ est facturé comme 250 kg. */
    public static final BigDecimal KG_PAR_M3 = new BigDecimal("250");

    private static final BigDecimal CENT = new BigDecimal("100");

    public Calcul calculer(Bareme bareme, Expedition expedition, BigDecimal tauxGazolePct) {
        BigDecimal poidsTaxable = poidsTaxable(expedition.poidsKg(), expedition.volumeM3());
        Tranche tranche = bareme.trancheFor(expedition.zone(), poidsTaxable)
                .orElseThrow(() -> new PoidsHorsGrilleException(expedition.zone(), poidsTaxable));

        List<LigneCalcul> lignes = new ArrayList<>();

        BigDecimal transport = tranche.prixPour(poidsTaxable);
        String libelleTransport = "Transport " + tranche.libelle();

        // Poids de bascule : si le minimum de la tranche suivante coûte moins cher,
        // on facture ce minimum. Sans cette règle, 290 kg coûteraient plus que 300 kg.
        Optional<Tranche> suivante = bareme.trancheSuivante(tranche);
        if (suivante.isPresent()) {
            Tranche bascule = suivante.get();
            BigDecimal prixBascule = bascule.prixPour(bascule.poidsMinKg());
            if (prixBascule.compareTo(transport) < 0) {
                transport = prixBascule;
                libelleTransport = "Transport " + bascule.libelle() + ", au poids de bascule "
                        + bascule.poidsMinKg().stripTrailingZeros().toPlainString() + " kg";
            }
        }

        if (transport.compareTo(bareme.minimumPerception()) < 0) {
            transport = bareme.minimumPerception();
            libelleTransport = "Transport, minimum de perception";
        }
        transport = auCentime(transport);
        lignes.add(new LigneCalcul(libelleTransport, transport));

        // La surcharge gazole porte sur le transport seul, jamais sur les options.
        BigDecimal gazole = auCentime(transport.multiply(tauxGazolePct).divide(CENT));
        if (gazole.signum() > 0) {
            lignes.add(new LigneCalcul(
                    "Surcharge gazole " + tauxGazolePct.stripTrailingZeros().toPlainString() + " %", gazole));
        }
        if (expedition.hayon()) {
            lignes.add(new LigneCalcul("Livraison avec hayon", auCentime(bareme.prixHayon())));
        }
        if (expedition.rendezVous()) {
            lignes.add(new LigneCalcul("Livraison sur rendez-vous", auCentime(bareme.prixRendezVous())));
        }

        BigDecimal totalHt = lignes.stream().map(LigneCalcul::montant).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tva = auCentime(totalHt.multiply(bareme.tauxTvaPct()).divide(CENT));
        return new Calcul(poidsTaxable, tranche, lignes, totalHt, tva, totalHt.add(tva));
    }

    /**
     * Le plus lourd du poids réel et du poids volumétrique, arrondi au kilo supérieur.
     * Un colis léger mais encombrant paie la place qu'il prend dans le camion.
     */
    static BigDecimal poidsTaxable(BigDecimal poidsKg, BigDecimal volumeM3) {
        BigDecimal volumetrique = volumeM3.multiply(KG_PAR_M3);
        return poidsKg.max(volumetrique).setScale(0, RoundingMode.CEILING);
    }

    private static BigDecimal auCentime(BigDecimal montant) {
        return montant.setScale(2, RoundingMode.HALF_UP);
    }
}
