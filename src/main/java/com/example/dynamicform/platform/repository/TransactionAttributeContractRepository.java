package com.example.dynamicform.platform.repository;

import com.example.dynamicform.platform.entity.TransactionAttributeContractEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionAttributeContractRepository extends JpaRepository<TransactionAttributeContractEntity, UUID> {

    Optional<TransactionAttributeContractEntity> findFirstByOrderByIdAsc();
}
