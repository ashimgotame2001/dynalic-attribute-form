package com.example.dynamicform.product.config;

import com.example.dynamicform.product.entity.DocumentEntity;
import com.example.dynamicform.product.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentDataLoader implements CommandLineRunner {

    private final DocumentRepository documentRepository;

    @Override
    public void run(String... args) {
        log.info("Initializing document types...");
        List<String> documentTypes = Arrays.asList(
                "Driving Licence",
                "Passport",
                "National ID",
                "Utility Bill",
                "Birth Certificate",
                "Voter ID",
                "PAN Card",
                "Social Security Card",
                "Visa",
                "Work Permit"
        );

        for (String type : documentTypes) {
            if (!documentRepository.existsByDocumentName(type)) {
                DocumentEntity document = DocumentEntity.builder()
                        .documentName(type)
                        .description(type + " for remittance")
                        .isActive(true)
                        .build();
                documentRepository.save(document);
                log.info("Loaded document type: {}", type);
            } else {
                log.info("Document type already exists, skipping: {}", type);
            }
        }
        log.info("Document types initialization completed.");
    }
}
