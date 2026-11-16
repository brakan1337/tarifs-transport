package io.github.brakan1337.tarifs.donnees;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ZoneDepartementRepository extends JpaRepository<ZoneDepartement, String> {

    List<ZoneDepartement> findAllByOrderByDepartementAsc();
}
