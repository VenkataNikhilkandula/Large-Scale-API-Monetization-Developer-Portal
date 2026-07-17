package com.enterprise.apimonetization.controller;

import com.enterprise.apimonetization.entity.ApiInfo;
import com.enterprise.apimonetization.entity.ApiStatus;
import com.enterprise.apimonetization.service.ApiCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/apis")
@Tag(name = "API Catalog & Management", description = "Operations to search, publish, and manage API catalog lifecycle")
@SecurityRequirement(name = "BearerAuth")
public class ApiController {

    @Autowired
    private ApiCatalogService apiCatalogService;

    @GetMapping("/catalog")
    @Operation(summary = "Get published API catalog with search, paging, and sorting")
    public ResponseEntity<Page<ApiInfo>> getCatalog(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        return ResponseEntity.ok(apiCatalogService.getPublishedCatalog(query, page, size, sortBy, sortDir));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all APIs in any lifecycle state (Admin only)")
    public ResponseEntity<Page<ApiInfo>> getAllApis(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        return ResponseEntity.ok(apiCatalogService.getAllApis(page, size, sortBy, sortDir));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN')")
    @Operation(summary = "Register/Create a new API in DRAFT state")
    public ResponseEntity<ApiInfo> createApi(@RequestBody ApiInfo apiInfo) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(apiCatalogService.createApi(apiInfo, username));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN')")
    @Operation(summary = "Submit a DRAFT API for administrator publishing approval")
    public ResponseEntity<ApiInfo> submitForApproval(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(apiCatalogService.submitForApproval(id, username));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve API publication (Admin only)")
    public ResponseEntity<ApiInfo> approveApi(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(apiCatalogService.approveApi(id, username));
    }

    @PostMapping("/{id}/deprecate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deprecate API (Admin only)")
    public ResponseEntity<ApiInfo> deprecateApi(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(apiCatalogService.deprecateApi(id, username));
    }

    @PostMapping("/{id}/retire")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Retire and disable API (Admin only)")
    public ResponseEntity<ApiInfo> retireApi(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(apiCatalogService.retireApi(id, username));
    }
}
