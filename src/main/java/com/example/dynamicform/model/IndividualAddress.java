package com.example.dynamicform.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndividualAddress {
    private List<KycAddress> address;
}
