package com.example.dynamicform.product.service;

import com.example.dynamicform.product.dto.RSPWiseDocumentSetupRequest;
import com.example.dynamicform.product.dto.RSPWiseDocumentSetupResponse;
import com.example.dynamicform.product.dto.RSPWiseDocumentTranslationRequest;

import java.util.List;
import java.util.UUID;

public interface RSPWiseDocumentSetupService {
    RSPWiseDocumentSetupResponse create(RSPWiseDocumentSetupRequest request);
    RSPWiseDocumentSetupResponse update(UUID id, RSPWiseDocumentSetupRequest request);
    RSPWiseDocumentSetupResponse updateTranslations(RSPWiseDocumentTranslationRequest request);
    RSPWiseDocumentSetupResponse getById(UUID id);
    List<RSPWiseDocumentSetupResponse> getAll();
    void delete(UUID id);
}
