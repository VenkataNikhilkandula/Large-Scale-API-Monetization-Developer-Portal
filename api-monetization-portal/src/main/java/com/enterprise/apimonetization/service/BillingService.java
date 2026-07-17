package com.enterprise.apimonetization.service;

import com.enterprise.apimonetization.entity.*;
import com.enterprise.apimonetization.repository.ConsumerAppRepository;
import com.enterprise.apimonetization.repository.InvoiceRepository;
import com.enterprise.apimonetization.repository.SubscriptionRepository;
import com.enterprise.apimonetization.repository.ApiUsageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class BillingService {

    private static final Logger logger = LoggerFactory.getLogger(BillingService.class);

    @Autowired
    private ConsumerAppRepository consumerAppRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private ApiUsageRepository apiUsageRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Transactional
    public Invoice generateInvoice(Long appId, String billingMonth) {
        ConsumerApp app = consumerAppRepository.findById(appId)
                .orElseThrow(() -> new IllegalArgumentException("Consumer application not found"));

        // If invoice already exists, return it or recalculate
        Optional<Invoice> existing = invoiceRepository.findByApp_IdAndBillingMonth(appId, billingMonth);
        if (existing.isPresent()) {
            return existing.get();
        }

        List<Subscription> subscriptions = subscriptionRepository.findByApp_IdAndStatus(appId, SubscriptionStatus.ACTIVE);
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Subscription sub : subscriptions) {
            BigDecimal subCost = sub.getTier().getBasePrice();
            
            // Calculate overages
            Optional<ApiUsage> usageOpt = apiUsageRepository.findByApp_IdAndApi_IdAndBillingMonth(appId, sub.getApi().getId(), billingMonth);
            if (usageOpt.isPresent()) {
                ApiUsage usage = usageOpt.get();
                long overageRequests = usage.getOverageCount();
                if (overageRequests > 0) {
                    BigDecimal overageCost = BigDecimal.valueOf(overageRequests).multiply(sub.getTier().getOverageRate());
                    subCost = subCost.add(overageCost);
                }
            }
            totalAmount = totalAmount.add(subCost);
        }

        Invoice invoice = Invoice.builder()
                .app(app)
                .billingMonth(billingMonth)
                .amount(totalAmount)
                .status(InvoiceStatus.PENDING)
                .generatedAt(LocalDateTime.now())
                .build();

        return invoiceRepository.save(invoice);
    }

    @Transactional
    public Invoice payInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));
        
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAt(LocalDateTime.now());
        return invoiceRepository.save(invoice);
    }

    public List<Invoice> getInvoicesByApp(Long appId) {
        return invoiceRepository.findByApp_Id(appId);
    }

    // Cron job running at midnight of 1st day of every month: "0 0 0 1 * ?"
    @Scheduled(cron = "0 0 0 1 * ?")
    public void runMonthlyBilling() {
        logger.info("Starting scheduled monthly billing run...");
        String previousMonth = LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));
        List<ConsumerApp> apps = consumerAppRepository.findAll();
        for (ConsumerApp app : apps) {
            try {
                generateInvoice(app.getId(), previousMonth);
                logger.info("Generated invoice for App: {} (ID: {}) for Month: {}", app.getName(), app.getId(), previousMonth);
            } catch (Exception e) {
                logger.error("Failed to generate invoice for App ID: {}. Error: {}", app.getId(), e.getMessage());
            }
        }
        logger.info("Completed monthly billing run.");
    }
}
