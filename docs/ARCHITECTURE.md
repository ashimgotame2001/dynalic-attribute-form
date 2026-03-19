# Metadata-Driven Dynamic Form System - Architecture

## 1. Overview

The system is a Spring Boot 3+ backend that generates dynamic forms based on JSON metadata stored in a database. It supports:

- Dynamic form rendering
- Nested objects and collections
- Runtime validation
- DTO mapping

Designed for fintech platforms requiring compliance with changing form requirements without code deployments.

## 2. Architectural Layers

### 2.1 Presentation Layer
- `FormConfigController` - Admin APIs for managing form configurations
- `FormRenderController` - Public APIs for form retrieval and submission
- `GlobalExceptionHandler` - Centralized error handling

### 2.2 Service Layer
- `FormConfigService` - CRUD operations for form metadata configurations
- `DynamicFormEngine` - Core engine that coordinates form generation, validation, and mapping
- `MetadataInterpreter` - Converts raw JSON to internal model
- `ValidationEngine` - Executes dynamic validation rules

### 2.3 Persistence Layer
- `FormConfigurationEntity` - JPA entity storing form metadata JSON and versioning
- `FormConfigurationRepository` - Spring Data JPA repository

### 2.4 DTO Packages
- `dto.metadata.*` - Raw JSON structure (immutable, matches external contract)
- `dto.*` - Internal form model (FormDefinition, FieldDefinition, ValidationDefinition)
- `dto` - API request/response contracts

### 2.5 Utilities
- `MetadataUtils` - String conversions, type inference
- `CacheConfig` - Spring Cache configuration

## 3. Core Components

### 3.1 MetadataInterpreter

Responsible for parsing the immutable JSON structure and transforming it into an internal, enriched model.

Input: `RawFormMetadata` (deserialized from JSON)
Output: `FormDefinition` (internal representation)

Key logic:
- Recursively processes `domainModel`
- Maps `modelName` to data types
- Detects nested objects (`reference=true`) and collections (`collection=true`)
- Generates UI labels from attribute names
- Converts raw validation rules to `ValidationDefinition`

### 3.2 DynamicFormEngine

The facade that uses the interpreter and validation engine.

Main operations:
- `getFormDefinition(formName)` - Reads from DB, interprets, caches result
- `validate(formName, data)` - Validates submitted JSON data
- `validateAndMap(formName, data)` - Validates and maps to target DTO
- `formExists(formName)` - Checks existence

Caching: `@Cacheable("formDefinitions")` on `getFormDefinition` to avoid repeated DB reads.

### 3.3 ValidationEngine

Evaluates validation rules defined in the metadata.

Supported rule types:

| Type        | Applies To       | Parameter Key | Description |
|-------------|------------------|---------------|-------------|
| required    | any              | N/A           | Value must be present and non-blank |
| regex       | String           | pattern       | Must match regex |
| min         | Number, String, Date | value     | Minimum value or length |
| max         | Number, String, Date | value     | Maximum value or length |
| dateFormat  | String           | pattern       | Date string must match pattern |
| allowFuture | LocalDate, LocalDateTime | N/A | Date must not be in future |
| allowPast   | LocalDate, LocalDateTime | N/A | Date must not be in past |

Validation is performed recursively to handle nested structures and collections.

### 3.4 ValidationError

Represents a field-level error with:
- `fieldPath` - slash notation (e.g., `individual/address/postalCode`)
- `message`
- `validationType`
- `invalidValue` (optional)



## 4. Dynamic DTO Mapping Strategy

The `target_dto_class_name` field in `FormConfigurationEntity` stores the Fully Qualified Class Name (FQCN) of the DTO that submitted data should be mapped to. At runtime:

1. `DynamicFormEngine` loads the `Class` using `Class.forName()`
2. After passing validation, Jackson `ObjectMapper.convertValue(data, targetClass)` maps the `Map<String,Object>` to the DTO instance.
3. The DTO must have a no-arg constructor and setters (or be a record) for Jackson to populate.

This allows the system to generate objects that exactly match existing domain models without hardcoding.

## 6. JSON Metadata Structure (Immutable)

```json
{
  "modelName": "RegisterCustomerRequest",
  "domainModel": [
    {
      "modelName": "String",
      "attributeName": "firstName",
      "visible": true,
      "validations": [
        { "type": "required" },
        { "type": "regex", "parameters": { "pattern": "^[A-Za-z]+$" } }
      ]
    },
    {
      "modelName": "Individual",
      "attributeName": "individual",
      "reference": true,
      "visible": true,
      "domainModel": [
        {
          "modelName": "String",
          "attributeName": "dateOfBirth",
          "visible": true,
          "validations": [
            { "type": "required" },
            { "type": "dateFormat", "parameters": { "pattern": "dd/MM/yyyy" } },
            { "type": "allowFuture", "parameters": {} }
          ]
        },
        {
          "modelName": "Address",
          "attributeName": "address",
          "reference": true,
          "collection": true,
          "visible": true,
          "domainModel": [
            {
              "modelName": "String",
              "attributeName": "postalCode",
              "visible": true,
              "validations": [ { "type": "required" } ]
            }
          ]
        }
      ]
    }
  ]
}
```

The interpreter builds a tree of `FieldDefinition` objects.

## 7. API Endpoints

### Configuration Management (Admin)

```
POST   /api/config/forms              - Create or update configuration
GET    /api/config/forms/{formName}   - Retrieve configuration (latest or by version)
GET    /api/config/forms              - List all configurations
DELETE /api/config/forms/{formName}?version=1 - Delete specific version
```

### Form Rendering & Submission

```
GET    /api/forms/{formName}           - Get form definition for frontend
POST   /api/forms/{formName}/submit   - Submit form data (validation + mapping)
POST   /api/forms/{formName}/validate - Validate without submitting
GET    /api/forms/{formName}/exists   - Check if form exists
```

### Example Response (GET /api/forms/{formName})

```json
{
  "formName": "customer-registration",
  "version": 1,
  "description": "Customer registration form",
  "targetDtoClassName": "com.example.dynamicform.model.RegisterCustomerRequest",
  "isActive": true,
  "metadata": {
    "modelName": "customer-registration",
    "domainModel": [
      {
        "modelName": "User",
        "attributeName": "user",
        "reference": true,
        "collection": false,
        "visible": true,
        "domainModel": [
          {
            "modelName": "String",
            "attributeName": "secret",
            "reference": false,
            "collection": false,
            "visible": true,
            "validations": [
              {
                "required": {
                  "message": "Secret is required.",
                  "value": true
                }
              }
            ]
          }
        ]
      },
      {
        "modelName": "Individual",
        "attributeName": "individual",
        "reference": true,
        "collection": false,
        "visible": true,
        "domainModel": [
          {
            "modelName": "String",
            "attributeName": "firstName",
            "reference": false,
            "collection": false,
            "visible": true,
            "validations": [
              {
                "required": {
                  "message": "First name is required.",
                  "value": true
                }
              },
              {
                "regex": {
                  "pattern": "^[A-Za-z]+$",
                  "message": "First name must contain only letters."
                }
              }
            ]
          },
          {
            "modelName": "String",
            "attributeName": "lastName",
            "reference": false,
            "collection": false,
            "visible": true,
            "validations": [
              {
                "required": {
                  "message": "Last name is required.",
                  "value": true
                }
              }
            ]
          },
          {
            "modelName": "LocalDate",
            "attributeName": "dateOfBirth",
            "reference": false,
            "collection": false,
            "visible": true,
            "validations": [
              {
                "required": {
                  "message": "Date of birth is required.",
                  "value": true
                }
              },
              {
                "futureDate": {
                  "message": "Date of birth cannot be in the future.",
                  "value": false
                }
              }
            ]
          },
          {
            "modelName": "Gender",
            "attributeName": "gender",
            "reference": true,
            "collection": false,
            "visible": true,
            "domainModel": [
              {
                "modelName": "Long",
                "attributeName": "id",
                "reference": false,
                "collection": false,
                "visible": true
              }
            ]
          },
          {
            "modelName": "Address",
            "attributeName": "address",
            "reference": true,
            "collection": false,
            "visible": true,
            "domainModel": [
              {
                "modelName": "PostalCodeInfo",
                "attributeName": "postalCodeInfo",
                "reference": true,
                "collection": false,
                "visible": true,
                "domainModel": [
                  {
                    "modelName": "String",
                    "attributeName": "postalCode",
                    "reference": false,
                    "collection": false,
                    "visible": true,
                    "validations": [
                      {
                        "required": {
                          "message": "Postal code is required.",
                          "value": true
                        }
                      },
                      {
                        "regex": {
                          "pattern": "^\\d{5}$",
                          "message": "Postal code must be exactly 5 digits."
                        }
                      }
                    ]
                  }
                ]
              },
              {
                "modelName": "String",
                "attributeName": "city",
                "reference": false,
                "collection": false,
                "visible": true
              },
              {
                "modelName": "String",
                "attributeName": "addressLine1",
                "reference": false,
                "collection": false,
                "visible": true,
                "validations": [
                  {
                    "required": {
                      "message": "Address line 1 is required.",
                      "value": true
                    }
                  }
                ]
              }
            ]
          },
          {
            "modelName": "String",
            "attributeName": "email",
            "reference": false,
            "collection": false,
            "visible": true,
            "validations": [
              {
                "required": {
                  "message": "Email is required.",
                  "value": true
                }
              },
              {
                "regex": {
                  "pattern": "^[^@]+@[^@]+\\.[^@]+$",
                  "message": "Email must be valid."
                }
              }
            ]
          },
          {
            "modelName": "String",
            "attributeName": "contactNumber",
            "reference": false,
            "collection": false,
            "visible": true,
            "validations": [
              {
                "required": {
                  "message": "Contact number is required.",
                  "value": true
                }
              }
            ]
          }
        ]
      }
    ]
  },
  "createdAt": "2026-03-11T12:01:53.932575",
  "updatedAt": "2026-03-11T12:01:53.932614"
}
```

### Example Submission

Request body to `/api/forms/register-customer/submit`:

```json
{
  "formName": "customer-registration",
  "description": "Customer registration form",
  "targetDtoClassName": "com.example.dynamicform.model.RegisterCustomerRequest",
  "fields": [
    {
      "modelName": "user.secret",
      "visible": true,
      "shortLabel": "Secret",
      "longLabel": "Enter secret password",
      "validations": [
        {
          "type": "required",
          "value": true,
          "message": "Secret is required."
        }
      ]
    },
    {
      "modelName": "individual.firstName",
      "visible": true,
      "shortLabel": "First Name",
      "longLabel": "Enter first name",
      "validations": [
        {
          "type": "required",
          "value": true,
          "message": "First name is required."
        },
        {
          "type": "regex",
          "pattern": "^[A-Za-z]+$",
          "message": "First name must contain only letters."
        }
      ]
    },
    {
      "modelName": "individual.lastName",
      "visible": true,
      "shortLabel": "Last Name",
      "longLabel": "Enter last name",
      "validations": [
        {
          "type": "required",
          "value": true,
          "message": "Last name is required."
        }
      ]
    },
    {
      "modelName": "individual.dateOfBirth",
      "visible": true,
      "shortLabel": "Date of Birth",
      "longLabel": "Enter date of birth (dd/MM/yyyy)",
      "validations": [
        {
          "type": "required",
          "value": true,
          "message": "Date of birth is required."
        },
        {
          "type": "futureDate",
          "value": false,
          "message": "Date of birth cannot be in the future."
        }
      ]
    },
    {
      "modelName": "individual.gender.id",
      "visible": true,
      "shortLabel": "Gender",
      "longLabel": "Select gender"
    },
    {
      "modelName": "individual.address.postalCodeInfo.postalCode",
      "visible": true,
      "shortLabel": "Postal Code",
      "longLabel": "Enter postal code",
      "validations": [
        {
          "type": "required",
          "value": true,
          "message": "Postal code is required."
        },
        {
          "type": "regex",
          "pattern": "^\\d{5}$",
          "message": "Postal code must be exactly 5 digits."
        }
      ]
    },
    {
      "modelName": "individual.address.city",
      "visible": true,
      "shortLabel": "City",
      "longLabel": "Enter city"
    },

    {
      "modelName": "individual.address.addressLine1",
      "visible": true,
      "shortLabel": "Address Line 1",
      "longLabel": "Enter address line 1",
      "validations": [
        {
          "type": "required",
          "value": true,
          "message": "Address line 1 is required."
        }
      ]
    },
    {
      "modelName": "individual.email",
      "visible": true,
      "shortLabel": "Email",
      "longLabel": "Enter email address",
      "validations": [
        {
          "type": "required",
          "value": true,
          "message": "Email is required."
        },
        {
          "type": "regex",
          "pattern": "^[^@]+@[^@]+\\.[^@]+$",
          "message": "Email must be valid."
        }
      ]
    },
    {
      "modelName": "individual.contactNumber",
      "visible": true,
      "shortLabel": "Contact Number",
      "longLabel": "Enter contact number",
      "validations": [
        {
          "type": "required",
          "value": true,
          "message": "Contact number is required."
        }
      ]
    }
  ]
}
```

If validation passes, returns 200 with success message. If fails, returns 400 with array of `ValidationError`.

## 8. Caching Strategy

- Form definitions are cached using Spring Cache (`ConcurrentMapCacheManager` by default).
- Cache key: form name.
- Cache eviction occurs when a configuration is created/updated/deleted via `@CacheEvict` on service methods.
- For production, replace with Redis or Hazelcast distributed cache.

## 9. Error Handling

- `FormNotFoundException` -> 404 with list containing one ValidationError
- `DynamicValidationException` -> 400 with list of all validation errors
- Generic `Exception` -> 500 with generic error message

All errors follow same JSON structure: `[{ "fieldPath": "...", "message": "...", "validationType": "..." }]`.

## 10. Extensibility Points

- **New validation types**: Add to `ValidationType` enum and implement logic in `ValidationEngineImpl`.
- **Custom UI components**: Extend `UiMetadata` with new properties; frontend can interpret.
- **Versioning**: Store multiple versions; fetch by version number.
- **Other persistence**: Replace JPA repository with Mongo or custom storage without affecting engine.
- **Internationalization**: Add i18n message codes in metadata and resolve in UI.

## 11. Production Considerations

- Use PostgreSQL with JSONB column for efficient storage and optional querying.
- Add Spring Security to secure admin endpoints.
- Add Swagger/OpenAPI documentation.
- Add metrics (Micrometer) for cache hits/misses, validation failures.
- Add audit logging for configuration changes.
- Implement optimistic locking (already present via @Version).
- Use MapStruct for DTO mapping to reduce boilerplate (optional but recommended).
- Enable SQL statement logging for debugging.

## 12. Class Diagram (Simplified)

```
[FormConfigurationEntity] -- (JPA) --> [FormConfigRepository]
        |
        | metadataJson
        v
[RawFormMetadata] --(interpreted by)--> [MetadataInterpreter] --(produces)--> [FormDefinition]
                                                                        |
                                                                        | used by
                                                                        v
                                                              [DynamicFormEngine]
                                                                        |
                                                                        | validates using
                                                                        v
                                                               [ValidationEngine]
                                                                        |
                                                                        | checks
                                                                        v
                                                             [FieldDefinition + ValidationDefinition]
```

## 13. Deployment

- Build: `./gradlew bootJar`
- Run: `java -jar build/libs/app.jar`
- Environment variables: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
- For Redis: add `spring.redis.host`, etc.

## 14. Testing

- Unit tests for `MetadataInterpreter` with various JSON inputs.
- Unit tests for `ValidationEngine` covering each validation type.
- Integration tests for API endpoints using MockMvc or TestRestTemplate.
- Property-based tests for nested collections.

## 15. Future Enhancements

- Conditional field visibility (based on other field values)
- Custom validators (scriptable)
- Form sections/pages
- Multi-step forms
- Client-side validation generation
- Form analytics (track field completion times)

---

This architecture decouples form structure from code, enabling non-developers (product, ops) to modify forms via UI or config management tools, while maintaining type-safe DTO mapping.
