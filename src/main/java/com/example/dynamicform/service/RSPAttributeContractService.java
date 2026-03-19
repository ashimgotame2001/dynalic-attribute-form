package com.example.dynamicform.service;

import com.example.dynamicform.dto.RSPAttributeContractCreateRequest;
import com.example.dynamicform.dto.RSPAttributeContractRequest;
import com.example.dynamicform.dto.RSPAttributeContractResponse;
import com.example.dynamicform.entity.RSPAttributeContractEntity;
import com.example.dynamicform.repository.RSPAttributeContractRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class RSPAttributeContractService {

    private static final String STATIC_METADATA_PATH = "customer_static_metadata.json";

    private final RSPAttributeContractRepository repository;
    private final ObjectMapper objectMapper;

    public RSPAttributeContractService(RSPAttributeContractRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public List<RSPAttributeContractResponse> findAll() {
        return repository.findAll().stream()
                .flatMap(e -> toResponses(e).stream())
                .toList();
    }

    public List<RSPAttributeContractResponse> findByRspId(Long rspId) {
        return repository.findByRspId(rspId).stream()
                .flatMap(e -> toResponses(e).stream())
                .toList();
    }

    public List<RSPAttributeContractResponse> findByRspIdAndFieldType(Long rspId, com.example.dynamicform.enums.DynamicFieldFor fieldType) {
        return repository.findByRspIdAndFieldType(rspId, fieldType)
                .map(this::toResponses)
                .orElse(List.of());
    }

    public Optional<RSPAttributeContractResponse> findById(UUID id) {
        return repository.findById(id)
                .flatMap(e -> toResponses(e).stream().findFirst());
    }

    /**
     * Create multiple contracts for a single RSP using a list of referenceModels.
     */
    @Transactional
    public List<RSPAttributeContractResponse> create(RSPAttributeContractCreateRequest request) {
        Long rspId = request.getRspId();
        com.example.dynamicform.enums.DynamicFieldFor fieldType = request.getFieldType();
        List<String> refs = request.getReferenceModels();

        RSPAttributeContractEntity entity = repository.findByRspIdAndFieldType(rspId, fieldType)
                .orElseGet(() -> RSPAttributeContractEntity.builder().rspId(rspId).fieldType(fieldType).build());

        // Reset all flags to false, then enable only requested ones
        entity.setGender(false);
        entity.setNationality(false);
        entity.setResidingAlphaTwoCode(false);
        entity.setPostalCode(false);
        entity.setCity(false);
        entity.setAddressLine1(false);
        entity.setFirstName(false);
        entity.setMiddleName(false);
        entity.setLastName(false);
        entity.setDateOfBirth(false);
        entity.setContactNumber(false);
        entity.setEmail(false);
        entity.setSecret(false);
        entity.setReferralCode(false);

        if (refs != null) {
            for (String ref : refs) {
                switch (ref) {
                    case "individual/gender/id" -> entity.setGender(true);
                    case "individual/origin/alphaTwoCode" -> entity.setNationality(true);
                    case "individual/residingCountry/alphaTwoCode" -> entity.setResidingAlphaTwoCode(true);
                    case "individual/address/postalCodeInfo/postalCode" -> entity.setPostalCode(true);
                    case "individual/address/city" -> entity.setCity(true);
                    case "individual/address/addressLine1" -> entity.setAddressLine1(true);
                    case "individual/firstName" -> entity.setFirstName(true);
                    case "individual/middleName" -> entity.setMiddleName(true);
                    case "individual/lastName" -> entity.setLastName(true);
                    case "individual/dateOfBirth" -> entity.setDateOfBirth(true);
                    case "individual/contactNumber" -> entity.setContactNumber(true);
                    case "individual/email" -> entity.setEmail(true);
                    case "user/secret" -> entity.setSecret(true);
                    case "referral/referralCode" -> entity.setReferralCode(true);
                    default -> {
                        // ignore unknown referenceModels
                    }
                }
            }
        }

        RSPAttributeContractEntity saved = repository.save(entity);
        return toResponses(saved);
    }

    @Transactional
    public Optional<RSPAttributeContractResponse> update(UUID id, RSPAttributeContractRequest request) {
        // For column-based structure, prefer using create(rspId, refs) as an upsert.
        // This method keeps a minimal behavior: ensure rspId matches and return first enabled attribute.
        return repository.findById(id)
                .map(existing -> {
                    existing.setRspId(request.getRspId());
                    return repository.save(existing);
                })
                .flatMap(e -> toResponses(e).stream().findFirst());
    }

    @Transactional
    public void delete(UUID id) {
        repository.deleteById(id);
    }

    private List<RSPAttributeContractResponse> toResponses(RSPAttributeContractEntity entity) {
        List<RSPAttributeContractResponse> result = new ArrayList<>();

        addIfTrue(result, entity.isGender(), entity, "individual/gender/id");
        addIfTrue(result, entity.isNationality(), entity, "individual/origin/alphaTwoCode");
        addIfTrue(result, entity.isResidingAlphaTwoCode(), entity, "individual/residingCountry/alphaTwoCode");
        addIfTrue(result, entity.isPostalCode(), entity, "individual/address/postalCodeInfo/postalCode");
        addIfTrue(result, entity.isCity(), entity, "individual/address/city");
        addIfTrue(result, entity.isAddressLine1(), entity, "individual/address/addressLine1");
        addIfTrue(result, entity.isFirstName(), entity, "individual/firstName");
        addIfTrue(result, entity.isMiddleName(), entity, "individual/middleName");
        addIfTrue(result, entity.isLastName(), entity, "individual/lastName");
        addIfTrue(result, entity.isDateOfBirth(), entity, "individual/dateOfBirth");
        addIfTrue(result, entity.isContactNumber(), entity, "individual/contactNumber");
        addIfTrue(result, entity.isEmail(), entity, "individual/email");
        addIfTrue(result, entity.isSecret(), entity, "user/secret");
        addIfTrue(result, entity.isReferralCode(), entity, "referral/referralCode");

        return result;
    }

    private void addIfTrue(List<RSPAttributeContractResponse> target,
                           boolean flag,
                           RSPAttributeContractEntity entity,
                           String referenceModel) {
        if (!flag) {
            return;
        }
        target.add(
                RSPAttributeContractResponse.builder()
                        .id(entity.getId())
                        .rspId(entity.getRspId())
                        .fieldType(entity.getFieldType())
                        .referenceModel(referenceModel)
                        .label(resolveLabel(referenceModel))
                        .build()
        );
    }

    /**
     * Reads the customer_static_metadata.json file and resolves the label
     * for a given referenceModel. If no match is found, falls back to a
     * simple label derived from the last segment of the referenceModel.
     */
    @SuppressWarnings("unchecked")
    private String resolveLabel(String referenceModel) {
        if (referenceModel == null || referenceModel.isBlank()) {
            return "";
        }

        try (InputStream is = new ClassPathResource(STATIC_METADATA_PATH).getInputStream()) {
            List<Map<String, Object>> items =
                    objectMapper.readValue(is, new TypeReference<List<Map<String, Object>>>() {});

            for (Map<String, Object> item : items) {
                Object ref = item.get("referenceModel");
                if (referenceModel.equals(ref)) {
                    Object label = item.get("label");
                    if (label != null) {
                        return label.toString();
                    }
                }
            }
        } catch (IOException ignored) {
            // In case of any IO/parse issues, fall back to derived label
        }

        String[] parts = referenceModel.split("/");
        String last = parts[parts.length - 1];
        // Simple humanization: "alphaTwoCode" -> "Alpha Two Code"
        return last.replaceAll("([a-z])([A-Z])", "$1 $2")
                .replaceFirst("^[a-zA-Z]", last.substring(0, 1).toUpperCase() + last.substring(1));
    }
}

