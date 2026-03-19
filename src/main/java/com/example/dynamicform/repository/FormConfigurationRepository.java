package com.example.dynamicform.repository;

import com.example.dynamicform.entity.FormConfigurationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FormConfigurationRepository extends JpaRepository<FormConfigurationEntity, java.util.UUID> {

    Optional<FormConfigurationEntity> findByFormNameAndVersion(String formName, Integer version);

    Optional<FormConfigurationEntity> findFirstByFormNameOrderByVersionDesc(String formName);

    List<FormConfigurationEntity> findByFormNameOrderByVersionDesc(String formName);

    boolean existsByFormName(String formName);

    @Query("SELECT f FROM FormConfigurationEntity f WHERE f.formName = :formName AND f.isActive = true ORDER BY f.version DESC")
    Optional<FormConfigurationEntity> findLatestActiveByFormName(String formName);
}
