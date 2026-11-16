import { aujourdhui, enEuros, libelleTranche, prixTranche } from './format';

/** Intl insère des espaces insécables : on les normalise pour comparer. */
const normal = (texte: string) => texte.replace(/\s/g, ' ');

describe('format', () => {
  it('écrit les montants en euros, à la française', () => {
    expect(normal(enEuros(1234.5))).toBe('1 234,50 €');
  });

  it('distingue un forfait d’un prix au poids', () => {
    expect(normal(prixTranche({ zone: 1, poidsMin: 0, poidsMax: 10, mode: 'FORFAIT', prix: 11.9 }))).toBe(
      '11,90 €',
    );
    expect(
      normal(prixTranche({ zone: 1, poidsMin: 100, poidsMax: 300, mode: 'AU_100KG', prix: 35.4 })),
    ).toBe('35,40 € / 100 kg');
  });

  it('affiche les bornes de la tranche avec séparateur de milliers', () => {
    expect(normal(libelleTranche({ poidsMin: 1000, poidsMax: 3000 }))).toBe('1 000 à 3 000 kg');
  });

  it('donne la date du jour au format ISO', () => {
    expect(aujourdhui(new Date(2026, 10, 16, 23, 30))).toBe('2026-11-16');
  });
});
