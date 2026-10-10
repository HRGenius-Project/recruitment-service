package com.hrgenius.recruitment_service.service;

import com.hrgenius.recruitment_service.dto.CandidateRequest;
import com.hrgenius.recruitment_service.dto.CandidateStatusEvent;
import com.hrgenius.recruitment_service.dto.JobOpeningRequest;
import com.hrgenius.recruitment_service.entity.Candidate;
import com.hrgenius.recruitment_service.entity.JobOpening;
import com.hrgenius.recruitment_service.enums.CandidateStatus;
import com.hrgenius.recruitment_service.producer.CandidateEventProducer;
import com.hrgenius.recruitment_service.repository.CandidateRepository;
import com.hrgenius.recruitment_service.repository.JobOpeningRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecruitmentService {

    private final JobOpeningRepository jobOpeningRepository;
    private final CandidateRepository candidateRepository;
    private final CandidateEventProducer candidateEventProducer;

    @Transactional
    public JobOpening createJobOpening(JobOpeningRequest request) {
        JobOpening job = JobOpening.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .department(request.getDepartment())
                .location(request.getLocation())
                .vacancies(request.getVacancies())
                .status(request.getStatus())
                .build();
        return jobOpeningRepository.save(job);
    }

    public List<JobOpening> getAllJobOpenings() {
        return jobOpeningRepository.findAll();
    }

    @Transactional
    public Candidate applyCandidate(CandidateRequest request) {
        JobOpening job = jobOpeningRepository.findById(request.getJobOpeningId())
                .orElseThrow(() -> new RuntimeException("Job opening not found with id: " + request.getJobOpeningId()));

        Candidate candidate = Candidate.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .resumeUrl(request.getResumeUrl())
                .jobOpening(job)
                .status(CandidateStatus.APPLIED)
                .build();

        return candidateRepository.save(candidate);
    }

    public List<Candidate> getCandidatesByJob(Long jobOpeningId) {
        return candidateRepository.findByJobOpeningId(jobOpeningId);
    }

    @Transactional
    public Candidate updateCandidateStatus(Long candidateId, CandidateStatus newStatus) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate not found with id: " + candidateId));

        CandidateStatus oldStatus = candidate.getStatus();
        candidate.setStatus(newStatus);
        Candidate updatedCandidate = candidateRepository.save(candidate);

        // RabbitMQ Event fire karo
        try {
            CandidateStatusEvent event = CandidateStatusEvent.builder()
                    .candidateId(updatedCandidate.getId())
                    .candidateName(updatedCandidate.getFirstName() + " " + updatedCandidate.getLastName())
                    .candidateEmail(updatedCandidate.getEmail())
                    .jobId(updatedCandidate.getJobOpening() != null ? updatedCandidate.getJobOpening().getId() : null)
                    .jobTitle(updatedCandidate.getJobOpening() != null ? updatedCandidate.getJobOpening().getTitle() : "N/A")
                    .oldStatus(oldStatus)
                    .newStatus(newStatus)
                    .timestamp(LocalDateTime.now())
                    .build();

            candidateEventProducer.sendCandidateStatusEvent(event);
        } catch (Exception ex) {
            log.error("Failed to trigger event after updating candidate: {}", ex.getMessage());
        }

        return updatedCandidate;
    }
}