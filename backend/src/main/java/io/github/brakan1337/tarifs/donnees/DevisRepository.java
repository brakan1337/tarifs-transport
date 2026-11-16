package io.github.brakan1337.tarifs.donnees;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DevisRepository extends JpaRepository<Devis, Long> {

    @EntityGraph(attributePaths = {"lignes", "departement", "grille"})
    Optional<Devis> findByNumero(String numero);

    @EntityGraph(attributePaths = {"departement", "grille"})
    List<Devis> findTop20ByOrderByCreeLeDesc();
}
