package io.github.brakan1337.tarifs.donnees;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IndiceGazoleRepository extends JpaRepository<IndiceGazole, LocalDate> {

    /** Le dernier taux publié à cette date : un mois pas encore publié reprend le précédent. */
    Optional<IndiceGazole> findFirstByMoisLessThanEqualOrderByMoisDesc(LocalDate mois);

    List<IndiceGazole> findAllByOrderByMoisAsc();
}
