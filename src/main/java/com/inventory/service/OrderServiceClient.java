package com.inventory.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.inventory.model.Order;
import com.inventory.model.OrderRequest;
import org.springframework.stereotype.Service;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceClient extends BaseServiceClient {

    public List<Order> getAllOrders(String token) throws Exception {
        HttpRequest request = createRequestBuilder("/api/v1/orders", token).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Order>>(){});
        }
        throw new RuntimeException("Failed to fetch orders: " + response.statusCode());
    }

    public Order getOrderById(Long id, String token) throws Exception {
        HttpRequest request = createRequestBuilder("/api/v1/orders/" + id, token).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), Order.class);
        }
        throw new RuntimeException("Order not found with ID: " + id);
    }

    public List<Order> getOrdersByCustomerId(Long customerId, String token) throws Exception {
        HttpRequest request = createRequestBuilder("/api/v1/orders/customer/" + customerId, token).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Order>>(){});
        }
        throw new RuntimeException("Failed to fetch orders for customer: " + response.statusCode());
    }

    public List<Order> getOrdersByStoreId(Long storeId, String token) throws Exception {
        HttpRequest request = createRequestBuilder("/api/v1/orders/store/" + storeId, token).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Order>>(){});
        }
        throw new RuntimeException("Failed to fetch orders for store: " + response.statusCode());
    }

    public Order createOrder(OrderRequest orderReq, String token) throws Exception {
        String body = objectMapper.writeValueAsString(orderReq);
        HttpRequest request = createRequestBuilder("/api/v1/orders", token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
                
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            if (response.body() != null && !response.body().isEmpty()) {
                return objectMapper.readValue(response.body(), Order.class);
            }
            return new Order();
        }
        throw new RuntimeException("Failed to create order: " + response.statusCode());
    }

    public void updateOrderStatus(Long orderId, Map<String, String> statusMap, String token) throws Exception {
        String body = objectMapper.writeValueAsString(statusMap);
        HttpRequest request = createRequestBuilder("/api/v1/orders/" + orderId + "/status", token)
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
                .build();
                
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Failed to update order status: " + response.statusCode());
        }
    }

    public void linkShipment(Long orderId, Map<String, Long> payload, String token) throws Exception {
        String body = objectMapper.writeValueAsString(payload);
        HttpRequest request = createRequestBuilder("/api/v1/orders/" + orderId + "/shipment", token)
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
                .build();
                
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Failed to link shipment: " + response.statusCode());
        }
    }

    public void deleteOrder(Long orderId, String token) throws Exception {
        HttpRequest request = createRequestBuilder("/api/v1/orders/" + orderId, token).DELETE().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Failed to delete order: " + response.statusCode());
        }
    }
}
