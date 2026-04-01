package com.example.dynamicform.product.repository;

import com.example.dynamicform.product.entity.RelationshipAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RelationshipAuditRepository extends JpaRepository<RelationshipAuditEntity, Long> {

    List<RelationshipAuditEntity> findByEntityTypeAndEntityIdOrderByTimestampDesc(String entityType, String entityId);

    @Query("SELECT r FROM RelationshipAuditEntity r WHERE r.entityType = :entityType AND r.entityId = :entityId AND r.timestamp >= :since")
    List<RelationshipAuditEntity> findByEntityTypeAndEntityIdSince(@Param("entityType") String entityType,
                                                                  @Param("entityId") String entityId,
                                                                  @Param("since") LocalDateTime since);

    List<RelationshipAuditEntity> findByUserIdOrderByTimestampDesc(String userId);
}
