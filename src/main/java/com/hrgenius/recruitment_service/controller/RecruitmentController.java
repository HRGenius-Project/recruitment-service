package com.hrgenius.recruitment_service.controller;

import com.hrgenius.recruitment_service.dto.CandidateRequest;
import com.hrgenius.recruitment_service.dto.JobOpeningRequest;
import com.hrgenius.recruitment_service.entity.Candidate;
import com.hrgenius.recruitment_service.entity.JobOpening;
import com.hrgenius.recruitment_service.enums.CandidateStatus;
import com.hrgenius.recruitment_service.enums.JobStatus;
import com.hrgenius.recruitment_service.service.RecruitmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recruitment")
@RequiredArgsConstructor
public class RecruitmentController {

    private final RecruitmentService recruitmentService;

    // 1. CREATE JOB (HR/ADMIN/RECRUITER only via Gateway injected role)
    @PostMapping("/jobs")
    public ResponseEntity<?> createJob(
            @RequestHeader(value = "X-User-Roles", required = false) String roles,
            @Valid @RequestBody JobOpeningRequest request) {

        if (!hasPrivilegedRole(roles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access Denied: Requires ADMIN, HR_MANAGER, or RECRUITER role.");
        }

        JobOpening createdJob = recruitmentService.createJobOpening(request);
        return new ResponseEntity<>(createdJob, HttpStatus.CREATED);
    }

    // 2. GET ALL JOBS (Public / Any Authenticated User)
    @GetMapping("/jobs")
    public ResponseEntity<List<JobOpening>> getAllJobs(@RequestParam(required = false) JobStatus status) {
        return ResponseEntity.ok(recruitmentService.getAllJobs(status));
    }

    // 3. GET SINGLE JOB
    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<JobOpening> getJobById(@PathVariable Long jobId) {
        return ResponseEntity.ok(recruitmentService.getJobById(jobId));
    }

    // 4. APPLY FOR A JOB (Chosen pattern: /jobs/{jobId}/apply)
    @PostMapping("/jobs/{jobId}/apply")
    public ResponseEntity<Candidate> applyForJob(
            @PathVariable Long jobId,
            @Valid @RequestBody CandidateRequest request) {

        request.setJobOpeningId(jobId);
        Candidate candidate = recruitmentService.applyForJob(request);
        return new ResponseEntity<>(candidate, HttpStatus.CREATED);
    }

    // 5. GET CANDIDATES BY JOB ID (HR/ADMIN only)
    @GetMapping("/jobs/{jobId}/candidates")
    public ResponseEntity<?> getCandidatesByJob(
            @RequestHeader(value = "X-User-Roles", required = false) String roles,
            @PathVariable Long jobId) {

        if (!hasPrivilegedRole(roles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access Denied: Requires ADMIN, HR_MANAGER, or RECRUITER role.");
        }

        return ResponseEntity.ok(recruitmentService.getCandidatesByJob(jobId));
    }

    // 6. UPDATE CANDIDATE STATUS (e.g. /candidates/1/status?status=HIRED)
    @PatchMapping("/candidates/{candidateId}/status")
    public ResponseEntity<?> updateCandidateStatus(
            @RequestHeader(value = "X-User-Roles", required = false) String roles,
            @PathVariable Long candidateId,
            @RequestParam CandidateStatus status) {

        if (!hasPrivilegedRole(roles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access Denied: Requires ADMIN, HR_MANAGER, or RECRUITER role.");
        }

        Candidate updated = recruitmentService.updateCandidateStatus(candidateId, status);
        return ResponseEntity.ok(updated);
    }

    private boolean hasPrivilegedRole(String roles) {
        if (roles == null) {
            return false;
        }
        return roles.contains("ADMIN") || roles.contains("HR_MANAGER") || roles.contains("RECRUITER");
    }
}