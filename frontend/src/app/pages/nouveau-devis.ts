import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { catchError, debounceTime, distinctUntilChanged, filter, map, of, startWith, switchMap, tap } from 'rxjs';
import { DemandeDevis, Devis, Probleme } from '../api/modeles';
import { lireProbleme, TarifsApi } from '../api/tarifs-api';
import { aujourdhui } from '../outils/format';
import { ResultatDevis } from './resultat-devis';

type Champ = 'departement' | 'poidsKg' | 'volumeM3' | 'dateEnlevement';

const MESSAGES: Record<Champ, Record<string, string>> = {
  departement: { required: 'Choisissez un département.' },
  poidsKg: {
    required: 'Indiquez le poids.',
    min: 'Le poids doit dépasser 0,1 kg.',
    max: 'Au-delà de 3 000 kg, c’est un affrètement : devis sur demande.',
  },
  volumeM3: {
    required: 'Indiquez le volume, 0 s’il est inconnu.',
    min: 'Le volume ne peut pas être négatif.',
    max: 'Au-delà de 30 m³, c’est un camion complet : devis sur demande.',
  },
  dateEnlevement: { required: 'Indiquez la date d’enlèvement.' },
};

@Component({
  selector: 'app-nouveau-devis',
  imports: [ReactiveFormsModule, ResultatDevis],
  template: `
    <h1>Nouveau devis</h1>
    <p class="sous-titre">Le prix se recalcule à chaque modification. Enregistrez-le pour lui donner un numéro.</p>

    <div class="colonnes">
      <form class="carte" [formGroup]="formulaire" (ngSubmit)="enregistrer()" novalidate>
        <div class="champ">
          <label for="departement">Département de livraison</label>
          <select id="departement" formControlName="departement">
            @for (d of departements(); track d.code) {
              <option [value]="d.code">{{ d.code }} {{ d.nom }} (zone {{ d.zone }})</option>
            }
          </select>
          @if (erreur('departement'); as message) {
            <p class="erreur-champ">{{ message }}</p>
          }
        </div>

        <div class="deux">
          <div class="champ">
            <label for="poids">Poids réel (kg)</label>
            <input id="poids" type="number" min="0.1" max="3000" step="0.1" formControlName="poidsKg" />
            @if (erreur('poidsKg'); as message) {
              <p class="erreur-champ">{{ message }}</p>
            }
          </div>
          <div class="champ">
            <label for="volume">Volume (m³)</label>
            <input id="volume" type="number" min="0" max="30" step="0.01" formControlName="volumeM3" />
            @if (erreur('volumeM3'); as message) {
              <p class="erreur-champ">{{ message }}</p>
            }
          </div>
        </div>

        <div class="champ">
          <label for="date">Date d’enlèvement</label>
          <input id="date" type="date" formControlName="dateEnlevement" />
          @if (erreur('dateEnlevement'); as message) {
            <p class="erreur-champ">{{ message }}</p>
          }
        </div>

        <fieldset>
          <legend>Options de livraison</legend>
          <label class="case"><input type="checkbox" formControlName="hayon" /> Camion avec hayon</label>
          <label class="case"><input type="checkbox" formControlName="rendezVous" /> Livraison sur rendez-vous</label>
        </fieldset>

        <button type="submit" [disabled]="!resultat() || enregistrement()">
          {{ enregistrement() ? 'Enregistrement…' : 'Enregistrer le devis' }}
        </button>
      </form>

      <section class="carte" aria-live="polite" [class.attente]="calculEnCours()">
        <h2>Prix</h2>
        @if (probleme(); as p) {
          <div class="alerte" role="alert">
            <strong>{{ p.title }}</strong>
            <p>{{ p.detail }}</p>
            @if (p.erreurs) {
              <ul>
                @for (e of listeErreurs(p); track e) {
                  <li>{{ e }}</li>
                }
              </ul>
            }
          </div>
        } @else if (resultat(); as devis) {
          <app-resultat-devis [devis]="devis" />
        } @else {
          <p class="vide">Remplissez le formulaire pour voir le prix.</p>
        }
      </section>
    </div>
  `,
  styles: `
    .colonnes {
      display: grid;
      grid-template-columns: minmax(0, 1fr) minmax(0, 1.1fr);
      gap: 20px;
      align-items: start;
    }
    @media (max-width: 820px) {
      .colonnes {
        grid-template-columns: 1fr;
      }
    }
    .champ {
      margin-bottom: 16px;
    }
    .deux {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 12px;
    }
    fieldset {
      border: 1px solid var(--bordure);
      border-radius: 6px;
      margin: 0 0 20px;
      padding: 10px 14px;
    }
    legend {
      font-weight: 500;
      padding: 0 4px;
    }
    .case {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: 400;
      margin: 6px 0;
    }
    .attente {
      opacity: 0.6;
      transition: opacity 0.2s;
    }
    .vide {
      color: var(--texte-doux);
    }
    .alerte p {
      margin: 4px 0 0;
    }
  `,
})
export class NouveauDevis {
  private readonly api = inject(TarifsApi);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly departements = toSignal(this.api.departements(), { initialValue: [] });

  protected readonly formulaire = inject(NonNullableFormBuilder).group({
    departement: ['33', Validators.required],
    poidsKg: [250, [Validators.required, Validators.min(0.1), Validators.max(3000)]],
    volumeM3: [0.5, [Validators.required, Validators.min(0), Validators.max(30)]],
    dateEnlevement: [aujourdhui(), Validators.required],
    hayon: [false],
    rendezVous: [false],
  });

  protected readonly resultat = signal<Devis | null>(null);
  protected readonly probleme = signal<Probleme | null>(null);
  protected readonly calculEnCours = signal(false);
  protected readonly enregistrement = signal(false);

  constructor() {
    // Simulation en direct : on attend une courte pause de frappe, on ignore les saisies
    // invalides, et switchMap annule la requête précédente si une nouvelle arrive.
    this.formulaire.valueChanges
      .pipe(
        startWith(null),
        debounceTime(250),
        map(() => this.formulaire.getRawValue()),
        tap(() => {
          if (this.formulaire.invalid) {
            this.resultat.set(null);
            this.probleme.set(null);
          }
        }),
        filter(() => this.formulaire.valid),
        distinctUntilChanged((a, b) => JSON.stringify(a) === JSON.stringify(b)),
        tap(() => this.calculEnCours.set(true)),
        switchMap((demande) =>
          this.api.simuler(demande).pipe(
            map((devis) => ({ devis, probleme: null })),
            catchError((e: unknown) => of({ devis: null, probleme: lireProbleme(e) })),
          ),
        ),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(({ devis, probleme }) => {
        this.calculEnCours.set(false);
        this.resultat.set(devis);
        this.probleme.set(probleme);
      });
  }

  protected erreur(champ: Champ): string | null {
    const controle = this.formulaire.controls[champ];
    if (!controle.errors || !(controle.touched || controle.dirty)) {
      return null;
    }
    const cle = Object.keys(controle.errors)[0];
    return MESSAGES[champ][cle] ?? 'Valeur incorrecte.';
  }

  protected listeErreurs(probleme: Probleme): string[] {
    return Object.values(probleme.erreurs ?? {});
  }

  protected enregistrer(): void {
    this.formulaire.markAllAsTouched();
    if (this.formulaire.invalid || this.enregistrement()) {
      return;
    }
    const demande: DemandeDevis = this.formulaire.getRawValue();
    this.enregistrement.set(true);
    this.api
      .enregistrer(demande)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (devis) => this.router.navigate(['/devis', devis.numero]),
        error: (e: unknown) => {
          this.enregistrement.set(false);
          this.probleme.set(lireProbleme(e));
        },
      });
  }
}
