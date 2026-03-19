- **Customer Registration Dynamic Fields**:
  - Additional KYC fields (e.g., proof of address) that vary based on RSP

- **Beneficiary Registration Examples**:
  - Bank account details fields that change based on beneficiary country (e.g., IBAN for EU countries, routing number for US)
  - Mobile money fields for countries with prevalent mobile payment systems (e.g., M-Pesa in Kenya)

- **Country-Wise Dynamic Fields for Beneficiary Registration**:
  - Regulatory compliance fields differing by jurisdiction (e.g., FATCA fields for US-based transactions)
  - Currency-specific validation rules and field formats
  - Document upload fields that require both front and back copies for certain countries (e.g., passport or ID card in specific jurisdictions)
  - Document expiry date validation rules that differ by country (e.g., varying minimum validity periods before expiry)

- **Country + Payment Mode Wise Setup**:
  - For wire transfers: SWIFT code and intermediary bank details
  - For mobile money: Phone number and operator selection
  - For cash pickup: Agent network selection and pickup location fields

- **Country + Payment Mode + Payment Agent Based Setup**:
  - Agent-specific fields
  - Custom validation rules per agent 
  - Additional security questions or OTP requirements varying by agent risk profile
