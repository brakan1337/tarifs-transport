package io.github.brakan1337.tarifs.web;

/** La demande est bien formée, mais on ne peut pas la chiffrer (département non desservi, date sans grille…). */
public class DemandeImpossibleException extends RuntimeException {

    public DemandeImpossibleException(String message) {
        super(message);
    }
}
