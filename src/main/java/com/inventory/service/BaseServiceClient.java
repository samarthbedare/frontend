package com.inventory.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.util.BackendConstants;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public abstract class BaseServiceClient {

    protected final HttpClient httpClient;
    protected final ObjectMapper objectMapper;

    protected BaseServiceClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
        // Support flexible parsing for legacy or irregular backend JSON
        this.objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        this.objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS, true);
    }

    protected HttpRequest.Builder createRequestBuilder(String path, String token) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BackendConstants.BACKEND_URL + path))
                .header("Accept", "application/json");

        if (token != null && !token.isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
        }
        return builder;
    }

    protected String extractErrorMessage(String body, int statusCode) {
        try {
            if (body != null && !body.isEmpty()) {
                com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(body);
                if (node.has("message")) {
                    return node.get("message").asText();
                }
            }
        } catch (Exception ignored) {}
        
        if (statusCode == 404) return "Resource not found with given ID.";
        return "An unexpected error occurred (Status: " + statusCode + ")";
    }
}
