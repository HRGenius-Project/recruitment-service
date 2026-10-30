package com.hrgenius.recruitment_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/recruitment")
public class PingController {

    @GetMapping("/ping")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'RECRUITER', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> ping(Authentication authentication) {
        return ResponseEntity.ok(Map.of(
                "service", "recruitment-service",
                "status", "UP",
                "authenticatedUser", authentication.getName(),
                "authorities", authentication.getAuthorities()
        ));
    }
}