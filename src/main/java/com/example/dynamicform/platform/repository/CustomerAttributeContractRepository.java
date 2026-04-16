package com.example.dynamicform.platform.repository;

import com.example.dynamicform.platform.entity.CustomerAttributeContractEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerAttributeContractRepository extends JpaRepository<CustomerAttributeContractEntity, UUID> {

    Optional<CustomerAttributeContractEntity> findFirstByOrderByIdAsc();
}
