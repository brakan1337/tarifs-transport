package io.github.brakan1337.tarifs.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * L'écran Angular est servi par la même application. Ses adresses internes
 * (/grille, /historique…) renvoient vers index.html, et Angular affiche la bonne page.
 */
@Controller
class PagesController {

    @GetMapping({"/grille", "/historique", "/devis/{numero}"})
    String application() {
        return "forward:/index.html";
    }
}
