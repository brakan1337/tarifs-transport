package io.github.brakan1337.tarifs.donnees;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "zone_departement")
public class ZoneDepartement {

    @Id
    private String departement;
    private String nom;
    private int zone;

    protected ZoneDepartement() {
    }

    public String getDepartement() {
        return departement;
    }

    public String getNom() {
        return nom;
    }

    public int getZone() {
        return zone;
    }
}
