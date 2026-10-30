package com.hrgenius.recruitment_service.dto;

import com.hrgenius.recruitment_service.enums.CandidateStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateStatusEvent implements Serializable {

    private Long candidateId;
    private String candidateName;
    private String candidateEmail;
    private Long jobId;
    private String jobTitle;
    private CandidateStatus oldStatus;
    private CandidateStatus newStatus;
    private LocalDateTime timestamp;
}