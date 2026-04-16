package com.example.dynamicform.product.service;

import com.example.dynamicform.product.dto.DocumentSetupRequest;
import com.example.dynamicform.product.dto.DocumentSetupResponse;
import com.example.dynamicform.product.dto.DocumentTranslationRequest;

import java.util.List;
import java.util.UUID;

public interface DocumentSetupService {
    DocumentSetupResponse create(DocumentSetupRequest request, Long languageId);
    DocumentSetupResponse update(UUID id, DocumentSetupRequest request, Long languageId);
    DocumentSetupResponse updateTranslations(DocumentTranslationRequest request, Long languageId);
    DocumentSetupResponse getById(UUID id, Long languageId);
    List<DocumentSetupResponse> getAll(Long languageId);
    void delete(UUID id);
}
