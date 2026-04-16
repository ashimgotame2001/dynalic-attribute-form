package com.example.dynamicform.platform.adapter;

import com.example.dynamicform.platform.api.dto.RelationshipDefinitionDTO;
import com.example.dynamicform.platform.service.RuntimeRelationshipPlatform;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("platformRelationshipController")
@RequestMapping("/api/platform/admin/relationships")
public class RelationshipController {

    private final RuntimeRelationshipPlatform relationshipPlatform;

    public RelationshipController(RuntimeRelationshipPlatform relationshipPlatform) {
        this.relationshipPlatform = relationshipPlatform;
    }

    @PostMapping("/definitions")
    public ResponseEntity<RelationshipDefinitionDTO> createRelationshipDefinition(@Valid @RequestBody RelationshipDefinitionDTO definition) {
        return ResponseEntity.status(HttpStatus.CREATED).body(relationshipPlatform.createRelationshipDefinition(definition));
    }

    @PutMapping("/definitions/{relationshipName}")
    public ResponseEntity<RelationshipDefinitionDTO> updateRelationshipDefinition(@PathVariable String relationshipName,
                                                                                  @Valid @RequestBody RelationshipDefinitionDTO definition) {
        return ResponseEntity.ok(relationshipPlatform.updateRelationshipDefinition(relationshipName, definition));
    }

    @DeleteMapping("/definitions/{relationshipName}")
    public ResponseEntity<Void> deleteRelationshipDefinition(@PathVariable String relationshipName) {
        relationshipPlatform.deleteRelationshipDefinition(relationshipName);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/definitions/{relationshipName}")
    public ResponseEntity<RelationshipDefinitionDTO> getRelationshipDefinition(@PathVariable String relationshipName) {
        return relationshipPlatform.getRelationshipDefinition(relationshipName)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/definitions")
    public ResponseEntity<List<RelationshipDefinitionDTO>> getAllRelationshipDefinitions() {
        return ResponseEntity.ok(relationshipPlatform.getAllRelationshipDefinitions());
    }

    @GetMapping("/definitions/entity/{entityName}")
    public ResponseEntity<List<RelationshipDefinitionDTO>> getRelationshipDefinitionsByEntity(@PathVariable String entityName) {
        return ResponseEntity.ok(relationshipPlatform.getRelationshipDefinitionsByEntity(entityName));
    }
}
