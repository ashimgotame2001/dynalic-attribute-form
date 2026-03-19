package com.example.dynamicform.repository;

import com.example.dynamicform.entity.RSPRelatedAttributeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RSPRelatedAttributeRepository extends JpaRepository<RSPRelatedAttributeEntity, UUID> {
    List<RSPRelatedAttributeEntity> findByRspId(Long rspId);
    List<RSPRelatedAttributeEntity> findByRspIdAndFieldType(Long rspId, com.example.dynamicform.enums.DynamicFieldFor fieldType);
}
