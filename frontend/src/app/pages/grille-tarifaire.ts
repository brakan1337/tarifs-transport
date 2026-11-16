import { Component, computed, inject, signal } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { catchError, map, of, switchMap } from 'rxjs';
import { Grille, Probleme, Tranche } from '../api/modeles';
import { lireProbleme, TarifsApi } from '../api/tarifs-api';
import { aujourdhui, enEuros, libelleTranche, prixTranche } from '../outils/format';

const ZONES = [1, 2, 3, 4] as const;

interface LigneGrille {
  libelle: string;
  prix: Record<number, string>;
}

/** Range les tranches en tableau : une ligne par tranche de poids, une colonne par zone. */
export function tableauGrille(tranches: Tranche[]): LigneGrille[] {
  const lignes = new Map<string, LigneGrille>();
  for (const t of [...tranches].sort((a, b) => a.poidsMin - b.poidsMin)) {
    const cle = `${t.poidsMin}-${t.poidsMax}`;
    const ligne = lignes.get(cle) ?? { libelle: libelleTranche(t), prix: {} };
    ligne.prix[t.zone] = prixTranche(t);
    lignes.set(cle, ligne);
  }
  return [...lignes.values()];
}

@Component({
  selector: 'app-grille-tarifaire',
  template: `
    <h1>Grille tarifaire</h1>
    <p class="sous-titre">Les prix changent avec la date : chaque grille a sa période de validité.</p>

    <div class="date">
      <label for="date">Grille en vigueur le</label>
      <input id="date" type="date" [value]="date()" (change)="changerDate($event)" />
    </div>

    @if (etat(); as e) {
      @if (e.probleme; as p) {
        <div class="alerte" role="alert"><strong>{{ p.title }}</strong> {{ p.detail }}</div>
      } @else if (e.grille; as g) {
        <section class="carte">
          <h2>{{ g.nom }}</h2>
          <p class="validite">
            Du {{ jour(g.valideDu) }} {{ g.valideAu ? 'au ' + jour(g.valideAu) : 'jusqu’à la prochaine grille' }}
          </p>
          <div class="defilement">
            <table>
              <thead>
                <tr>
                  <th scope="col">Poids taxable</th>
                  @for (z of zones; track z) {
                    <th scope="col" class="nombre">Zone {{ z }}</th>
                  }
                </tr>
              </thead>
              <tbody>
                @for (ligne of lignes(); track ligne.libelle) {
                  <tr>
                    <th scope="row">{{ ligne.libelle }}</th>
                    @for (z of zones; track z) {
                      <td class="nombre">{{ ligne.prix[z] ?? '—' }}</td>
                    }
                  </tr>
                }
              </tbody>
            </table>
          </div>
        </section>

        <div class="bas">
          <section class="carte">
            <h2>Règles et options</h2>
            <ul class="regles">
              <li>Minimum de perception : <strong>{{ euros(g.minimumPerception) }}</strong></li>
              <li>Livraison avec hayon : <strong>{{ euros(g.prixHayon) }}</strong></li>
              <li>Livraison sur rendez-vous : <strong>{{ euros(g.prixRendezVous) }}</strong></li>
              <li>TVA : <strong>{{ g.tauxTva }} %</strong></li>
              <li>1 m³ est facturé comme 250 kg ; on retient le plus lourd des deux.</li>
              <li>Poids de bascule : un envoi ne paie jamais plus que le minimum de la tranche suivante.</li>
            </ul>
          </section>

          <section class="carte">
            <h2>Zones au départ de Mont-de-Marsan</h2>
            @for (z of zones; track z) {
              <p class="zone"><strong>Zone {{ z }}</strong> {{ zonesTexte()[z] }}</p>
            }
          </section>

          <section class="carte">
            <h2>Surcharge gazole</h2>
            <table>
              <tbody>
                @for (i of indices(); track i.mois) {
                  <tr>
                    <td>{{ mois(i.mois) }}</td>
                    <td class="nombre">{{ i.taux }} %</td>
                  </tr>
                }
              </tbody>
            </table>
          </section>
        </div>
      }
    }
  `,
  styles: `
    .date {
      max-width: 220px;
      margin-bottom: 20px;
    }
    .validite {
      color: var(--texte-doux);
      margin: -8px 0 12px;
    }
    tbody th {
      color: var(--texte);
      font-size: inherit;
      font-weight: 500;
    }
    .bas {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
      gap: 20px;
      margin-top: 20px;
      align-items: start;
    }
    .regles {
      margin: 0;
      padding-left: 18px;
    }
    .regles li {
      margin-bottom: 6px;
    }
    .zone {
      margin: 0 0 8px;
      font-size: 0.9rem;
      color: var(--texte-doux);
    }
    .zone strong {
      display: block;
      color: var(--texte);
    }
  `,
})
export class GrilleTarifaire {
  private readonly api = inject(TarifsApi);

  protected readonly zones = ZONES;
  protected readonly euros = enEuros;
  protected readonly date = signal(aujourdhui());

  protected readonly etat = toSignal(
    toObservable(this.date).pipe(
      switchMap((date) =>
        this.api.grilleEnVigueur(date).pipe(
          map((grille): { grille: Grille | null; probleme: Probleme | null } => ({ grille, probleme: null })),
          catchError((e: unknown) => of({ grille: null, probleme: lireProbleme(e) })),
        ),
      ),
    ),
  );

  protected readonly lignes = computed(() => tableauGrille(this.etat()?.grille?.tranches ?? []));

  protected readonly indices = toSignal(this.api.indicesGazole(), { initialValue: [] });

  private readonly departements = toSignal(this.api.departements(), { initialValue: [] });

  protected readonly zonesTexte = computed(() => {
    const texte: Record<number, string> = {};
    for (const z of ZONES) {
      texte[z] = this.departements()
        .filter((d) => d.zone === z)
        .map((d) => d.nom)
        .join(', ');
    }
    return texte;
  });

  protected changerDate(evenement: Event): void {
    const valeur = (evenement.target as HTMLInputElement).value;
    if (valeur) {
      this.date.set(valeur);
    }
  }

  protected jour(iso: string): string {
    return new Date(`${iso}T12:00:00`).toLocaleDateString('fr-FR');
  }

  protected mois(iso: string): string {
    return new Date(`${iso}T12:00:00`).toLocaleDateString('fr-FR', { month: 'long', year: 'numeric' });
  }
}
