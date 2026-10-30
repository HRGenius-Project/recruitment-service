package com.hrgenius.recruitment_service.service;

import com.hrgenius.recruitment_service.client.EmployeeClient;
import com.hrgenius.recruitment_service.dto.CandidateRequest;
import com.hrgenius.recruitment_service.dto.CandidateStatusEvent;
import com.hrgenius.recruitment_service.dto.EmployeeCreateDto;
import com.hrgenius.recruitment_service.dto.JobOpeningRequest;
import com.hrgenius.recruitment_service.entity.Candidate;
import com.hrgenius.recruitment_service.entity.JobOpening;
import com.hrgenius.recruitment_service.enums.CandidateStatus;
import com.hrgenius.recruitment_service.enums.JobStatus;
import com.hrgenius.recruitment_service.producer.CandidateEventProducer;
import com.hrgenius.recruitment_service.repository.CandidateRepository;
import com.hrgenius.recruitment_service.repository.JobOpeningRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecruitmentService {

    private final JobOpeningRepository jobOpeningRepository;
    private final CandidateRepository candidateRepository;
    private final CandidateEventProducer eventProducer;
    private final EmployeeClient employeeClient;

    // --- JOB OPENINGS ---

    @Transactional
    public JobOpening createJobOpening(JobOpeningRequest request) {
        JobOpening job = JobOpening.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .department(request.getDepartment())
                .location(request.getLocation())
                .vacancies(request.getVacancies())
                .status(request.getStatus() != null ? request.getStatus() : JobStatus.OPEN)
                .build();

        return jobOpeningRepository.save(job);
    }

    public List<JobOpening> getAllJobs(JobStatus status) {
        if (status != null) {
            return jobOpeningRepository.findByStatus(status);
        }
        return jobOpeningRepository.findAll();
    }

    public JobOpening getJobById(Long id) {
        return jobOpeningRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job opening not found with id: " + id));
    }

    // --- CANDIDATE APPLY ---

    @Transactional
    public Candidate applyForJob(CandidateRequest request) {
        JobOpening job = getJobById(request.getJobOpeningId());

        if (job.getStatus() != JobStatus.OPEN) {
            throw new IllegalStateException("Cannot apply. This job opening is currently " + job.getStatus());
        }

        if (candidateRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Candidate with email " + request.getEmail() + " has already applied for a position.");
        }

        Candidate candidate = Candidate.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .resumeUrl(request.getResumeUrl())
                .jobOpening(job)
                .status(CandidateStatus.APPLIED)
                .build();

        Candidate savedCandidate = candidateRepository.save(candidate);

        // RabbitMQ: New Application Received Event
        eventProducer.sendCandidateStatusEvent(CandidateStatusEvent.builder()
                .candidateId(savedCandidate.getId())
                .candidateName(savedCandidate.getFirstName() + " " + savedCandidate.getLastName())
                .candidateEmail(savedCandidate.getEmail())
                .jobId(job.getId())
                .jobTitle(job.getTitle())
                .oldStatus(null)
                .newStatus(CandidateStatus.APPLIED)
                .timestamp(LocalDateTime.now())
                .build());

        return savedCandidate;
    }

    public List<Candidate> getCandidatesByJob(Long jobId) {
        return candidateRepository.findByJobOpeningId(jobId);
    }

    // --- STATUS UPDATE & RESILIENT ONBOARDING ---

    @Transactional
    public Candidate updateCandidateStatus(Long candidateId, CandidateStatus newStatus) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate not found with id: " + candidateId));

        CandidateStatus oldStatus = candidate.getStatus();
        candidate.setStatus(newStatus);
        Candidate updatedCandidate = candidateRepository.save(candidate);

        // RabbitMQ: Stage Update Event
        eventProducer.sendCandidateStatusEvent(CandidateStatusEvent.builder()
                .candidateId(updatedCandidate.getId())
                .candidateName(updatedCandidate.getFirstName() + " " + updatedCandidate.getLastName())
                .candidateEmail(updatedCandidate.getEmail())
                .jobId(candidate.getJobOpening().getId())
                .jobTitle(candidate.getJobOpening().getTitle())
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .timestamp(LocalDateTime.now())
                .build());

        // Agar HIRED hua toh Employee Service me draft onboarding trigger karo
        if (newStatus == CandidateStatus.HIRED) {
            triggerEmployeeOnboarding(updatedCandidate);
        }

        return updatedCandidate;
    }

    private void triggerEmployeeOnboarding(Candidate candidate) {
        EmployeeCreateDto dto = EmployeeCreateDto.builder()
                .firstName(candidate.getFirstName())
                .lastName(candidate.getLastName())
                .email(candidate.getEmail())
                .phone(candidate.getPhone())
                .department(candidate.getJobOpening().getDepartment())
                .designation(candidate.getJobOpening().getTitle())
                .salary(0.0)
                .joiningDate(LocalDate.now())
                .status("ONBOARDING")
                .build();

        try {
            log.info("Sending Feign request to employee-service for candidate: {}", candidate.getEmail());
            employeeClient.createEmployee(dto);
            log.info("Successfully created draft employee for: {}", candidate.getEmail());
        } catch (Exception ex) {
            // Fail-safe handling: candidate status HIRED hi rahega
            log.error("CRITICAL: Failed to auto-create draft employee in employee-service for email: {}. Error: {}",
                    candidate.getEmail(), ex.getMessage());
            log.warn("Candidate marked HIRED. HR can manually retry sync or verify employee-service status.");
        }
    }
}