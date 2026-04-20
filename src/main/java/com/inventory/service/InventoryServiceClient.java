package com.inventory.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.inventory.model.Inventory;
import org.springframework.stereotype.Service;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Service
public class InventoryServiceClient extends BaseServiceClient {

    public List<Inventory> getAllInventory(String token) throws Exception {
        HttpRequest request = createRequestBuilder("/api/v1/inventory", token).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Inventory>>(){});
        } else if (response.statusCode() == 404) {
            return new java.util.ArrayList<>();
        }
        throw new RuntimeException("Failed to fetch all inventory: " + response.statusCode());
    }

    public List<Inventory> getInventoryByStoreId(Long storeId, String token) throws Exception {
        HttpRequest request = createRequestBuilder("/api/v1/inventory/store/" + storeId, token).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Inventory>>(){});
        } else if (response.statusCode() == 404) {
            return new java.util.ArrayList<>();
        }
        throw new RuntimeException("Failed to fetch inventory for store: " + response.statusCode());
    }

    public List<Inventory> getInventoryByProductId(Long productId, String token) throws Exception {
        HttpRequest request = createRequestBuilder("/api/v1/inventory/product/" + productId, token).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Inventory>>(){});
        } else if (response.statusCode() == 404) {
            return new java.util.ArrayList<>();
        }
        throw new RuntimeException("Failed to fetch inventory for product: " + response.statusCode());
    }

    public void addInventory(Inventory inventory, String token) throws Exception {
        String body = objectMapper.writeValueAsString(inventory);
        HttpRequest request = createRequestBuilder("/api/v1/inventory", token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
                
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Failed to create inventory: " + response.statusCode());
        }
    }

    public void updateStock(Map<String, Object> payload, boolean isAdd, String token) throws Exception {
        String endpoint = isAdd ? "/api/v1/inventory/add" : "/api/v1/inventory/reduce";
        String body = objectMapper.writeValueAsString(payload);
        HttpRequest request = createRequestBuilder(endpoint, token)
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
                .build();
                
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Failed to update stock: " + response.statusCode());
        }
    }

    public void deleteInventory(Long storeId, Long productId, String token) throws Exception {
        HttpRequest request = createRequestBuilder("/api/v1/inventory/store/" + storeId + "/product/" + productId, token).DELETE().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Failed to delete inventory: " + response.statusCode());
        }
    }
}
