package com.enterprise.apimonetization.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/gw")
@Tag(name = "Simulated API Gateway Routing", description = "Mock API endpoints to test key validation, rate-limiting, and quota monitoring")
@SecurityRequirement(name = "ApiKeyAuth")
public class GatewayMockController {

    @RequestMapping(value = "/api/{apiId}/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    @Operation(summary = "Execute mock requests behind rate limiting and quota verification filters")
    public ResponseEntity<Map<String, Object>> mockRoute(
            @PathVariable Long apiId,
            HttpServletRequest request,
            @RequestHeader(value = "X-API-KEY", required = false) String apiKey) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "API Gateway successfully routed the request!");
        response.put("apiId", apiId);
        response.put("requestPath", request.getRequestURI());
        response.put("httpMethod", request.getMethod());
        response.put("timestamp", System.currentTimeMillis());
        response.put("gatewayNode", "monetization-gateway-node-1");

        return ResponseEntity.ok(response);
    }
}
