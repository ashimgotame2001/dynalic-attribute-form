package com.example.dynamicform.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.example.dynamicform.model.IndividualSource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Implements {@link IndividualSource} to allow reuse of Customer KYC update mapping logic.
 * This does not change the domain meaning of this class.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class KycIndividual implements IndividualSource {
    @NotNull(message = "firstName field is mandatory")
    private String firstName;
    private String middleName;
    @NotNull(message = "lastName field is mandatory")
    private String lastName;
    @NotNull(message = "dateOfBirth field is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDate dateOfBirth;
    @Valid
    private Gender gender;
    @Valid
    private List<KycAddress> address;
    @Valid
    private BasicOccupation occupation;
    @Valid
    private List<SupportingDocument> supportingDocuments;
    private Origin origin;
    private String contactNumber;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String email;
    @Valid
    private String profilePicture;
}