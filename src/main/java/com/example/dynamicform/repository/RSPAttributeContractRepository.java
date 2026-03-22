package com.example.dynamicform.repository;

import com.example.dynamicform.entity.CustomerRSPAttributeContractEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RSPAttributeContractRepository extends JpaRepository<CustomerRSPAttributeContractEntity, UUID> {

    List<CustomerRSPAttributeContractEntity> findByRspId(Long rspId);

}

