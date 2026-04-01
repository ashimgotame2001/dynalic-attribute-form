# How The Platform Works

## Purpose

The platform provides runtime capabilities for:

- generating JSON-oriented metadata from existing domain models
- defining relationships dynamically without changing those models
- persisting nested domain data through a generic abstraction
- enforcing runtime validation and compatibility rules
- auditing schema, relationship, and data changes
- caching metadata, relationships, and frequently accessed data

The key design rule is: existing product domain models stay unchanged, and the platform manages the dynamic behavior around them.

## Main Building Blocks

### Runtime metadata

- `RuntimeMetadataGenerator`
- `DynamicMetadataBuildService`
- `MetadataInterpreter`

These components inspect existing model classes and build metadata that the rest of the system can use for forms and runtime contracts.

### Generic persistence

- `DomainModelPersistence`
- `InMemoryDomainModelPersistence`

This layer stores JSON-like `Map<String, Object>` payloads for any entity type. It supports nested objects and nested-path lookup.

### Interface-based access layer

- `DomainModelAccessLayer`
- `DomainModelAccessor`
- `DomainModelAccessorImpl`

This is the main platform entry point for working with domain model data. It hides persistence details, relationship expansion, compatibility logic, validation, and audit logging.

### Runtime relationship management

- `RuntimeRelationshipPlatform`
- `RuntimeRelationshipPlatformAdapter`
- `RelationshipService`

The platform exposes relationship operations through a platform-facing port. Internally, the current implementation delegates to the relationship service and its repositories.

### Runtime validation

- `RuntimeValidationPlatform`
- `RuntimeValidationPlatformAdapter`
- `ConstraintValidationEngine`
- `DomainIntegrityValidationService`

The validation flow has two levels:

- generic constraint validation
- tighter domain integrity validation for relationship and business-rule checks

### Backward compatibility and versioning

- `SchemaVersioningService`
- `BackwardCompatibilityService`

When data is saved through the platform access layer, schema version information is checked and the payload is adjusted to remain compatible with the latest registered version.

### Audit and traceability

- `RelationshipAuditService`
- `RelationshipAuditEntity`

Audit is used not only for relationship changes, but also for schema changes and generic data changes made through the platform access layer.

### Cache

Configured cache regions:

- `formDefinitions`
- `metadataDefinitions`
- `relationshipDefinitions`
- `relationshipInstances`
- `domainModelData`
- `schemaVersions`
- `auditTrail`
- `validationConfigs`

## Runtime Flow

### 1. Metadata generation

1. A caller asks for metadata for a domain class.
2. `RuntimeMetadataGenerator` builds raw metadata from the class.
3. The metadata is cached in `metadataDefinitions`.
4. The result is returned as JSON-ready metadata.

### 2. Domain data save

1. A caller requests an accessor from `DomainModelAccessLayer` for an entity type.
2. `DomainModelAccessorImpl.save()` receives the payload.
3. Compatibility is applied using `SchemaVersioningService` and `BackwardCompatibilityService`.
4. Constraint validation and domain integrity validation run.
5. The payload is stored through `DomainModelPersistence`.
6. A data audit entry is written.
7. Relevant cache entries are evicted.

### 3. Relationship-aware read

1. A caller loads an entity through `findWithRelationships`.
2. The base entity data is loaded from generic persistence.
3. Runtime relationship definitions and instances are fetched through `RuntimeRelationshipPlatform`.
4. The related data is attached under `_relationships`.
5. The response is returned as a stable JSON structure.

### 4. Relationship definition and linking

1. Admin APIs create or update relationship definitions at runtime.
2. Relationship instances link source and target records.
3. Those definitions are later used by accessors, metadata interpretation, and domain validation.
4. All changes are audited.

## JSON-First Runtime API

The platform exposes a JSON-oriented runtime contract through:

- `PlatformRuntimeController`
- `PlatformRuntimeService`

Important endpoints:

- `GET /api/platform/runtime/contract/{entityType}`
- `PUT /api/platform/runtime/entities/{entityType}/{entityId}`
- `GET /api/platform/runtime/entities/{entityType}/{entityId}`
- relationship link and unlink endpoints under `/api/platform/runtime/entities/.../relationships/...`

These endpoints are intended to present the platform as a runtime JSON engine rather than a model-specific implementation.

## Dependency Direction

The intended dependency direction is:

1. product layer depends on platform ports
2. platform ports adapt to current implementations
3. persistence, audit, validation, and relationship storage stay behind the platform boundary

That keeps product code simpler and prevents it from owning platform concerns directly.
