package com.inventory.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.inventory.model.AuthRequest;
import com.inventory.util.BackendConstants;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class AuthServiceClient extends BaseServiceClient {

    public String login(String username, String password) throws Exception {
        AuthRequest authRequest = new AuthRequest(username, password);
        String requestBody = objectMapper.writeValueAsString(authRequest);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BackendConstants.BACKEND_URL + "/api/v1/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            String body = response.body();
            try {
                JsonNode rootNode = objectMapper.readTree(body);
                if (rootNode.has("token")) {
                    return rootNode.get("token").asText();
                } else if (rootNode.has("jwt")) {
                    return rootNode.get("jwt").asText();
                }
            } catch (Exception ignored) {}
            return body;
        } else {
            throw new RuntimeException("Login failed with status: " + response.statusCode());
        }
    }
}
