package com.hrgenius.recruitment_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hrgenius.recruitment_service.enums.CandidateStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "candidates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String email;

    private String phone;

    private String resumeUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_opening_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private JobOpening jobOpening;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CandidateStatus status = CandidateStatus.APPLIED;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}