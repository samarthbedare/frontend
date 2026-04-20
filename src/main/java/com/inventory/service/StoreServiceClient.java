package com.inventory.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.inventory.model.Store;
import org.springframework.stereotype.Service;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Service
public class StoreServiceClient extends BaseServiceClient {

    public List<Store> getAllStores(String token) throws Exception {
        HttpRequest request = createRequestBuilder("/api/v1/stores", token).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Store>>(){});
        }
        throw new RuntimeException("Failed to fetch stores: " + response.statusCode());
    }

    public Store getStoreById(Long id, String token) throws Exception {
        HttpRequest request = createRequestBuilder("/api/v1/stores/" + id, token).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), Store.class);
        }
        throw new RuntimeException("Store not found with ID: " + id);
    }

    public List<Store> searchStoresByAddress(String query, String token) throws Exception {
        String encodedQuery = java.net.URLEncoder.encode(query, "UTF-8");
        HttpRequest request = createRequestBuilder("/api/v1/stores/search/address?q=" + encodedQuery, token).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Store>>(){});
        }
        throw new RuntimeException("Failed to search stores: " + response.statusCode());
    }

    public Store createStore(Store store, String token) throws Exception {
        String body = objectMapper.writeValueAsString(store);
        HttpRequest request = createRequestBuilder("/api/v1/stores", token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
                
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            if (response.body() != null && !response.body().isEmpty()) {
                return objectMapper.readValue(response.body(), Store.class);
            }
            return store;
        }
        throw new RuntimeException("Failed to create store: " + response.statusCode());
    }

    public Store updateStore(Long id, Store store, String token) throws Exception {
        String body = objectMapper.writeValueAsString(store);
        HttpRequest request = createRequestBuilder("/api/v1/stores/" + id, token)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();
                
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            if (response.body() != null && !response.body().isEmpty()) {
                return objectMapper.readValue(response.body(), Store.class);
            }
            return store;
        }
        throw new RuntimeException("Failed to update store: " + response.statusCode());
    }

    public void deleteStore(Long id, String token) throws Exception {
        HttpRequest request = createRequestBuilder("/api/v1/stores/" + id, token).DELETE().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Failed to delete store: " + response.statusCode());
        }
    }
}
