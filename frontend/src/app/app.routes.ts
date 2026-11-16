import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    title: 'Nouveau devis',
    loadComponent: () => import('./pages/nouveau-devis').then((m) => m.NouveauDevis),
  },
  {
    path: 'grille',
    title: 'Grille tarifaire',
    loadComponent: () => import('./pages/grille-tarifaire').then((m) => m.GrilleTarifaire),
  },
  {
    path: 'historique',
    title: 'Historique des devis',
    loadComponent: () => import('./pages/historique').then((m) => m.Historique),
  },
  {
    path: 'devis/:numero',
    title: 'Devis',
    loadComponent: () => import('./pages/detail-devis').then((m) => m.DetailDevis),
  },
  { path: '**', redirectTo: '' },
];
