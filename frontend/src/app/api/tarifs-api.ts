import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  Departement,
  DemandeDevis,
  Devis,
  DevisResume,
  Grille,
  IndiceGazole,
  Probleme,
} from './modeles';

@Injectable({ providedIn: 'root' })
export class TarifsApi {
  private readonly http = inject(HttpClient);

  departements(): Observable<Departement[]> {
    return this.http.get<Departement[]>('/api/departements');
  }

  simuler(demande: DemandeDevis): Observable<Devis> {
    return this.http.post<Devis>('/api/devis/simulation', demande);
  }

  enregistrer(demande: DemandeDevis): Observable<Devis> {
    return this.http.post<Devis>('/api/devis', demande);
  }

  devis(numero: string): Observable<Devis> {
    return this.http.get<Devis>(`/api/devis/${encodeURIComponent(numero)}`);
  }

  derniersDevis(): Observable<DevisResume[]> {
    return this.http.get<DevisResume[]>('/api/devis');
  }

  grilleEnVigueur(date: string): Observable<Grille> {
    return this.http.get<Grille>('/api/grilles/en-vigueur', { params: new HttpParams().set('date', date) });
  }

  indicesGazole(): Observable<IndiceGazole[]> {
    return this.http.get<IndiceGazole[]>('/api/indices-gazole');
  }
}

/** Extrait un message lisible d'une erreur HTTP, quelle qu'elle soit. */
export function lireProbleme(erreur: unknown): Probleme {
  if (erreur instanceof HttpErrorResponse) {
    if (erreur.status === 0) {
      return { title: 'Serveur injoignable', detail: "L'API ne répond pas. Est-elle démarrée ?" };
    }
    if (erreur.error && typeof erreur.error === 'object') {
      return erreur.error as Probleme;
    }
  }
  return { title: 'Erreur inattendue', detail: 'Réessayez dans un instant.' };
}
