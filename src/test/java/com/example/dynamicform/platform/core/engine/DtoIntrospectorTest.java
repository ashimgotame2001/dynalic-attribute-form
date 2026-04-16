package com.example.dynamicform.platform.core.engine;

import com.example.dynamicform.platform.api.dto.FieldSpecRequest;
import com.example.dynamicform.platform.api.dto.metadata.RawFormMetadata;
import com.example.dynamicform.product.model.RegisterCustomerRequest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DtoIntrospectorTest {

    private final DtoIntrospector dtoIntrospector = new DtoIntrospector();

    @Test
    void testBuildMetadata_WithNestedAndCustomFields() {
        List<FieldSpecRequest.FieldSpec> fields = new ArrayList<>();
        
        // 1. Nested field: individual/firstName
        fields.add(FieldSpecRequest.FieldSpec.builder()
                .referenceModel("individual/firstName")
                .visible(true)
                .shortLabel("First Name")
                .longLabel("Customer First Name")
                .validations(List.of(
                        java.util.Map.of("type", "REQUIRED", "message", "First name is required"),
                        java.util.Map.of("type", "MIN", "value", 2)
                ))
                .build());

        // 2. Custom dynamic attribute: customNotes
        fields.add(FieldSpecRequest.FieldSpec.builder()
                .referenceModel("customNotes")
                .visible(true)
                .shortLabel("Notes")
                .longLabel("Additional Notes (Dynamic Attribute)")
                .build());

        RawFormMetadata metadata = dtoIntrospector.buildMetadata(
                fields,
                "CustomerRegistration",
                RegisterCustomerRequest.class,
                null, // enabledReferenceModels
                null  // requestAttributes
        );

        assertNotNull(metadata);
        assertNotNull(metadata.getDomainModel());
        assertNotNull(metadata.getDomainModel().getAttributes());
        
        // Check if attributes are present
        assertNotNull(metadata.getDomainModel().getAttributes(), "Attributes list should not be null");
        assertFalse(metadata.getDomainModel().getAttributes().isEmpty(), "Attributes list should not be empty");
        
        // Find individual and customNotes
        boolean foundIndividual = false;
        boolean foundCustomNotes = false;

        for (var attr : metadata.getDomainModel().getAttributes()) {
            if ("individual".equals(attr.getAttributeName())) {
                foundIndividual = true;
                assertNotNull(attr.getDomainModel(), "Individual should have nested domain model");
                boolean foundFirstName = false;
                for (var nested : attr.getDomainModel().getAttributes()) {
                    if ("firstName".equals(nested.getAttributeName())) {
                        foundFirstName = true;
                        assertEquals("First Name", nested.getShortLabel());
                        assertNotNull(nested.getValidations());
                        assertFalse(nested.getValidations().isEmpty());
                    }
                }
                assertTrue(foundFirstName, "firstName should be found inside individual");
            }
            if ("customNotes".equals(attr.getAttributeName())) {
                foundCustomNotes = true;
                assertEquals("Notes", attr.getShortLabel());
            }
        }

        assertTrue(foundIndividual, "individual should be found");
        assertTrue(foundCustomNotes, "customNotes should be found");
    }
}
