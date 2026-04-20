package com.inventory.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.inventory.model.Product;
import org.springframework.stereotype.Service;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Service
public class ProductServiceClient extends BaseServiceClient {

    public List<Product> getAllProducts(String token, String name, String brand, String colour, String size) throws Exception {
        StringBuilder query = new StringBuilder("/api/v1/products");
        boolean hasQuery = false;
        
        if (name != null && !name.trim().isEmpty()) {
            query.append(hasQuery ? "&" : "?").append("name=").append(java.net.URLEncoder.encode(name, "UTF-8"));
            hasQuery = true;
        }
        if (brand != null && !brand.trim().isEmpty()) {
            query.append(hasQuery ? "&" : "?").append("brand=").append(java.net.URLEncoder.encode(brand, "UTF-8"));
            hasQuery = true;
        }
        if (colour != null && !colour.trim().isEmpty()) {
            query.append(hasQuery ? "&" : "?").append("colour=").append(java.net.URLEncoder.encode(colour, "UTF-8"));
            hasQuery = true;
        }
        if (size != null && !size.trim().isEmpty()) {
            query.append(hasQuery ? "&" : "?").append("size=").append(java.net.URLEncoder.encode(size, "UTF-8"));
            hasQuery = true;
        }

        HttpRequest request = createRequestBuilder(query.toString(), token).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Product>>() {});
        }
        throw new RuntimeException("Failed to fetch products. Status: " + response.statusCode());
    }

    public Product getProductById(Long id, String token) throws Exception {
        HttpRequest request = createRequestBuilder("/api/v1/products/" + id, token).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return objectMapper.readValue(response.body(), Product.class);
        }
        throw new RuntimeException(extractErrorMessage(response.body(), response.statusCode()));
    }

    public Product createProduct(Product product, String token) throws Exception {
        String body = objectMapper.writeValueAsString(product);
        HttpRequest request = createRequestBuilder("/api/v1/products", token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            if (response.body() != null && !response.body().isEmpty()) {
                return objectMapper.readValue(response.body(), Product.class);
            }
            return product;
        }
        throw new RuntimeException("Create failed: " + extractErrorMessage(response.body(), response.statusCode()));
    }

    public Product updateProduct(Long id, Map<String, Object> updates, String token) throws Exception {
        String body = objectMapper.writeValueAsString(updates);
        HttpRequest request = createRequestBuilder("/api/v1/products/" + id, token)
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            if (response.body() != null && !response.body().isEmpty()) {
                return objectMapper.readValue(response.body(), Product.class);
            }
            return null;
        }
        throw new RuntimeException(extractErrorMessage(response.body(), response.statusCode()));
    }

    public void deleteProduct(Long id, String token) throws Exception {
        HttpRequest request = createRequestBuilder("/api/v1/products/" + id, token).DELETE().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException(extractErrorMessage(response.body(), response.statusCode()));
        }
    }
}
