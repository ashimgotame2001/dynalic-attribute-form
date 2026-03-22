package com.example.dynamicform.product.repository;

import com.example.dynamicform.product.entity.RSPWiseDocumentSetupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RSPWiseDocumentSetupRepository extends JpaRepository<RSPWiseDocumentSetupEntity, UUID> {
    List<RSPWiseDocumentSetupEntity> findByRspId(Long rspId);
}
