package com.example.dynamicform.product.repository;

import com.example.dynamicform.product.entity.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity, UUID> {
    Optional<DocumentEntity> findByDocumentName(String documentName);
    boolean existsByDocumentName(String documentName);
}
