package com.example.dynamicform.model;

import lombok.*;

/**
 * DTO for ApplicationUser
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationUserIdentifierRequest {
    private String identifier;

}
