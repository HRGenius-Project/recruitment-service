package com.hrgenius.recruitment_service.producer;

import com.hrgenius.recruitment_service.config.RabbitMQConfig;
import com.hrgenius.recruitment_service.dto.CandidateStatusEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CandidateEventProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendCandidateStatusEvent(CandidateStatusEvent event) {
        try {
            log.info("Publishing status event for candidate: {} to RabbitMQ", event.getCandidateId());
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.RECRUITMENT_EXCHANGE,
                    RabbitMQConfig.CANDIDATE_STATUS_ROUTING_KEY,
                    event
            );
            log.info("Successfully published event for candidate: {}", event.getCandidateId());
        } catch (Exception ex) {
            log.error("Failed to publish candidate status event: {}", ex.getMessage(), ex);
        }
    }
}