package com.enterprise.apimonetization.controller;

import com.enterprise.apimonetization.entity.Subscription;
import com.enterprise.apimonetization.entity.SubscriptionTier;
import com.enterprise.apimonetization.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscriptions")
@Tag(name = "Subscription Management", description = "Endpoints for subscribing applications to APIs and upgrading/downgrading tiers")
@SecurityRequirement(name = "BearerAuth")
@PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN')")
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    @PostMapping
    @Operation(summary = "Subscribe an application to an API with a selected tier (FREE, BASIC, PREMIUM, ENTERPRISE)")
    public ResponseEntity<Subscription> subscribe(
            @RequestParam Long appId,
            @RequestParam Long apiId,
            @RequestParam SubscriptionTier tier) {
        return ResponseEntity.ok(subscriptionService.subscribe(appId, apiId, tier));
    }

    @PutMapping("/{id}/change")
    @Operation(summary = "Upgrade or downgrade an active subscription plan tier")
    public ResponseEntity<Subscription> changeTier(
            @PathVariable Long id,
            @RequestParam SubscriptionTier tier) {
        return ResponseEntity.ok(subscriptionService.upgradeDowngrade(id, tier));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel an active API subscription")
    public ResponseEntity<Subscription> cancelSubscription(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.cancelSubscription(id));
    }

    @GetMapping("/app/{appId}")
    @Operation(summary = "List all active subscriptions for a consumer application")
    public ResponseEntity<List<Subscription>> getSubscriptionsByApp(@PathVariable Long appId) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionsByApp(appId));
    }
}
