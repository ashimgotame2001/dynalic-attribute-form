# How The Product Uses The Platform

## Purpose

The product layer uses the platform to avoid owning generic runtime concerns such as:

- dynamic relationship management
- generic JSON persistence
- schema compatibility
- validation registration
- audit handling
- cache-aware runtime data access

After the refactor, product code should depend on platform-facing ports and services, not on low-level platform internals or direct implementation classes.

## Current Usage Pattern

### Product controllers

#### `RelationshipController`

`RelationshipController` now uses:

- `RuntimeRelationshipPlatform`
- `RuntimeValidationPlatform`

This means the controller no longer needs to:

- depend directly on `RelationshipService`
- cast `ConstraintValidationEngine` to `ConstraintValidationEngineImpl`

The controller stays focused on HTTP behavior while the platform ports handle the runtime capabilities.

#### `PlatformRuntimeController`

This controller is already platform-first. It exposes product-usable runtime APIs for:

- saving entity data as JSON
- reading entity data with relationships
- linking and unlinking runtime relationships
- reading the runtime contract for an entity type

## Product services

### Product validation services

The product-specific validation services such as:

- `BaseProductValidationService`
- `RemittanceProductValidationService`

still define product behavior, but they are separate from runtime relationship and persistence infrastructure. They contribute product rules and metadata customization on top of the platform.

### Existing form services

Services like `FormConfigService` and `DocumentFormConfigService` still handle product metadata/configuration concerns. The platform complements them by providing:

- runtime metadata generation
- relationship discovery
- generic access to domain data

## Where product code now uses platform ports

### Relationship access

The following code paths now depend on `RuntimeRelationshipPlatform` instead of directly on `RelationshipService`:

- `RelationshipController`
- `MetadataInterpreterImpl`
- `DomainIntegrityValidationService`
- `DomainModelAccessorImpl`
- `PlatformRuntimeService`

This gives the product a stable interface even if the underlying relationship implementation changes later.

### Validation registration

The product now uses `RuntimeValidationPlatform` for:

- registering runtime validation rules
- registering declarative validation configs
- listing validation configs
- removing validation configs

That keeps product code independent from implementation details of `ConstraintValidationEngineImpl`.

## Example Flow

### Example 1: Product creates a runtime relationship

1. Product calls the relationship admin endpoint.
2. `RelationshipController` delegates to `RuntimeRelationshipPlatform`.
3. The platform adapter delegates to the current relationship service.
4. Relationship definitions and audit entries are stored.

From the product point of view, it only uses the platform-facing relationship port.

### Example 2: Product reads runtime-aware entity data

1. Product calls `PlatformRuntimeController`.
2. `PlatformRuntimeService` gets a `DomainModelAccessor` from `DomainModelAccessLayer`.
3. The accessor loads data, expands relationships, applies compatibility rules, and returns JSON.

The product does not need to know how persistence, relationship lookup, or audit are implemented.

### Example 3: Product registers validation rules

1. Product sends validation config requests to `RelationshipController`.
2. The controller delegates to `RuntimeValidationPlatform`.
3. The platform adapter passes them to the validation engine.

The product only sees a platform contract for runtime validation.

## Practical Rule For Product Code

When adding new product functionality:

- use `DomainModelAccessLayer` or `PlatformRuntimeService` for runtime entity data access
- use `RuntimeRelationshipPlatform` for relationship operations
- use `RuntimeValidationPlatform` for runtime validation registration
- avoid depending directly on relationship repositories or validation engine implementation classes

## Result

The product remains responsible for business-specific configuration and rules, while the platform owns the reusable runtime engine:

- metadata
- relationships
- persistence
- compatibility
- validation infrastructure
- audit
- cache

That separation keeps the product thinner and makes the platform reusable across more than one product.
