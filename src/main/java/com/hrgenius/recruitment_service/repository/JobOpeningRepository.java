package com.hrgenius.recruitment_service.repository;

import com.hrgenius.recruitment_service.entity.JobOpening;
import com.hrgenius.recruitment_service.enums.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobOpeningRepository extends JpaRepository<JobOpening, Long> {
    List<JobOpening> findByStatus(JobStatus status);
}