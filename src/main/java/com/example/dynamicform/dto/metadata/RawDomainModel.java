package com.example.dynamicform.dto.metadata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Raw DTO for the domainModel object.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RawDomainModel {

    private List<RawDomainAttribute> attributes;
}