# Dynamic Form Engine - Remittance Platform

Metadata-driven dynamic form generation system for fintech applications. Supports country, payment mode, and provider-specific form configurations without redeployment.

## Architecture

### Clean Layers

- **controller** - REST API endpoints
- **service** - Business logic (FormService for runtime, FormLoaderService for metadata ingestion)
- **engine** - Core form building and validation logic
- **repository** - Data access layer
- **entity** - JPA entities
- **dto** - Data transfer objects
- **config** - Configuration components (startup loader)
- **util** - Utility classes

### Core Components

1. **MetadataInterpreter** - Parses existing JSON metadata (dynamicFieldAttributes.json) and converts to FormAttribute entities
2. **DynamicFormEngine** - Builds FormConfigResponse from FormDefinition including fields and UI components
3. **ValidationEngine** - Applies metadata-driven validation rules dynamically
4. **FormLoaderService** - Loads/updates form definitions from JSON into database
5. **FormLoaderInitializer** - Preloads common form configurations on startup

### Metadata Interpreter Layer

The system reads `src/main/resources/dynamicFieldAttributes.json` which contains the master list of available fields. This JSON structure is fixed and cannot be modified.

Example entry:
```json
{
  "field": "Email",
  "key": "primaryEmail",
  "fieldOrder": 17,
  "fieldType": "CUSTOMER"
}
```

The **MetadataInterpreter** maps these to internal FormAttribute entities with:
- `attributeName` = JSON `key`
- `attributeType` = derived from `fieldType` (default: input_text)
- `shortLabel` = JSON `field`
- `displayOrder` = JSON `fieldOrder`
- Auto-generated validation rules for common fields (email regex, phone format, required for core fields)

### Database Schema

```sql
form_definition (id, module_name, artifact_name, version, country, payment_mode, provider, created_at, updated_at)
form_attributes (id, form_id, attribute_name, attribute_type, visible, display_order, short_label, long_label, placeholder, default_value)
validation_rules (id, attribute_id, validation_type, validation_value, message, display_order)
dropdown_options (id, attribute_id, label, value, display_order)
ui_metadata (id, form_id, ui_component, display_order, short_label, long_label, attribute_name, visible)
```

## API Reference

### Runtime Endpoints

**Get Form Configuration**
```
GET /api/forms?module={module}&artifact={artifact}&country={country}&provider={provider}&paymentMode={paymentMode}
```

Example:
```
GET /api/forms?module=CustomerManagement&artifact=CustomerRegistration&country=US&provider=stripe&paymentMode=CREDIT_CARD
```

Response:
```json
{
  "moduleName": "Customer Management",
  "artifactName": "CustomerRegistration",
  "version": "1.0.0",
  "fields": [
    {
      "name": "email",
      "type": "input_text",
      "required": true,
      "visible": true,
      "shortLabel": "Email",
      "longLabel": "Enter your email address",
      "placeholder": "Enter email",
      "defaultValue": null,
      "displayOrder": 1,
      "validations": [
        {
          "type": "required",
          "value": null,
          "message": "Email is required",
          "displayOrder": 1
        },
        {
          "type": "regex",
          "value": "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
          "message": "Invalid email address",
          "displayOrder": 2
        }
      ],
      "options": null
    }
  ],
  "uiComponents": []
}
```

**Submit Form**
```
POST /api/forms/submit
Content-Type: application/json
```

Request body:
```json
{
  "module": "CustomerManagement",
  "artifact": "CustomerRegistration",
  "country": "US",
  "provider": "stripe",
  "paymentMode": "CREDIT_CARD",
  "data": {
    "email": "user@example.com",
    "firstName": "John"
  }
}
```

Success response: `{"message":"Form submitted successfully"}`

Validation error response:
```json
[
  {
    "field": "email",
    "message": "Email is required"
  }
]
```

### Metadata Management Endpoints

**Load Form Definition from JSON**
```
POST /api/forms/loader/load
```

Parameters:
- `module` (required) - Module name
- `artifact` (required) - Artifact name
- `version` (optional, default: 1.0.0) - Version
- `country` (required) - Country code (e.g., US, NP, IN)
- `paymentMode` (optional) - Payment mode (e.g., BANK_TRANSFER, WALLET, CARD)
- `provider` (optional) - Provider name (e.g., stripe, ime)
- `jsonPath` (optional, default: dynamicFieldAttributes.json) - Classpath resource path
- `fieldKeys` (optional) - Comma-separated list of field keys to include from the JSON

Example:
```
POST /api/forms/loader/load?module=Remittance&artifact=BankTransfer&country=NP&paymentMode=BANK_TRANSFER&fieldKeys=primaryEmail,address,bankAccountNumber,bankName
```

### Dynamic Field Catalog

The system reads `src/main/resources/dynamicFieldAttributes.json` which contains all available fields. All fields have a `key` that uniquely identifies them.

Example entries:
```json
[
  {
    "field": "Email",
    "key": "primaryEmail",
    "fieldOrder": 17,
    "fieldType": "CUSTOMER"
  },
  {
    "field": "Mobile Number",
    "key": "primaryMobileNo",
    "fieldOrder": 15,
    "fieldType": "BOTH"
  }
]
```

### Supported Field Types (mapped from JSON)

- `input_text` (default) - Text input
- `input_number` - Numeric input (detected from field name containing "Number", "Amount", etc.)
- `textarea` - Multiline text (detected from field name containing "Address", "Remarks")
- `dropdown` - Dropdown selection (must be configured separately with options)
- `date` - Date picker (detected from field name containing "Date", "Birth")
- `checkbox` - Checkbox input

### Automatic Validation Rules

The MetadataInterpreter generates sensible defaults:

- **Email fields** (containing "email") - Required + regex validation for email format
- **Mobile number fields** (containing "MobileNo") - Required + regex for 10-15 digits
- **Name fields** (firstName, lastName) - Required
- **Bank fields** (bankAccountNumber, bankCode) - Required + numeric validation

### Extending with Dropdown Options

Dropdown fields require options to be populated separately via database insert:

```sql
INSERT INTO dropdown_options (attribute_id, label, value, display_order) 
VALUES (?, 'Male', 'M', 1);
```

Future versions will include an admin API for managing dropdown options.

## Startup Preloading

The `FormLoaderInitializer` runs on startup (with `default` profile) and creates these default configurations:

1. **CustomerRegistration** (GLOBAL) - All customer fields
2. **BankTransfer** (NP, BANK_TRANSFER) - Customer fields + bank fields
3. **WalletTopup** (IN, WALLET) - Customer fields + mobile + wallet
4. **CardPayment** (US, CARD, stripe) - Customer fields + email

To customize startup loading, edit `FormLoaderInitializer.java`.

## How to Use

### 1. Build and Run

```bash
./gradlew bootRun
```

The application starts on port 8080 and auto-creates the database schema via Flyway.

### 2. Load a New Form Configuration

```bash
curl -X POST "http://localhost:8080/api/forms/loader/load?module=Remittance&artifact=BankTransfer&country=NP&paymentMode=BANK_TRANSFER&fieldKeys=primaryEmail,address,bankAccountNumber,bankName"
```

### 3. Fetch Form Configuration

```bash
curl "http://localhost:8080/api/forms?module=Remittance&artifact=BankTransfer&country=NP&paymentMode=BANK_TRANSFER&provider="
```

### 4. Submit Form Data

```bash
curl -X POST "http://localhost:8080/api/forms/submit" \
  -H "Content-Type: application/json" \
  -d '{
    "module": "Remittance",
    "artifact": "BankTransfer",
    "country": "NP",
    "paymentMode": "BANK_TRANSFORM",
    "provider": "",
    "data": {
      "primaryEmail": "user@example.com",
      "bankAccountNumber": "1234567890"
    }
  }'
```

## Customization

### Adding New Validation Rules

Extend `MetadataInterpreter.createDefaultValidationRules()` to add custom logic based on attribute name, type, or metadata.

### Supporting New Field Types

Update `MetadataInterpreter.mapFieldTypeToAttributeType()` to map JSON `fieldType` to UI types like `dropdown`, `radio`, etc.

### Advanced UI Components

UI metadata can be added separately to `ui_metadata` table for titles, sections, etc.

## Database

- **H2 in-memory** - Development (default)
- **PostgreSQL** - Production (update application.yml)

The schema is automatically migrated by Flyway on startup.

## Testing

Unit tests should cover:
- MetadataInterpreter parsing accuracy
- DynamicFormEngine building correct DTOs
- ValidationEngine rule application
- FormLoaderService CRUD operations
- FormController API contracts

Use `@DataJpaTest` for repository tests and `@SpringBootTest` for integration tests.

## Notes

- The JSON metadata is read-only and shared across services
- Form definitions are cached in memory via JPA/Hibernate (second-level cache optional)
- Validation is stateless and thread-safe
- All timestamps are in UTC

## Future Enhancements

- Admin UI for form builder
- Conditional field visibility (show/hide based on other field values)
- Versioning of form definitions
- A/B testing support
- Internationalization of labels
- Audit trail for changes