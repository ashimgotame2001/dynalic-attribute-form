package com.example.dynamicform.model;

import com.example.dynamicform.model.IndividualSource;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

/**
 * Implements {@link IndividualSource} to allow reuse of Customer KYC update mapping logic.
 * This does not change the domain meaning of this class.
 */

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IndividualUpdate implements IndividualSource {

    @NotEmpty
    private String firstName;
    private String middleName;
    @NotEmpty
    private String lastName;
    @NotEmpty
    private String email;
    private String contactNumber;
    private Gender gender;
    private BasicOccupation occupation;
    private List<KycAddress> address;
    private Origin origin;
}

