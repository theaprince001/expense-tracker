package com.expensetracker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Slf4j
public class ResendEmailService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${resend.api-key}")
    private String apiKey;

    @Value("${resend.from-email:onboarding@resend.dev}")
    private String fromEmail;

    private static final String RESEND_API_URL = "https://api.resend.com/emails";

    public void sendEmail(String toEmail, String subject, String textBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        // Resend expects HTML; wrap plain text in <pre> to preserve line breaks
        String htmlBody = "<pre style=\"font-family: sans-serif; white-space: pre-wrap;\">" + textBody + "</pre>";

        Map<String, Object> body = Map.of(
                "from", "Expense Tracker <" + fromEmail + ">",
                "to", new String[]{toEmail},
                "subject", subject,
                "html", htmlBody
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(RESEND_API_URL, request, String.class);
            log.info("Email sent via Resend to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send email via Resend to {}: {}", toEmail, e.getMessage());
            throw e;
        }
    }
}
