package com.example.dynamicform.product.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Data;

import java.time.LocalDate;

@Data
public class Individual {
    @NotEmpty
    private String firstName;
    private String middleName;
    @NotEmpty
    private String lastName;
    @Valid
    @NotNull
    private Gender gender;
    private Origin origin;
    private ResidingCountry residingCountry;
    private Address address;
    @NotNull(message = "dateOfBirth field is mandatory")
    @Past(message = "{201}")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDate dateOfBirth;
    @NotNull
    private String contactNumber;
    @NotEmpty(message = "email is required")
    private String email;
}


