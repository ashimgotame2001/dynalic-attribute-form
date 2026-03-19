package com.example.dynamicform.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardRequest {

    private String userId;
    private String appVersion;
    private String phoneBrand;
    private String phoneOs;
    private String fcmId;
    private String osVersion;
}
