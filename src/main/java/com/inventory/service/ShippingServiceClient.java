package com.inventory.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.inventory.model.Shipment;
import org.springframework.stereotype.Service;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Service
public class ShippingServiceClient extends BaseServiceClient {

    public List<Shipment> getAllShipments(String token) throws Exception {
        HttpRequest request = createRequestBuilder("/api/v1/shipments", token).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Shipment>>(){});
        }
        throw new RuntimeException("Failed to fetch shipments: " + response.statusCode());
    }

    public Shipment getShipmentById(Long id, String token) throws Exception {
        HttpRequest request = createRequestBuilder("/api/v1/shipments/" + id, token).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), Shipment.class);
        }
        throw new RuntimeException("Shipment not found: " + response.statusCode());
    }

    public List<Shipment> getShipmentsByCustomer(Long customerId, String token) throws Exception {
        HttpRequest request = createRequestBuilder("/api/v1/shipments/customer/" + customerId, token).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Shipment>>(){});
        }
        throw new RuntimeException("Failed to fetch shipments for customer: " + response.statusCode());
    }

    public List<Shipment> getShipmentsByStore(Long storeId, String token) throws Exception {
        HttpRequest request = createRequestBuilder("/api/v1/shipments/store/" + storeId, token).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Shipment>>(){});
        }
        throw new RuntimeException("Failed to fetch shipments for store: " + response.statusCode());
    }

    public Shipment createShipment(Shipment shipment, String token) throws Exception {
        String body = objectMapper.writeValueAsString(shipment);
        HttpRequest request = createRequestBuilder("/api/v1/shipments", token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return objectMapper.readValue(response.body(), Shipment.class);
        }
        throw new RuntimeException(extractErrorMessage(response.body(), response.statusCode()));
    }

    public Shipment updateShipmentStatus(Long id, String status, String token) throws Exception {
        String body = objectMapper.writeValueAsString(status);
        HttpRequest request = createRequestBuilder("/api/v1/shipments/" + id + "/status", token)
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return objectMapper.readValue(response.body(), Shipment.class);
        }
        throw new RuntimeException(extractErrorMessage(response.body(), response.statusCode()));
    }

    public void deleteShipment(Long id, String token) throws Exception {
        HttpRequest request = createRequestBuilder("/api/v1/shipments/" + id, token).DELETE().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 300) {
            throw new RuntimeException(extractErrorMessage(response.body(), response.statusCode()));
        }
    }
}
