import { Tranche } from '../api/modeles';

const euros = new Intl.NumberFormat('fr-FR', { style: 'currency', currency: 'EUR' });
const nombre = new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 3 });

export function enEuros(montant: number): string {
  return euros.format(montant);
}

export function enNombre(valeur: number): string {
  return nombre.format(valeur);
}

/** « 12,50 € » pour un forfait, « 35,40 € / 100 kg » pour un prix au poids. */
export function prixTranche(tranche: Tranche): string {
  return tranche.mode === 'FORFAIT' ? enEuros(tranche.prix) : `${enEuros(tranche.prix)} / 100 kg`;
}

/** « 0 à 10 kg », « 1 000 à 3 000 kg ». */
export function libelleTranche(tranche: Pick<Tranche, 'poidsMin' | 'poidsMax'>): string {
  return `${enNombre(tranche.poidsMin)} à ${enNombre(tranche.poidsMax)} kg`;
}

/** La date du jour au format attendu par l'API (AAAA-MM-JJ), en heure locale. */
export function aujourdhui(maintenant = new Date()): string {
  const decalage = maintenant.getTimezoneOffset() * 60_000;
  return new Date(maintenant.getTime() - decalage).toISOString().slice(0, 10);
}
