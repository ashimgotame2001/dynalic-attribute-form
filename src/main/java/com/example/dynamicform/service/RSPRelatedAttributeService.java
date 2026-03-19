package com.example.dynamicform.service;

import com.example.dynamicform.dto.RSPRelatedAttributeRequest;
import com.example.dynamicform.dto.RSPRelatedAttributeResponse;
import com.example.dynamicform.entity.RSPRelatedAttributeEntity;
import com.example.dynamicform.repository.RSPRelatedAttributeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RSPRelatedAttributeService {
    private final RSPRelatedAttributeRepository repository;

    public RSPRelatedAttributeService(RSPRelatedAttributeRepository repository) {
        this.repository = repository;
    }

    public List<RSPRelatedAttributeResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<RSPRelatedAttributeResponse> findByRspId(Long rspId) {
        return repository.findByRspId(rspId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<RSPRelatedAttributeResponse> findByRspIdAndFieldType(Long rspId, com.example.dynamicform.enums.DynamicFieldFor fieldType) {
        return repository.findByRspIdAndFieldType(rspId, fieldType).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public Optional<RSPRelatedAttributeResponse> findById(UUID id) {
        return repository.findById(id).map(this::toResponse);
    }

    @Transactional
    public RSPRelatedAttributeResponse create(RSPRelatedAttributeRequest request) {
        RSPRelatedAttributeEntity entity = new RSPRelatedAttributeEntity();
        entity.setRspId(request.getRspId());
        entity.setAttributeName(request.getAttributeName());
        entity.setFieldType(request.getFieldType());
        return toResponse(repository.save(entity));
    }

    @Transactional
    public Optional<RSPRelatedAttributeResponse> update(UUID id, RSPRelatedAttributeRequest request) {
        return repository.findById(id).map(existing -> {
            existing.setRspId(request.getRspId());
            existing.setAttributeName(request.getAttributeName());
            existing.setFieldType(request.getFieldType());
            return toResponse(repository.save(existing));
        });
    }

    @Transactional
    public void delete(UUID id) {
        repository.deleteById(id);
    }

    private RSPRelatedAttributeResponse toResponse(RSPRelatedAttributeEntity entity) {
        return RSPRelatedAttributeResponse.builder()
                .id(entity.getId())
                .rspId(entity.getRspId())
                .attributeName(entity.getAttributeName())
                .fieldType(entity.getFieldType())
                .build();
    }
}
