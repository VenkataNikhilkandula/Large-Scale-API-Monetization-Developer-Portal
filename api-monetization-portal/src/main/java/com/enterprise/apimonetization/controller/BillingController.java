package com.enterprise.apimonetization.controller;

import com.enterprise.apimonetization.entity.Invoice;
import com.enterprise.apimonetization.service.BillingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/billing")
@Tag(name = "Billing & Invoices", description = "Operations to review invoices, initiate payment, and trigger monthly run cycles")
@SecurityRequirement(name = "BearerAuth")
public class BillingController {

    @Autowired
    private BillingService billingService;

    @PostMapping("/invoice/generate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Manually trigger invoice generation for a consumer app for a specific month (Admin only)")
    public ResponseEntity<Invoice> generateInvoice(
            @RequestParam Long appId,
            @RequestParam String billingMonth) {
        return ResponseEntity.ok(billingService.generateInvoice(appId, billingMonth));
    }

    @PostMapping("/invoice/{id}/pay")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN')")
    @Operation(summary = "Pay a pending invoice")
    public ResponseEntity<Invoice> payInvoice(@PathVariable Long id) {
        return ResponseEntity.ok(billingService.payInvoice(id));
    }

    @GetMapping("/app/{appId}")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN')")
    @Operation(summary = "Retrieve invoice history for an application")
    public ResponseEntity<List<Invoice>> getInvoicesByApp(@PathVariable Long appId) {
        return ResponseEntity.ok(billingService.getInvoicesByApp(appId));
    }
}
