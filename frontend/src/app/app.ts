import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <header class="entete">
      <div class="marque">
        <span class="logo" aria-hidden="true">▶</span>
        <div>
          <strong>Tarifs Transport</strong>
          <small>Transporteur fictif, agence de Mont-de-Marsan</small>
        </div>
      </div>
      <nav aria-label="Navigation principale">
        <a routerLink="/" routerLinkActive="actif" [routerLinkActiveOptions]="{ exact: true }">Nouveau devis</a>
        <a routerLink="/grille" routerLinkActive="actif">Grille tarifaire</a>
        <a routerLink="/historique" routerLinkActive="actif">Historique</a>
      </nav>
    </header>
    <main>
      <router-outlet />
    </main>
  `,
  styleUrl: './app.css',
})
export class App {}
