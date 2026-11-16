/** Les formes de données renvoyées par l'API Spring Boot. Les montants arrivent en nombres. */

export interface Departement {
  code: string;
  nom: string;
  zone: number;
}

export interface DemandeDevis {
  departement: string;
  poidsKg: number;
  volumeM3: number;
  dateEnlevement: string;
  hayon: boolean;
  rendezVous: boolean;
}

export interface LigneDevis {
  libelle: string;
  montant: number;
}

export interface Devis {
  numero: string | null;
  creeLe: string | null;
  departement: string;
  nomDepartement: string;
  zone: number;
  grille: string;
  poidsKg: number;
  volumeM3: number;
  poidsTaxableKg: number;
  dateEnlevement: string;
  hayon: boolean;
  rendezVous: boolean;
  tauxGazole: number;
  lignes: LigneDevis[];
  totalHt: number;
  tva: number;
  totalTtc: number;
}

export interface DevisResume {
  numero: string;
  creeLe: string;
  departement: string;
  nomDepartement: string;
  poidsKg: number;
  dateEnlevement: string;
  totalTtc: number;
}

export type ModeTarif = 'FORFAIT' | 'AU_100KG';

export interface Tranche {
  zone: number;
  poidsMin: number;
  poidsMax: number;
  mode: ModeTarif;
  prix: number;
}

export interface Grille {
  nom: string;
  valideDu: string;
  valideAu: string | null;
  minimumPerception: number;
  prixHayon: number;
  prixRendezVous: number;
  tauxTva: number;
  tranches: Tranche[];
}

export interface IndiceGazole {
  mois: string;
  taux: number;
}

/** Une erreur au format Problem Details (RFC 9457), telle que le serveur la renvoie. */
export interface Probleme {
  title?: string;
  detail?: string;
  erreurs?: Record<string, string>;
}
