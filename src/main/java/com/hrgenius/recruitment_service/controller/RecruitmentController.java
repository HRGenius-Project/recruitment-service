package com.hrgenius.recruitment_service.controller;

import com.hrgenius.recruitment_service.dto.CandidateRequest;
import com.hrgenius.recruitment_service.dto.JobOpeningRequest;
import com.hrgenius.recruitment_service.entity.Candidate;
import com.hrgenius.recruitment_service.entity.JobOpening;
import com.hrgenius.recruitment_service.enums.CandidateStatus;
import com.hrgenius.recruitment_service.service.RecruitmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recruitment")
@RequiredArgsConstructor
public class RecruitmentController {

    private final RecruitmentService recruitmentService;

    // 1. Nayi Job Create karna (Admin / HR / Recruiter)
    @PostMapping("/jobs")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'RECRUITER')")
    public ResponseEntity<JobOpening> createJob(@Valid @RequestBody JobOpeningRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recruitmentService.createJobOpening(request));
    }

    // 2. Saari Jobs fetch karna
    @GetMapping("/jobs")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'RECRUITER', 'MANAGER')")
    public ResponseEntity<List<JobOpening>> getJobs() {
        return ResponseEntity.ok(recruitmentService.getAllJobOpenings());
    }

    // 3. Kisi Job ke liye Candidate add karna
    @PostMapping("/candidates")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'RECRUITER')")
    public ResponseEntity<Candidate> applyCandidate(@Valid @RequestBody CandidateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recruitmentService.applyCandidate(request));
    }

    // 4. Job-wise Candidate list dekhna
    @GetMapping("/jobs/{jobId}/candidates")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'RECRUITER', 'MANAGER')")
    public ResponseEntity<List<Candidate>> getCandidatesByJob(@PathVariable Long jobId) {
        return ResponseEntity.ok(recruitmentService.getCandidatesByJob(jobId));
    }

    // 5. Candidate ka Status change karna (APPLIED -> SHORTLISTED -> HIRED etc.)
    @PatchMapping("/candidates/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'RECRUITER')")
    public ResponseEntity<Candidate> updateStatus(
            @PathVariable Long id,
            @RequestParam CandidateStatus status) {
        return ResponseEntity.ok(recruitmentService.updateCandidateStatus(id, status));
    }
}