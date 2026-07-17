package com.enterprise.apimonetization.controller;

import com.enterprise.apimonetization.entity.ApiKey;
import com.enterprise.apimonetization.service.ApiKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/keys")
@Tag(name = "API Key Management", description = "Endpoints to generate, rotate, and manage consumer API credentials")
@SecurityRequirement(name = "BearerAuth")
@PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN')")
public class ApiKeyController {

    @Autowired
    private ApiKeyService apiKeyService;

    @PostMapping("/generate")
    @Operation(summary = "Generate a new API key for a consumer application")
    public ResponseEntity<ApiKey> generateKey(@RequestParam Long appId) {
        return ResponseEntity.ok(apiKeyService.generateKey(appId));
    }

    @PostMapping("/{id}/regenerate")
    @Operation(summary = "Revoke current key and generate/rotate a new API key")
    public ResponseEntity<ApiKey> regenerateKey(@PathVariable Long id) {
        return ResponseEntity.ok(apiKeyService.regenerateKey(id));
    }

    @GetMapping("/app/{appId}")
    @Operation(summary = "List all API keys belonging to a consumer application")
    public ResponseEntity<List<ApiKey>> getKeysByApp(@PathVariable Long appId) {
        return ResponseEntity.ok(apiKeyService.getKeysByApp(appId));
    }
}
