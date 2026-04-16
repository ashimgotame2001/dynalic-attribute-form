package com.example.dynamicform.platform.repository;

import com.example.dynamicform.platform.entity.BeneficiaryAttributeContractEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BeneficiaryAttributeContractRepository extends JpaRepository<BeneficiaryAttributeContractEntity, UUID> {

    Optional<BeneficiaryAttributeContractEntity> findFirstByOrderByIdAsc();
}
