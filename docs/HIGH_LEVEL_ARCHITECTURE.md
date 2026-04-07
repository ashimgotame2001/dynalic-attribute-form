# Platform Dynamic Relationships - High-Level Architecture

## Overview

The Platform for Dynamic Domain Model Relationships is a comprehensive system that enables runtime configuration and management of relationships between domain models without modifying existing code. It provides a generic persistence abstraction, interface-based access layer, backward compatibility management, and complete audit traceability.

---

## Core Architecture Principles

### 1. **No Model Modification**
- Existing domain models remain completely unchanged
- Relationships are stored separately in dedicated tables
- Runtime configuration without code changes

### 2. **Layered Architecture**
- Clean separation of concerns
- Interface-based design for extensibility
- Pluggable components

### 3. **Backward Compatibility**
- Schema versioning with automatic migration
- Data preservation across schema changes
- Default value provisioning

### 4. **Complete Audit Trail**
- All operations tracked with user and timestamp
- Old and new values preserved
- Compliance-ready traceability

---

## Architecture Layers

```
┌─────────────────────────────────────────────────────┐
│         Application Layer (Products/Solutions)      │
├─────────────────────────────────────────────────────┤
│  Uses DomainModelAccessor interface                │
└─────────────────────┬───────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────┐
│         Access Layer (Interface-Based)              │
│  • DomainModelAccessor (Interface)                  │
│  • DomainModelAccessorImpl (Implementation)         │
│  • DomainModelAccessorFactory (Factory)             │
└─────────────────────┬───────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────┐
│         Business Logic Layer                        │
│  • RelationshipService (CRUD Operations)            │
│  • RelationshipAuditService (Audit Logging)         │
│  • SchemaVersioningService (Version Management)     │
│  • BackwardCompatibilityService (Migration)         │
└─────────────────────┬───────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────┐
│         Persistence Abstraction Layer               │
│  • DomainModelPersistence (Interface)               │
│  • InMemoryDomainModelPersistence (Implementation)  │
└─────────────────────┬───────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────┐
│         Data Layer                                   │
│  • RelationshipDefinitionEntity                      │
│  • RelationshipInstanceEntity                        │
│  • RelationshipAuditEntity                           │
│  • Domain entity tables (unchanged)                  │
└─────────────────────────────────────────────────────┘
```

---

## How It Works: Step-by-Step Flow

### Step 1: Define a Relationship (Runtime Configuration)

**User Action**: Admin defines a relationship via REST API
```bash
POST /api/admin/relationships/definitions
{
  "relationshipName": "customer_has_transactions",
  "sourceEntity": "Customer",
  "targetEntity": "Transaction",
  "relationshipType": "ONE_TO_MANY",
  "sourceKeyField": "customerId",
  "targetKeyField": "customerId"
}
```

**System Flow**:
1. `RelationshipController` receives request
2. `RelationshipService.createRelationshipDefinition()` validates and saves
3. `RelationshipDefinitionEntity` stored in database
4. `RelationshipAuditService` logs the creation
5. Response returned to user

**Result**: Relationship is now available for use without any code changes

---

### Step 2: Create Relationship Instances (Link Data)

**User Action**: Application links two entities
```bash
POST /api/admin/relationships/instances
{
  "relationshipName": "customer_has_transactions",
  "sourceEntityId": "CUST-001",
  "targetEntityId": "TXN-12345"
}
```

**System Flow**:
1. `RelationshipController` receives request
2. `RelationshipService.createRelationshipInstance()` validates relationship exists
3. `RelationshipInstanceEntity` created and stored
4. `RelationshipAuditService` logs the linking operation
5. Response returned

**Result**: Entities are now linked through the relationship

---

### Step 3: Access Data with Relationships (Application Usage)

**Application Code**:
```java
// Get accessor for entity type
DomainModelAccessor<?> accessor = DomainModelAccessorFactory.getAccessor("Customer");

// Save customer data
accessor.save("CUST-001", customerData);

// Load customer with all relationships
var customerWithRelations = accessor.findWithRelationships("CUST-001");

// Get related transactions
var transactions = accessor.getRelated("CUST-001", "customer_has_transactions");
```

**System Flow**:
1. `DomainModelAccessorFactory` creates or returns accessor instance
2. `DomainModelAccessorImpl` delegates to `DomainModelPersistence` for data
3. `RelationshipService` fetches relationship instances for the entity
4. Data combined with relationship information
5. Enhanced data returned to application

**Result**: Application gets data with relationship context

---

### Step 4: Form Engine Integration (Automatic Enhancement)

**When Form is Requested**:
```bash
GET /api/form/Customer
```

**System Flow**:
1. `DynamicFormEngine.getFormDefinition()` called
2. `MetadataInterpreter.interpret()` processes metadata
3. **NEW**: `RelationshipService.getRelationshipDefinitionsByEntity()` fetches relationships
4. FormDefinition enhanced with relationships field
5. Form returned with relationship awareness

**Result**: Forms automatically know about available relationships

---

### Step 5: Schema Evolution (Backward Compatibility)

**When Schema Changes**:
- New field added to Customer entity
- Existing data needs migration

**System Flow**:
1. `SchemaVersioningService.registerSchema()` creates new version
2. `BackwardCompatibilityService.ensureCompatibility()` checks data
3. If needed, `migrateData()` transforms data to new version
4. `addDefaultsForNewRequiredFields()` provides defaults
5. Data saved in compatible format

**Result**: Existing applications continue working seamlessly

---

### Step 6: Audit Trail (Compliance & Debugging)

**All Operations Automatically Logged**:
- Relationship definition changes
- Relationship instance creation/deletion
- Data access patterns
- Schema version changes

**Query Audit**:
```bash
GET /api/audit/relationships/DEFINITION/1
```

**System Flow**:
1. `RelationshipAuditService` logs all operations
2. `RelationshipAuditEntity` stored with full context
3. `RelationshipAuditRepository` provides query capabilities
4. Audit data available for compliance and debugging

**Result**: Complete traceability of all relationship operations

---

## Key Components Deep Dive

### Relationship Management System

**Purpose**: Core business logic for relationship operations

**Components**:
- `RelationshipDefinitionEntity`: Metadata about relationships
- `RelationshipInstanceEntity`: Actual links between entities
- `RelationshipService`: CRUD operations and validation
- `RelationshipController`: REST API endpoints

**How It Works**:
1. Definitions stored separately from domain models
2. Instances created on-demand
3. Validation ensures referential integrity
4. All operations audited

### Generic Persistence Abstraction

**Purpose**: Hide persistence implementation details

**Interface**: `DomainModelPersistence<T>`
```java
interface DomainModelPersistence<T> {
    Map<String, Object> save(String entityId, String entityType, Map<String, Object> data);
    Optional<Map<String, Object>> findById(String entityId, String entityType);
    List<Map<String, Object>> findAll(String entityType);
    // ... more methods
}
```

**Implementation**: `InMemoryDomainModelPersistence`
- Thread-safe concurrent storage
- Nested data support (dot notation)
- Criteria-based querying
- Can be replaced with JDBC, MongoDB, etc.

**How It Works**:
1. Applications use interface
2. Implementation handles storage details
3. Easy to switch backends
4. Supports complex data structures

### Interface-Based Access Layer

**Purpose**: Provide clean, simple API for applications

**Interface**: `DomainModelAccessor<T>`
```java
interface DomainModelAccessor<T> {
    Map<String, Object> save(String entityId, Map<String, Object> data);
    Optional<Map<String, Object>> findWithRelationships(String entityId);
    List<Map<String, Object>> getRelated(String entityId, String relationshipName);
    // ... more methods
}
```

**Factory**: `DomainModelAccessorFactory`
- Creates accessor instances per entity type
- Manages accessor lifecycle
- Provides type-safe access

**How It Works**:
1. Applications get accessor from factory
2. Accessor transparently handles relationships
3. Persistence and relationship complexity hidden
4. Clean, intuitive API

### Backward Compatibility Management

**Purpose**: Ensure schema changes don't break existing applications

**Services**:
- `SchemaVersioningService`: Track schema versions
- `BackwardCompatibilityService`: Handle migrations

**Process**:
1. Schema changes registered as new versions
2. Data validated against target version
3. Automatic migration if needed
4. Defaults provided for new fields
5. Deprecation tracking

### Audit & Traceability System

**Purpose**: Complete audit trail for compliance and debugging

**Components**:
- `RelationshipAuditEntity`: Audit data storage
- `RelationshipAuditService`: Automatic logging
- `RelationshipAuditRepository`: Query audit data

**Tracked Operations**:
- CREATE, UPDATE, DELETE on definitions
- CREATE, DELETE on instances
- User ID and timestamp
- Old and new values (JSON)

---

## Data Flow Examples

### Example 1: Customer-Transaction Relationship

```
1. Define Relationship
   Customer (1) ──── ONE_TO_MANY ──── Transaction (N)

2. Create Instances
   CUST-001 ──── customer_has_transactions ──── TXN-123
   CUST-001 ──── customer_has_transactions ──── TXN-456

3. Query Related
   GET /related/Customer/CUST-001/customer_has_transactions
   → Returns [TXN-123, TXN-456]

4. Load with Relationships
   accessor.findWithRelationships("CUST-001")
   → Returns customer data + {"customer_has_transactions": [TXN-123, TXN-456]}
```

### Example 2: Schema Migration

```
Version 1: Customer {id, name, email}
Version 2: Customer {id, name, email, phone}

Process:
1. Register version 2 schema
2. Load existing customer data (version 1)
3. Migration detects missing "phone" field
4. Add default value: phone = ""
5. Save migrated data
6. Existing apps continue working
```

---

## Integration Points

### With Dynamic Form Engine
- Forms automatically include relationship information
- Relationship validation can be added to forms
- Form metadata enhanced with relationship context

### With Existing Applications
- No changes required to existing code
- New relationships available immediately
- Backward compatibility maintained

### With Database Systems
- JPA entities for relationship data
- Can use any JPA-compatible database
- Indices provided for performance

### With Security Systems
- Integrates with Spring Security
- Role-based access control
- Audit trail for compliance

---

## Performance Characteristics

### Time Complexity
- Relationship definition: O(1)
- Relationship instance creation: O(1)
- Query by relationship name: O(1) (indexed)
- Get all related entities: O(n) where n = instances
- Schema validation: O(m) where m = fields

### Scalability
- In-memory implementation: ~10K relationships
- Database implementation: ~1M+ relationships
- Thread-safe concurrent access
- Indexed queries for performance

### Caching
- Relationship definitions cached
- Form definitions cached
- Accessor instances cached per entity type

---

## Extensibility Points

### Custom Persistence
```java
@Component
public class JdbcDomainModelPersistence implements DomainModelPersistence<Map<String, Object>> {
    // Custom JDBC implementation
}
```

### Custom Accessor
```java
public class CustomerAccessor extends DomainModelAccessorImpl {
    // Customer-specific logic
}
```

### Custom Validation
```java
public class RelationshipValidationRule extends ValidationRule {
    // Custom relationship validation
}
```

### Custom Audit Handler
```java
public class ExternalAuditService extends RelationshipAuditService {
    // Send audit to external system
}
```

---

## Error Handling & Resilience

### Validation
- Relationship existence checked before operations
- Entity ID validation
- Data type validation
- Referential integrity maintained

### Error Recovery
- Transaction rollback on failures
- Partial failure handling
- Graceful degradation
- Error logging and monitoring

### Monitoring Points
- Relationship creation rates
- Query performance metrics
- Audit log volume
- Schema migration success rates

---

## Security Considerations

### Authentication
- All endpoints require authentication
- Spring Security integration
- Token-based access

### Authorization
- Role-based access control
- ADMIN role for relationship management
- Read-only access for applications

### Data Protection
- Audit trail for all operations
- Sensitive data encryption support
- Access logging

---

## Deployment Architecture

### Single Instance
```
Application Server
├── Platform Components
├── Relationship Services
├── Persistence Layer
└── Database
```

### Multi-Instance (Recommended)
```
Load Balancer
├── App Server 1
│   ├── Platform Components
│   └── Relationship Services
├── App Server 2
│   ├── Platform Components
│   └── Relationship Services
└── Database Cluster
    ├── Primary DB
    └── Read Replicas
```

### Microservices Option
```
API Gateway
├── Relationship Service
│   ├── Relationship Management
│   └── Audit Service
├── Persistence Service
│   ├── Domain Model Storage
│   └── Schema Versioning
└── Access Service
    ├── DomainModelAccessor
    └── Backward Compatibility
```

---

## Summary

The Platform for Dynamic Domain Model Relationships works by:

1. **Separating relationships from domain models** - No code changes needed
2. **Providing runtime configuration** - Define relationships via API
3. **Using layered architecture** - Clean separation of concerns
4. **Maintaining backward compatibility** - Automatic schema evolution
5. **Ensuring complete auditability** - Full traceability of changes
6. **Offering interface-based access** - Simple, clean API for applications

This architecture enables applications to dynamically manage complex relationships while maintaining data integrity, backward compatibility, and complete audit trails.

---

**Document Version**: 1.0  
**Date**: April 2026  
**Status**: Production Ready
