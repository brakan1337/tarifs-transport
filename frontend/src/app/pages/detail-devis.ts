import { Component, inject, input } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { catchError, map, of, switchMap } from 'rxjs';
import { Devis, Probleme } from '../api/modeles';
import { lireProbleme, TarifsApi } from '../api/tarifs-api';
import { ResultatDevis } from './resultat-devis';

@Component({
  selector: 'app-detail-devis',
  imports: [ResultatDevis, RouterLink],
  template: `
    @if (etat(); as e) {
      @if (e.probleme; as p) {
        <div class="alerte" role="alert"><strong>{{ p.title }}</strong> {{ p.detail }}</div>
        <p><a routerLink="/historique">Retour à l’historique</a></p>
      } @else if (e.devis; as d) {
        <div class="titre">
          <div>
            <h1>Devis {{ d.numero }}</h1>
            <p class="sous-titre">
              Établi le {{ horodatage(d.creeLe) }} pour un enlèvement le {{ jour(d.dateEnlevement) }}.
            </p>
          </div>
          <div class="actions">
            <button type="button" class="secondaire" (click)="imprimer()">Imprimer</button>
            <a routerLink="/">Nouveau devis</a>
          </div>
        </div>
        <section class="carte">
          <app-resultat-devis [devis]="d" />
        </section>
      }
    }
  `,
  styles: `
    .titre {
      display: flex;
      flex-wrap: wrap;
      justify-content: space-between;
      align-items: start;
      gap: 12px;
    }
    .actions {
      display: flex;
      align-items: center;
      gap: 16px;
    }
    section {
      max-width: 640px;
    }
    @media print {
      .actions {
        display: none;
      }
    }
  `,
})
export class DetailDevis {
  private readonly api = inject(TarifsApi);

  /** Rempli par le routeur à partir de l'adresse /devis/:numero. */
  readonly numero = input.required<string>();

  protected readonly etat = toSignal(
    toObservable(this.numero).pipe(
      switchMap((numero) =>
        this.api.devis(numero).pipe(
          map((devis): { devis: Devis | null; probleme: Probleme | null } => ({ devis, probleme: null })),
          catchError((e: unknown) => of({ devis: null, probleme: lireProbleme(e) })),
        ),
      ),
    ),
  );

  protected imprimer(): void {
    window.print();
  }

  protected jour(iso: string): string {
    return new Date(`${iso}T12:00:00`).toLocaleDateString('fr-FR');
  }

  protected horodatage(iso: string | null): string {
    return iso ? new Date(iso).toLocaleString('fr-FR', { dateStyle: 'long', timeStyle: 'short' }) : '';
  }
}
