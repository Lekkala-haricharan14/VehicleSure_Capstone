package org.thehartford.willowshield.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class EmailHubService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${n8n.webhook.url}")
    private String n8nWebhookUrl;

    public void sendStaffWelcomeEmail(String email, String username, String password, String role) {
        String message = String.format(
            "Hello %s,\n\nYour staff account has been created successfully.\n\nRole: %s\nUsername: %s\nPassword: %s\n\nPlease log in and change your password immediately.",
            username, role, username, password
        );

        Map<String, Object> payload = new HashMap<>();
        payload.put("email", email);
        payload.put("subject", "Welcome to VehicleSure  - Your Staff Credentials");
        payload.put("message", message);

        try {
            restTemplate.postForObject(n8nWebhookUrl, payload, String.class);
            System.out.println("Email request sent to n8n for user: " + username);
        } catch (Exception e) {
            System.err.println("Failed to send email via n8n: " + e.getMessage());
            // We don't want to fail staff creation if email fails, but we should log it
        }
    }
}
