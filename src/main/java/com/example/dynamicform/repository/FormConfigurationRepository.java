package com.example.dynamicform.repository;

import com.example.dynamicform.entity.CustomerFormConfigurationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FormConfigurationRepository extends JpaRepository<CustomerFormConfigurationEntity, java.util.UUID> {

    Optional<CustomerFormConfigurationEntity> findByFormNameAndVersion(String formName, Integer version);

    Optional<CustomerFormConfigurationEntity> findFirstByFormNameOrderByVersionDesc(String formName);

    List<CustomerFormConfigurationEntity> findByFormNameOrderByVersionDesc(String formName);

    boolean existsByFormName(String formName);

    @Query("SELECT f FROM CustomerFormConfigurationEntity f WHERE f.formName = :formName AND f.isActive = true ORDER BY f.version DESC")
    Optional<CustomerFormConfigurationEntity> findLatestActiveByFormName(String formName);
}
