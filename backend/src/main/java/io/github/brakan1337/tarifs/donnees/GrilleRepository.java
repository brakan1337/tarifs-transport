package io.github.brakan1337.tarifs.donnees;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GrilleRepository extends JpaRepository<Grille, Long> {

    /** Les grilles valables à une date, la plus récente en tête. */
    @EntityGraph(attributePaths = "tranches")
    @Query("""
            select g from Grille g
            where g.valideDu <= :date and (g.valideAu is null or g.valideAu >= :date)
            order by g.valideDu desc
            """)
    List<Grille> valablesLe(@Param("date") LocalDate date);

    /** La grille qui s'applique : si deux périodes se chevauchent, la plus récente gagne. */
    default Optional<Grille> enVigueur(LocalDate date) {
        return valablesLe(date).stream().findFirst();
    }

    List<Grille> findAllByOrderByValideDuAsc();
}
