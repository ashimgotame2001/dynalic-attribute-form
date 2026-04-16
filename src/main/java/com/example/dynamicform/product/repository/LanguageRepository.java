package com.example.dynamicform.product.repository;

import com.example.dynamicform.product.entity.LanguageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LanguageRepository extends JpaRepository<LanguageEntity, Long> {
    Optional<LanguageEntity> findByCode(String code);
    Optional<LanguageEntity> findByIdAndActiveTrue(Long id);
}
