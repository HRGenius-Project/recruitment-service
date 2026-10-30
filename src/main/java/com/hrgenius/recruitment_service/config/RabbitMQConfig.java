package com.hrgenius.recruitment_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String RECRUITMENT_EXCHANGE = "recruitment.exchange";
    public static final String CANDIDATE_QUEUE = "notification.recruitment.queue";
    public static final String CANDIDATE_STATUS_ROUTING_KEY = "candidate.status.updated";

    @Bean
    public TopicExchange recruitmentExchange() {
        return new TopicExchange(RECRUITMENT_EXCHANGE);
    }

    @Bean
    public Queue recruitmentNotificationQueue() {
        return new Queue(CANDIDATE_QUEUE, true);
    }

    @Bean
    public Binding bindingRecruitmentNotification(Queue recruitmentNotificationQueue, TopicExchange recruitmentExchange) {
        return BindingBuilder.bind(recruitmentNotificationQueue).to(recruitmentExchange).with(CANDIDATE_STATUS_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}