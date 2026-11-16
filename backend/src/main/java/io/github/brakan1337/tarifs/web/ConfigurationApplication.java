package io.github.brakan1337.tarifs.web;

import io.github.brakan1337.tarifs.moteur.MoteurTarif;
import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class ConfigurationApplication {

    /** L'heure passe par un bean : les tests peuvent la figer. */
    @Bean
    Clock horloge() {
        return Clock.system(ZoneId.of("Europe/Paris"));
    }

    @Bean
    MoteurTarif moteurTarif() {
        return new MoteurTarif();
    }
}
