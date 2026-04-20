package com.inventory.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.inventory.model.Customer;
import org.springframework.stereotype.Service;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Service
public class CustomerServiceClient extends BaseServiceClient {

    public List<Customer> getAllCustomers(String token) throws Exception {
        HttpRequest request = createRequestBuilder("/api/v1/customers", token).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Customer>>(){});
        }
        throw new RuntimeException("Failed to fetch customers: " + response.statusCode());
    }

    public Customer getCustomerById(Long id, String token) throws Exception {
        HttpRequest request = createRequestBuilder("/api/v1/customers/" + id, token).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), Customer.class);
        }
        throw new RuntimeException("Customer not found with ID: " + id);
    }

    public Customer getCustomerByEmail(String email, String token) throws Exception {
        HttpRequest request = createRequestBuilder("/api/v1/customers/email/" + email, token).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), Customer.class);
        }
        throw new RuntimeException("Customer not found for Email: " + email);
    }

    public Customer createCustomer(Customer customer, String token) throws Exception {
        String body = objectMapper.writeValueAsString(customer);
        HttpRequest request = createRequestBuilder("/api/v1/customers", token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
                
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            if (response.body() != null && !response.body().isEmpty()) {
                return objectMapper.readValue(response.body(), Customer.class);
            }
            return customer;
        }
        throw new RuntimeException("Failed to create customer: " + response.statusCode());
    }

    public Customer updateCustomer(Long id, Customer customer, String token) throws Exception {
        String body = objectMapper.writeValueAsString(customer);
        HttpRequest request = createRequestBuilder("/api/v1/customers/" + id, token)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();
                
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            if (response.body() != null && !response.body().isEmpty()) {
                return objectMapper.readValue(response.body(), Customer.class);
            }
            return customer;
        }
        throw new RuntimeException("Failed to update customer: " + response.statusCode());
    }

    public void deleteCustomer(Long id, String token) throws Exception {
        HttpRequest request = createRequestBuilder("/api/v1/customers/" + id, token).DELETE().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Failed to delete customer: " + response.statusCode());
        }
    }

    public boolean validateCustomer(Long id, String token) throws Exception {
        HttpRequest request = createRequestBuilder("/api/v1/customers/validate/" + id, token).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            String body = response.body();
            return body != null && body.trim().equalsIgnoreCase("true");
        }
        return false;
    }
}
