import { Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { DevisResume, Probleme } from '../api/modeles';
import { lireProbleme, TarifsApi } from '../api/tarifs-api';
import { enEuros, enNombre } from '../outils/format';

@Component({
  selector: 'app-historique',
  imports: [RouterLink],
  template: `
    <h1>Historique des devis</h1>
    <p class="sous-titre">Les 20 derniers devis enregistrés. Leurs montants restent figés, même si la grille change.</p>

    @if (etat(); as e) {
      @if (e.probleme; as p) {
        <div class="alerte" role="alert"><strong>{{ p.title }}</strong> {{ p.detail }}</div>
      } @else if (e.devis.length === 0) {
        <div class="carte">Aucun devis enregistré pour l’instant.</div>
      } @else {
        <div class="carte defilement">
          <table>
            <thead>
              <tr>
                <th scope="col">Numéro</th>
                <th scope="col">Créé le</th>
                <th scope="col">Destination</th>
                <th scope="col" class="nombre">Poids</th>
                <th scope="col">Enlèvement</th>
                <th scope="col" class="nombre">Total TTC</th>
              </tr>
            </thead>
            <tbody>
              @for (d of e.devis; track d.numero) {
                <tr>
                  <td><a [routerLink]="['/devis', d.numero]">{{ d.numero }}</a></td>
                  <td>{{ horodatage(d.creeLe) }}</td>
                  <td>{{ d.departement }} {{ d.nomDepartement }}</td>
                  <td class="nombre">{{ nombre(d.poidsKg) }} kg</td>
                  <td>{{ jour(d.dateEnlevement) }}</td>
                  <td class="nombre">{{ euros(d.totalTtc) }}</td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      }
    }
  `,
})
export class Historique {
  private readonly api = inject(TarifsApi);

  protected readonly euros = enEuros;
  protected readonly nombre = enNombre;

  protected readonly etat = toSignal(
    this.api.derniersDevis().pipe(
      map((devis): { devis: DevisResume[]; probleme: Probleme | null } => ({ devis, probleme: null })),
      catchError((e: unknown) => of({ devis: [], probleme: lireProbleme(e) })),
    ),
  );

  protected jour(iso: string): string {
    return new Date(`${iso}T12:00:00`).toLocaleDateString('fr-FR');
  }

  protected horodatage(iso: string): string {
    return new Date(iso).toLocaleString('fr-FR', { dateStyle: 'short', timeStyle: 'short' });
  }
}
