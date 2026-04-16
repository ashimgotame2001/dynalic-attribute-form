package com.example.dynamicform.product.config;

import com.example.dynamicform.product.entity.LanguageEntity;
import com.example.dynamicform.product.repository.LanguageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class LanguagePreloader {

    private static final Logger logger = LoggerFactory.getLogger(LanguagePreloader.class);

    @Bean
    public CommandLineRunner preloadLanguages(LanguageRepository languageRepository) {
        return args -> {
            if (languageRepository.count() == 0) {
                logger.info("Preloading languages into the database...");
                List<LanguageEntity> languages = List.of(
                        LanguageEntity.builder().code("en").name("English").active(true).build(),
                        LanguageEntity.builder().code("es").name("Spanish").active(true).build(),
                        LanguageEntity.builder().code("fr").name("French").active(true).build(),
                        LanguageEntity.builder().code("de").name("German").active(true).build(),
                        LanguageEntity.builder().code("ne").name("Nepali").active(true).build()
                );
                languageRepository.saveAll(languages);
                logger.info("Successfully preloaded {} languages.", languages.size());
            } else {
                logger.debug("Languages already present in the database, skipping preloading.");
            }
        };
    }
}
