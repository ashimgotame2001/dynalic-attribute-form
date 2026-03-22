package com.example.dynamicform.product.repository;

import com.example.dynamicform.product.entity.TransactionRSPAttributeContractEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRSPAttributeContractRepository extends JpaRepository<TransactionRSPAttributeContractEntity, UUID> {
    Optional<TransactionRSPAttributeContractEntity> findByRspId(Long rspId);
}
