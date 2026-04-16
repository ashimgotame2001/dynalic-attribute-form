package com.example.dynamicform.platform.repository;

import com.example.dynamicform.platform.entity.RelationshipDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("platformRelationshipDefinitionRepository")
public interface RelationshipDefinitionRepository extends JpaRepository<RelationshipDefinitionEntity, Long> {

    Optional<RelationshipDefinitionEntity> findByRelationshipName(String relationshipName);

    List<RelationshipDefinitionEntity> findBySourceEntity(String sourceEntity);

    List<RelationshipDefinitionEntity> findByTargetEntity(String targetEntity);

    @Query("SELECT r FROM PlatformRelationshipDefinitionEntity r WHERE r.active = true")
    List<RelationshipDefinitionEntity> findAllActive();

    @Query("SELECT r FROM PlatformRelationshipDefinitionEntity r WHERE r.sourceEntity = :entity OR r.targetEntity = :entity")
    List<RelationshipDefinitionEntity> findByEntityInvolved(@Param("entity") String entity);
}
