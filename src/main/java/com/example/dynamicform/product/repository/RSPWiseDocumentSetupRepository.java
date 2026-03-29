package com.example.dynamicform.product.repository;

import com.example.dynamicform.product.entity.RSPWiseDocumentSetupEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RSPWiseDocumentSetupRepository extends JpaRepository<RSPWiseDocumentSetupEntity, UUID> {
    List<RSPWiseDocumentSetupEntity> findByRspId(Long rspId);

    Optional<RSPWiseDocumentSetupEntity> findByRspIdAndDocument_Id(Long rspId, UUID documentId);

    @EntityGraph(attributePaths = {"document", "fieldConfigs", "fieldConfigs.translations", "fieldConfigs.validations", "fieldConfigs.validations.translations"})
    Optional<RSPWiseDocumentSetupEntity> findDetailedById(UUID id);

    @EntityGraph(attributePaths = {"document", "fieldConfigs", "fieldConfigs.translations", "fieldConfigs.validations", "fieldConfigs.validations.translations"})
    Optional<RSPWiseDocumentSetupEntity> findDetailedByRspIdAndDocument_Id(Long rspId, UUID documentId);
}
