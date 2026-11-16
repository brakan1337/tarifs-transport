package io.github.brakan1337.tarifs.moteur;

/** Comment une tranche de poids est facturée. */
public enum ModeTarif {
    /** Un prix fixe pour toute la tranche (petits colis). */
    FORFAIT,
    /** Un prix pour 100 kg, multiplié par le poids taxable (palettes, lots). */
    AU_100KG
}
