package com.example.dynamicform.product.repository;

import com.example.dynamicform.product.entity.DocumentSetupEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentSetupRepository extends JpaRepository<DocumentSetupEntity, UUID> {

    Optional<DocumentSetupEntity> findByDocument_Id(UUID documentId);

    @EntityGraph(attributePaths = {"document", "fieldConfigs", "fieldConfigs.translations", "fieldConfigs.validations", "fieldConfigs.validations.translations"})
    Optional<DocumentSetupEntity> findDetailedById(UUID id);

    @EntityGraph(attributePaths = {"document", "fieldConfigs", "fieldConfigs.translations", "fieldConfigs.validations", "fieldConfigs.validations.translations"})
    Optional<DocumentSetupEntity> findDetailedByDocument_Id(UUID documentId);
}
