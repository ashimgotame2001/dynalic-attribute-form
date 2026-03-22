package com.example.dynamicform.product.service;

import com.example.dynamicform.product.dto.RSPWiseDocumentSetupRequest;
import com.example.dynamicform.product.dto.RSPWiseDocumentSetupResponse;
import com.example.dynamicform.product.entity.DocumentEntity;
import com.example.dynamicform.product.entity.RSPWiseDocumentSetupEntity;
import com.example.dynamicform.product.repository.DocumentRepository;
import com.example.dynamicform.product.repository.RSPWiseDocumentSetupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RSPWiseDocumentSetupServiceImpl implements RSPWiseDocumentSetupService {

    private final RSPWiseDocumentSetupRepository repository;
    private final DocumentRepository documentRepository;

    @Override
    @Transactional
    public RSPWiseDocumentSetupResponse create(RSPWiseDocumentSetupRequest request) {
        DocumentEntity document = documentRepository.findById(request.getDocumentId())
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + request.getDocumentId()));

        RSPWiseDocumentSetupEntity entity = RSPWiseDocumentSetupEntity.builder()
                .document(document)
                .isPrimary(request.getIsPrimary())
                .rspId(request.getRspId())
                .isBackRequired(request.isBackRequired())
                .isExpiryDateRequired(request.isExpiryDateRequired())
                .build();

        RSPWiseDocumentSetupEntity saved = repository.save(entity);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public RSPWiseDocumentSetupResponse update(UUID id, RSPWiseDocumentSetupRequest request) {
        RSPWiseDocumentSetupEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("RSPWiseDocumentSetup not found with id: " + id));

        DocumentEntity document = documentRepository.findById(request.getDocumentId())
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + request.getDocumentId()));

        entity.setDocument(document);
        entity.setIsPrimary(request.getIsPrimary());
        entity.setRspId(request.getRspId());
        entity.setBackRequired(request.isBackRequired());
        entity.setExpiryDateRequired(request.isExpiryDateRequired());

        RSPWiseDocumentSetupEntity updated = repository.save(entity);
        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public RSPWiseDocumentSetupResponse getById(UUID id) {
        RSPWiseDocumentSetupEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("RSPWiseDocumentSetup not found with id: " + id));
        return mapToResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RSPWiseDocumentSetupResponse> getAll() {
        return repository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("RSPWiseDocumentSetup not found with id: " + id);
        }
        repository.deleteById(id);
    }

    private RSPWiseDocumentSetupResponse mapToResponse(RSPWiseDocumentSetupEntity entity) {
        return RSPWiseDocumentSetupResponse.builder()
                .id(entity.getId())
                .documentId(entity.getDocument() != null ? entity.getDocument().getId() : null)
                .documentName(entity.getDocument() != null ? entity.getDocument().getDocumentName() : null)
                .isPrimary(entity.getIsPrimary())
                .rspId(entity.getRspId())
                .isBackRequired(entity.isBackRequired())
                .isExpiryDateRequired(entity.isExpiryDateRequired())
                .build();
    }
}
