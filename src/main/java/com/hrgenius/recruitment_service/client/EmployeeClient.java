package com.hrgenius.recruitment_service.client;

import com.hrgenius.recruitment_service.dto.EmployeeCreateDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "EMPLOYEE-SERVICE", url = "http://localhost:8092")
public interface EmployeeClient {

    @PostMapping("/api/employees")
    ResponseEntity<Map<String, Object>> createEmployee(@RequestBody EmployeeCreateDto request);
}