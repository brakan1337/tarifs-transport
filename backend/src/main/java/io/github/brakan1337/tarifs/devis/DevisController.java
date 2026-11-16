package io.github.brakan1337.tarifs.devis;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/devis")
class DevisController {

    private final DevisService service;

    DevisController(DevisService service) {
        this.service = service;
    }

    @PostMapping
    ResponseEntity<DevisVue> creer(@Valid @RequestBody DemandeDevis demande) {
        DevisVue devis = service.enregistrer(demande);
        return ResponseEntity.created(URI.create("/api/devis/" + devis.numero())).body(devis);
    }

    @PostMapping("/simulation")
    DevisVue simuler(@Valid @RequestBody DemandeDevis demande) {
        return service.simuler(demande);
    }

    @GetMapping
    List<DevisResume> derniers() {
        return service.derniers();
    }

    @GetMapping("/{numero}")
    DevisVue trouver(@PathVariable String numero) {
        return service.trouver(numero);
    }
}
