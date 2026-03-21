package ru.itis.dis403.lab2_2.context.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import ru.itis.dis403.lab2_2.context.components.MarketService;
import ru.itis.dis403.lab2_2.context.model.Market;
import ru.itis.dis403.lab2_2.orm.EntityManagerFactory;

@Configuration
@ComponentScan("ru.itis.dis403.lab2_2.context.components")
public class Config {

    @Bean
    public EntityManagerFactory entityManagerFactory() throws Exception {
        return new EntityManagerFactory("application.yaml");
    }

    @Bean
    @Scope("singleton")
    public Market market() {
        return new Market();
    }

    @Bean
    public MarketService marketService(Market market, EntityManagerFactory emf) {
        return new MarketService(market, emf);
    }
}