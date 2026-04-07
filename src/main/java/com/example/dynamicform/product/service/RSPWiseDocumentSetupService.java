package com.example.dynamicform.product.service;

import com.example.dynamicform.product.dto.RSPWiseDocumentSetupRequest;
import com.example.dynamicform.product.dto.RSPWiseDocumentSetupResponse;
import com.example.dynamicform.product.dto.RSPWiseDocumentTranslationRequest;

import java.util.List;
import java.util.UUID;

public interface RSPWiseDocumentSetupService {
    RSPWiseDocumentSetupResponse create(RSPWiseDocumentSetupRequest request, String language);
    RSPWiseDocumentSetupResponse update(UUID id, RSPWiseDocumentSetupRequest request, String language);
    RSPWiseDocumentSetupResponse updateTranslations(RSPWiseDocumentTranslationRequest request, String language);
    RSPWiseDocumentSetupResponse getById(UUID id, String language);
    List<RSPWiseDocumentSetupResponse> getAll(String language);
    void delete(UUID id);
}
