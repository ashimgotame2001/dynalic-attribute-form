package com.example.dynamicform.product.repository;

import com.example.dynamicform.product.entity.RelationshipDefinitionEntity;
import com.example.dynamicform.product.entity.RelationshipInstanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RelationshipInstanceRepository extends JpaRepository<RelationshipInstanceEntity, Long> {

    List<RelationshipInstanceEntity> findByRelationshipDefinition(RelationshipDefinitionEntity relationshipDefinition);

    List<RelationshipInstanceEntity> findByRelationshipDefinitionAndActive(RelationshipDefinitionEntity relationshipDefinition, boolean active);

    @Query("SELECT r FROM RelationshipInstanceEntity r WHERE r.relationshipDefinition.relationshipName = :relationshipName AND r.active = true")
    List<RelationshipInstanceEntity> findActiveByRelationshipName(@Param("relationshipName") String relationshipName);

    @Query("SELECT r FROM RelationshipInstanceEntity r WHERE r.sourceEntityId = :sourceId AND r.relationshipDefinition.sourceEntity = :sourceEntity AND r.active = true")
    List<RelationshipInstanceEntity> findActiveBySourceEntityId(@Param("sourceId") String sourceId, @Param("sourceEntity") String sourceEntity);

    @Query("SELECT r FROM RelationshipInstanceEntity r WHERE r.targetEntityId = :targetId AND r.relationshipDefinition.targetEntity = :targetEntity AND r.active = true")
    List<RelationshipInstanceEntity> findActiveByTargetEntityId(@Param("targetId") String targetId, @Param("targetEntity") String targetEntity);

    @Query("SELECT r FROM RelationshipInstanceEntity r WHERE r.sourceEntityId = :sourceId AND r.relationshipDefinition.relationshipName = :relationshipName AND r.active = true")
    List<RelationshipInstanceEntity> findActiveBySourceIdAndRelationship(@Param("sourceId") String sourceId, @Param("relationshipName") String relationshipName);
}
