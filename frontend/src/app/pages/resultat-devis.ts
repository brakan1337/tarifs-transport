import { Component, input } from '@angular/core';
import { Devis } from '../api/modeles';
import { enEuros, enNombre } from '../outils/format';

/** Le détail chiffré d'un devis : lignes, totaux et ce qui a servi au calcul. */
@Component({
  selector: 'app-resultat-devis',
  template: `
    @let d = devis();
    <dl class="contexte">
      <div>
        <dt>Destination</dt>
        <dd>{{ d.departement }} {{ d.nomDepartement }}, zone {{ d.zone }}</dd>
      </div>
      <div>
        <dt>Poids taxable</dt>
        <dd>
          {{ nombre(d.poidsTaxableKg) }} kg
          @if (d.poidsTaxableKg > d.poidsKg) {
            <span class="note">volume ({{ nombre(d.volumeM3) }} m³) plus lourd que le poids réel</span>
          }
        </dd>
      </div>
      <div>
        <dt>Grille appliquée</dt>
        <dd>{{ d.grille }}</dd>
      </div>
    </dl>

    <table>
      <tbody>
        @for (ligne of d.lignes; track $index) {
          <tr>
            <td>{{ ligne.libelle }}</td>
            <td class="nombre">{{ euros(ligne.montant) }}</td>
          </tr>
        }
      </tbody>
      <tfoot>
        <tr>
          <th scope="row">Total HT</th>
          <td class="nombre">{{ euros(d.totalHt) }}</td>
        </tr>
        <tr>
          <th scope="row">TVA</th>
          <td class="nombre">{{ euros(d.tva) }}</td>
        </tr>
        <tr class="ttc">
          <th scope="row">Total TTC</th>
          <td class="nombre">{{ euros(d.totalTtc) }}</td>
        </tr>
      </tfoot>
    </table>
  `,
  styles: `
    .contexte {
      display: grid;
      gap: 10px;
      margin: 0 0 16px;
    }
    dt {
      font-size: 0.8rem;
      color: var(--texte-doux);
    }
    dd {
      margin: 0;
      font-weight: 500;
    }
    .note {
      display: block;
      font-weight: 400;
      font-size: 0.85rem;
      color: var(--texte-doux);
    }
    tfoot th {
      color: var(--texte);
      font-size: inherit;
    }
    .ttc th,
    .ttc td {
      font-size: 1.15rem;
      font-weight: 700;
      border-bottom: none;
    }
  `,
})
export class ResultatDevis {
  readonly devis = input.required<Devis>();
  protected readonly euros = enEuros;
  protected readonly nombre = enNombre;
}
