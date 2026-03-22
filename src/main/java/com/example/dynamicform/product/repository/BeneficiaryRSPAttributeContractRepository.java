package com.example.dynamicform.product.repository;

import com.example.dynamicform.product.entity.BeneficiaryRSPAttributeContractEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BeneficiaryRSPAttributeContractRepository extends JpaRepository<BeneficiaryRSPAttributeContractEntity, UUID> {
    Optional<BeneficiaryRSPAttributeContractEntity> findByRspId(Long rspId);
}
