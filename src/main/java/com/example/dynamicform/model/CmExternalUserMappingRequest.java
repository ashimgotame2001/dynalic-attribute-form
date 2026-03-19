package com.example.dynamicform.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CmExternalUserMappingRequest {
    private String identifier;
    private String externalUserId;
    private Long rspId;
}
