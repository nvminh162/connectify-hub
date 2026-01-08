package com.nvminh162.notification.controller;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.nvminh162.event.dto.NotificationEvent;
import com.nvminh162.notification.dto.request.Recipient;
import com.nvminh162.notification.dto.request.SendEmailRequest;
import com.nvminh162.notification.service.EmailService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationController {

    EmailService emailService;

    @KafkaListener(topics = "notification-delivery")
    public void listenNotificationDelivery(NotificationEvent message) {
        log.info("Message received: {}", message);
        emailService.sendEmail(SendEmailRequest.builder()
                .to(Recipient.builder().email(message.getRecipient()).build())
                .subject(message.getSubject())
                .htmlContent(message.getBody())
                .build());
    }
}
