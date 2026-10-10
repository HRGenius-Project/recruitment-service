package com.hrgenius.recruitment_service.repository;

import com.hrgenius.recruitment_service.entity.Candidate;
import com.hrgenius.recruitment_service.enums.CandidateStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    List<Candidate> findByJobOpeningId(Long jobOpeningId);
    List<Candidate> findByStatus(CandidateStatus status);
}