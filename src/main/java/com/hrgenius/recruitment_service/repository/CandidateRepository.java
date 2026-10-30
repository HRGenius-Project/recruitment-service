package com.hrgenius.recruitment_service.repository;

import com.hrgenius.recruitment_service.entity.Candidate;
import com.hrgenius.recruitment_service.enums.CandidateStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    List<Candidate> findByJobOpeningId(Long jobOpeningId);
    List<Candidate> findByStatus(CandidateStatus status);
    boolean existsByEmail(String email);
    Optional<Candidate> findByEmail(String email);
}