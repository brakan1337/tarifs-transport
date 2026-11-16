package io.github.brakan1337.tarifs.web;

import io.github.brakan1337.tarifs.moteur.PoidsHorsGrilleException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Toutes les erreurs sortent au format standard « Problem Details » (RFC 9457),
 * avec un message en français que l'écran peut afficher tel quel.
 */
@RestControllerAdvice
class GestionErreurs extends ResponseEntityExceptionHandler {

    @ExceptionHandler({DemandeImpossibleException.class, PoidsHorsGrilleException.class})
    ProblemDetail demandeImpossible(RuntimeException e) {
        ProblemDetail probleme = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, e.getMessage());
        probleme.setTitle("Devis impossible");
        return probleme;
    }

    @ExceptionHandler(IntrouvableException.class)
    ProblemDetail introuvable(IntrouvableException e) {
        ProblemDetail probleme = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        probleme.setTitle("Introuvable");
        return probleme;
    }

    /** Ajoute le détail champ par champ, pour que le formulaire affiche chaque erreur sous le bon champ. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> erreurs = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(erreur -> erreurs.putIfAbsent(erreur.getField(), erreur.getDefaultMessage()));
        ProblemDetail probleme = ex.getBody();
        probleme.setTitle("Demande invalide");
        probleme.setDetail("Certains champs sont incorrects.");
        probleme.setProperty("erreurs", erreurs);
        return handleExceptionInternal(ex, probleme, headers, status, request);
    }
}
