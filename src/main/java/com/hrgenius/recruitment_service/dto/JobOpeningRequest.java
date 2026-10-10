package com.hrgenius.recruitment_service.dto;

import com.hrgenius.recruitment_service.enums.JobStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JobOpeningRequest {
    @NotBlank(message = "Job title is required")
    private String title;

    private String description;

    @NotBlank(message = "Department is required")
    private String department;

    private String location;

    @NotNull(message = "Vacancies count is required")
    private Integer vacancies;

    private JobStatus status = JobStatus.OPEN;
}