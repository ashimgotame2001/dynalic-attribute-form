package com.example.dynamicform.repository;

import com.example.dynamicform.entity.RSPAttributeContractEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RSPAttributeContractRepository extends JpaRepository<RSPAttributeContractEntity, UUID> {

    List<RSPAttributeContractEntity> findByRspId(Long rspId);

    Optional<RSPAttributeContractEntity> findByRspIdAndFieldType(Long rspId, com.example.dynamicform.enums.DynamicFieldFor fieldType);
}

