package com.enterprise.apimonetization.service;

import com.enterprise.apimonetization.entity.ApiInfo;
import com.enterprise.apimonetization.entity.ApiStatus;
import com.enterprise.apimonetization.entity.AuditLog;
import com.enterprise.apimonetization.entity.User;
import com.enterprise.apimonetization.repository.ApiInfoRepository;
import com.enterprise.apimonetization.repository.AuditLogRepository;
import com.enterprise.apimonetization.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ApiCatalogService {

    @Autowired
    private ApiInfoRepository apiInfoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Transactional
    public ApiInfo createApi(ApiInfo apiInfo, String ownerUsername) {
        User owner = userRepository.findByUsername(ownerUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        apiInfo.setOwner(owner);
        apiInfo.setStatus(ApiStatus.DRAFT);
        
        ApiInfo saved = apiInfoRepository.save(apiInfo);
        logAudit("CREATE_API", ownerUsername, "Created API: " + saved.getName() + " with id: " + saved.getId());
        return saved;
    }

    public Page<ApiInfo> getPublishedCatalog(String query, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        if (query != null && !query.trim().isEmpty()) {
            return apiInfoRepository.searchCatalog(ApiStatus.PUBLISHED, query, pageable);
        }
        return apiInfoRepository.findByStatus(ApiStatus.PUBLISHED, pageable);
    }

    public Page<ApiInfo> getAllApis(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return apiInfoRepository.findAll(pageable);
    }

    public Optional<ApiInfo> getApiById(Long id) {
        return apiInfoRepository.findById(id);
    }

    @Transactional
    public ApiInfo submitForApproval(Long id, String username) {
        ApiInfo api = apiInfoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("API not found"));
        
        if (api.getStatus() != ApiStatus.DRAFT) {
            throw new IllegalStateException("API is not in DRAFT state");
        }
        
        api.setStatus(ApiStatus.PENDING_APPROVAL);
        ApiInfo saved = apiInfoRepository.save(api);
        logAudit("SUBMIT_API_APPROVAL", username, "Submitted API id: " + id + " for approval");
        return saved;
    }

    @Transactional
    public ApiInfo approveApi(Long id, String approverUsername) {
        ApiInfo api = apiInfoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("API not found with id: " + id));

        if (api.getStatus() != ApiStatus.PENDING_APPROVAL) {
            throw new IllegalStateException(
                "API id=" + id + " is currently in state '" + api.getStatus() +
                "'. It must be in PENDING_APPROVAL state. " +
                "Call POST /api/v1/apis/" + id + "/submit first.");
        }

        api.setStatus(ApiStatus.PUBLISHED);
        ApiInfo saved = apiInfoRepository.save(api);
        logAudit("APPROVE_API", approverUsername, "Approved API id: " + id + " to PUBLISHED status");
        return saved;
    }

    @Transactional
    public ApiInfo deprecateApi(Long id, String adminUsername) {
        ApiInfo api = apiInfoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("API not found"));
        
        api.setStatus(ApiStatus.DEPRECATED);
        ApiInfo saved = apiInfoRepository.save(api);
        logAudit("DEPRECATE_API", adminUsername, "Deprecated API id: " + id);
        return saved;
    }

    @Transactional
    public ApiInfo retireApi(Long id, String adminUsername) {
        ApiInfo api = apiInfoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("API not found"));
        
        api.setStatus(ApiStatus.RETIRED);
        ApiInfo saved = apiInfoRepository.save(api);
        logAudit("RETIRE_API", adminUsername, "Retired API id: " + id);
        return saved;
    }

    private void logAudit(String action, String performedBy, String details) {
        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .performedBy(performedBy)
                .details(details)
                .build();
        auditLogRepository.save(auditLog);
    }
}
