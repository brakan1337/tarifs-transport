import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Grille } from '../api/modeles';
import { GrilleTarifaire, tableauGrille } from './grille-tarifaire';

const GRILLE: Grille = {
  nom: 'Grille 2026',
  valideDu: '2026-01-01',
  valideAu: '2026-12-31',
  minimumPerception: 14.5,
  prixHayon: 28,
  prixRendezVous: 12,
  tauxTva: 20,
  tranches: [
    { zone: 2, poidsMin: 0, poidsMax: 10, mode: 'FORFAIT', prix: 14.9 },
    { zone: 1, poidsMin: 100, poidsMax: 300, mode: 'AU_100KG', prix: 35.4 },
    { zone: 1, poidsMin: 0, poidsMax: 10, mode: 'FORFAIT', prix: 11.9 },
  ],
};

const normal = (texte: string | null | undefined) => (texte ?? '').replace(/\s+/g, ' ').trim();

describe('tableauGrille', () => {
  it('range les tranches par poids, une colonne par zone', () => {
    const lignes = tableauGrille(GRILLE.tranches);
    expect(lignes.map((l) => normal(l.libelle))).toEqual(['0 à 10 kg', '100 à 300 kg']);
    expect(normal(lignes[0].prix[1])).toBe('11,90 €');
    expect(normal(lignes[0].prix[2])).toBe('14,90 €');
    expect(lignes[1].prix[2]).toBeUndefined();
  });
});

describe('GrilleTarifaire', () => {
  it('affiche la grille renvoyée par l’API', async () => {
    TestBed.configureTestingModule({
      imports: [GrilleTarifaire],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    const fixture = TestBed.createComponent(GrilleTarifaire);
    const http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();

    http.expectOne((r) => r.url === '/api/grilles/en-vigueur').flush(GRILLE);
    http.expectOne('/api/indices-gazole').flush([{ mois: '2026-11-01', taux: 21 }]);
    http.expectOne('/api/departements').flush([{ code: '40', nom: 'Landes', zone: 1 }]);
    await fixture.whenStable();

    const page = fixture.nativeElement as HTMLElement;
    expect(page.querySelector('h2')?.textContent).toBe('Grille 2026');
    const premiereLigne = page.querySelector('table tbody tr');
    const cellules = [...(premiereLigne?.querySelectorAll('td') ?? [])].map((td) => normal(td.textContent));
    expect(cellules).toEqual(['11,90 €', '14,90 €', '—', '—']);
    expect(normal(page.textContent)).toContain('Zone 1 Landes');
    http.verify();
  });
});
