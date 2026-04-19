package com.inventory.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.model.AuthRequest;
import com.inventory.model.Product;
import com.inventory.model.Customer;
import com.inventory.model.Order;
import com.inventory.model.OrderRequest;
import com.inventory.model.Store;
import com.inventory.model.Inventory;
import com.inventory.util.BackendConstants;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

@Service
public class BackendHttpClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public BackendHttpClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        this.objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS, true);
    }

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
            // Try to parse if it's JSON {"token": "..."}
            try {
                JsonNode rootNode = objectMapper.readTree(body);
                if (rootNode.has("token")) {
                    return rootNode.get("token").asText();
                } else if (rootNode.has("jwt")) {
                    return rootNode.get("jwt").asText();
                }
            } catch (Exception e) {
                // If it's not JSON or parsing fails, maybe the raw body is exactly the token
            }
            return body;
        } else {
            throw new RuntimeException("Login failed with status: " + response.statusCode());
        }
    }

    public List<Product> getAllProducts(String token, String name, String brand, String colour, String size) throws Exception {
        StringBuilder uriBuilder = new StringBuilder(BackendConstants.BACKEND_URL + "/api/v1/products");
        boolean hasQuery = false;
        
        if (name != null && !name.trim().isEmpty()) {
            uriBuilder.append(hasQuery ? "&" : "?").append("name=").append(java.net.URLEncoder.encode(name, "UTF-8"));
            hasQuery = true;
        }
        if (brand != null && !brand.trim().isEmpty()) {
            uriBuilder.append(hasQuery ? "&" : "?").append("brand=").append(java.net.URLEncoder.encode(brand, "UTF-8"));
            hasQuery = true;
        }
        if (colour != null && !colour.trim().isEmpty()) {
            uriBuilder.append(hasQuery ? "&" : "?").append("colour=").append(java.net.URLEncoder.encode(colour, "UTF-8"));
            hasQuery = true;
        }
        if (size != null && !size.trim().isEmpty()) {
            uriBuilder.append(hasQuery ? "&" : "?").append("size=").append(java.net.URLEncoder.encode(size, "UTF-8"));
            hasQuery = true;
        }

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(uriBuilder.toString()))
                .header("Accept", "application/json")
                .GET();

        if (token != null && !token.isEmpty()) {
            requestBuilder.header("Authorization", "Bearer " + token);
        }

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Product>>() {});
        } else {
            throw new RuntimeException("Failed to fetch products. Status: " + response.statusCode());
        }
    }

    public Product getProductById(Long id, String token) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BackendConstants.BACKEND_URL + "/api/v1/products/" + id))
                .header("Accept", "application/json")
                .GET();

        if (token != null && !token.isEmpty()) {
            requestBuilder.header("Authorization", "Bearer " + token);
        }

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return objectMapper.readValue(response.body(), Product.class);
        } else {
            throw new RuntimeException("Failed to fetch product " + id + ". Status: " + response.statusCode());
        }
    }

    public Product createProduct(Product product, String token) throws Exception {
        String requestBody = objectMapper.writeValueAsString(product);
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BackendConstants.BACKEND_URL + "/api/v1/products"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody));

        if (token != null && !token.isEmpty()) {
            requestBuilder.header("Authorization", "Bearer " + token);
        }

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            // Some backends return the created object, if not, we can just return null or parse
            if (response.body() != null && !response.body().isEmpty()) {
                return objectMapper.readValue(response.body(), Product.class);
            }
            return product; // Fallback
        } else {
            throw new RuntimeException("Failed to create product. Status: " + response.statusCode() + " Body: " + response.body());
        }
    }

    public Product updateProduct(Long id, java.util.Map<String, Object> updates, String token) throws Exception {
        String requestBody = objectMapper.writeValueAsString(updates);
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BackendConstants.BACKEND_URL + "/api/v1/products/" + id))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(requestBody));

        if (token != null && !token.isEmpty()) {
            requestBuilder.header("Authorization", "Bearer " + token);
        }

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            if (response.body() != null && !response.body().isEmpty()) {
                return objectMapper.readValue(response.body(), Product.class);
            }
            return null;
        } else {
            throw new RuntimeException("Failed to update product. Status: " + response.statusCode() + " Body: " + response.body());
        }
    }

    public void deleteProduct(Long id, String token) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BackendConstants.BACKEND_URL + "/api/v1/products/" + id))
                .DELETE();

        if (token != null && !token.isEmpty()) {
            requestBuilder.header("Authorization", "Bearer " + token);
        }

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return;
        } else {
            throw new RuntimeException("Failed to delete product. Status: " + response.statusCode() + " Body: " + response.body());
        }
    }

    // --- CUSTOMER SERVICE ---
    public List<Customer> getAllCustomers(String token) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BackendConstants.BACKEND_URL + "/api/v1/customers"))
                .header("Accept", "application/json")
                .GET();
        if (token != null && !token.isEmpty()) requestBuilder.header("Authorization", "Bearer " + token);
        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Customer>>(){});
        }
        throw new RuntimeException("Failed to fetch customers: " + response.statusCode());
    }

    public Customer getCustomerById(Long id, String token) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BackendConstants.BACKEND_URL + "/api/v1/customers/" + id))
                .header("Accept", "application/json")
                .GET();
        if (token != null && !token.isEmpty()) requestBuilder.header("Authorization", "Bearer " + token);
        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), Customer.class);
        }
        throw new RuntimeException("Customer not found: " + response.statusCode());
    }

    public Customer getCustomerByEmail(String email, String token) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BackendConstants.BACKEND_URL + "/api/v1/customers/email/" + email))
                .header("Accept", "application/json")
                .GET();
        if (token != null && !token.isEmpty()) requestBuilder.header("Authorization", "Bearer " + token);
        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), Customer.class);
        }
        throw new RuntimeException("Customer not found: " + response.statusCode());
    }

    public Customer createCustomer(Customer customer, String token) throws Exception {
        String requestBody = objectMapper.writeValueAsString(customer);
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BackendConstants.BACKEND_URL + "/api/v1/customers"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody));
        if (token != null && !token.isEmpty()) requestBuilder.header("Authorization", "Bearer " + token);
        HttpRequest request = requestBuilder.build();
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
        String requestBody = objectMapper.writeValueAsString(customer);
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BackendConstants.BACKEND_URL + "/api/v1/customers/" + id))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody));
        if (token != null && !token.isEmpty()) requestBuilder.header("Authorization", "Bearer " + token);
        HttpRequest request = requestBuilder.build();
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
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BackendConstants.BACKEND_URL + "/api/v1/customers/" + id))
                .DELETE();
        if (token != null && !token.isEmpty()) requestBuilder.header("Authorization", "Bearer " + token);
        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Failed to delete customer: " + response.statusCode());
        }
    }

    public boolean validateCustomer(Long id, String token) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BackendConstants.BACKEND_URL + "/api/v1/customers/validate/" + id))
                .GET();
        if (token != null && !token.isEmpty()) requestBuilder.header("Authorization", "Bearer " + token);
        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode() == 200;
    }

    // --- ORDER SERVICE ---
    public List<Order> getAllOrders(String token) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BackendConstants.BACKEND_URL + "/api/v1/orders"))
                .header("Accept", "application/json")
                .GET();
        if (token != null && !token.isEmpty()) requestBuilder.header("Authorization", "Bearer " + token);
        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Order>>(){});
        }
        throw new RuntimeException("Failed to fetch orders: " + response.statusCode());
    }

    public Order getOrderById(Long id, String token) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BackendConstants.BACKEND_URL + "/api/v1/orders/" + id))
                .header("Accept", "application/json")
                .GET();
        if (token != null && !token.isEmpty()) requestBuilder.header("Authorization", "Bearer " + token);
        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), Order.class);
        }
        throw new RuntimeException("Order not found: " + response.statusCode());
    }

    public List<Order> getOrdersByCustomerId(Long customerId, String token) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BackendConstants.BACKEND_URL + "/api/v1/orders/customer/" + customerId))
                .header("Accept", "application/json")
                .GET();
        if (token != null && !token.isEmpty()) requestBuilder.header("Authorization", "Bearer " + token);
        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Order>>(){});
        }
        throw new RuntimeException("Failed to fetch orders for customer: " + response.statusCode());
    }

    public List<Order> getOrdersByStoreId(Long storeId, String token) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BackendConstants.BACKEND_URL + "/api/v1/orders/store/" + storeId))
                .header("Accept", "application/json")
                .GET();
        if (token != null && !token.isEmpty()) requestBuilder.header("Authorization", "Bearer " + token);
        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Order>>(){});
        }
        throw new RuntimeException("Failed to fetch orders for store: " + response.statusCode());
    }

    public Order createOrder(OrderRequest orderReq, String token) throws Exception {
        String requestBody = objectMapper.writeValueAsString(orderReq);
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BackendConstants.BACKEND_URL + "/api/v1/orders"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody));
        if (token != null && !token.isEmpty()) requestBuilder.header("Authorization", "Bearer " + token);
        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            if (response.body() != null && !response.body().isEmpty()) {
                return objectMapper.readValue(response.body(), Order.class);
            }
            return new Order(); // Alternatively could attempt to map something else
        }
        throw new RuntimeException("Failed to create order: " + response.statusCode() + " Body: " + response.body());
    }

    public void updateOrderStatus(Long orderId, java.util.Map<String, String> statusMap, String token) throws Exception {
        String requestBody = objectMapper.writeValueAsString(statusMap);
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BackendConstants.BACKEND_URL + "/api/v1/orders/" + orderId + "/status"))
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(requestBody));
        if (token != null && !token.isEmpty()) requestBuilder.header("Authorization", "Bearer " + token);
        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Failed to update order status: " + response.statusCode());
        }
    }

    public void linkShipment(Long orderId, java.util.Map<String, Long> payload, String token) throws Exception {
        String requestBody = objectMapper.writeValueAsString(payload);
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BackendConstants.BACKEND_URL + "/api/v1/orders/" + orderId + "/shipment"))
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(requestBody));
        if (token != null && !token.isEmpty()) requestBuilder.header("Authorization", "Bearer " + token);
        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Failed to link shipment: " + response.statusCode());
        }
    }

    public void deleteOrder(Long orderId, String token) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BackendConstants.BACKEND_URL + "/api/v1/orders/" + orderId))
                .DELETE();
        if (token != null && !token.isEmpty()) requestBuilder.header("Authorization", "Bearer " + token);
        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Failed to delete order: " + response.statusCode());
        }
    }

    // --- STORE SERVICE ---
    public List<Store> getAllStores(String token) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BackendConstants.BACKEND_URL + "/api/v1/stores"))
                .header("Accept", "application/json")
                .GET();
        if (token != null && !token.isEmpty()) requestBuilder.header("Authorization", "Bearer " + token);
        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Store>>(){});
        }
        throw new RuntimeException("Failed to fetch stores: " + response.statusCode());
    }

    public Store getStoreById(Long id, String token) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BackendConstants.BACKEND_URL + "/api/v1/stores/" + id))
                .header("Accept", "application/json")
                .GET();
        if (token != null && !token.isEmpty()) requestBuilder.header("Authorization", "Bearer " + token);
        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), Store.class);
        }
        throw new RuntimeException("Store not found: " + response.statusCode());
    }

    public List<Store> searchStoresByAddress(String query, String token) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BackendConstants.BACKEND_URL + "/api/v1/stores/search/address?q=" + java.net.URLEncoder.encode(query, "UTF-8")))
                .header("Accept", "application/json")
                .GET();
        if (token != null && !token.isEmpty()) requestBuilder.header("Authorization", "Bearer " + token);
        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Store>>(){});
        }
        throw new RuntimeException("Failed to search stores: " + response.statusCode());
    }

    public Store createStore(Store store, String token) throws Exception {
        String requestBody = objectMapper.writeValueAsString(store);
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BackendConstants.BACKEND_URL + "/api/v1/stores"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody));
        if (token != null && !token.isEmpty()) requestBuilder.header("Authorization", "Bearer " + token);
        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            if (response.body() != null && !response.body().isEmpty()) {
                return objectMapper.readValue(response.body(), Store.class);
            }
            return store;
        }
        throw new RuntimeException("Failed to create store: " + response.statusCode() + " Body: " + response.body());
    }

    public Store updateStore(Long id, Store store, String token) throws Exception {
        String requestBody = objectMapper.writeValueAsString(store);
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BackendConstants.BACKEND_URL + "/api/v1/stores/" + id))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody));
        if (token != null && !token.isEmpty()) requestBuilder.header("Authorization", "Bearer " + token);
        HttpRequest request = requestBuilder.build();
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
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BackendConstants.BACKEND_URL + "/api/v1/stores/" + id))
                .DELETE();
        if (token != null && !token.isEmpty()) requestBuilder.header("Authorization", "Bearer " + token);
        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Failed to delete store: " + response.statusCode());
        }
    }

    // --- INVENTORY SERVICE ---
    public List<Inventory> getAllInventory(String token) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BackendConstants.BACKEND_URL + "/api/v1/inventory"))
                .header("Accept", "application/json")
                .GET();
        if (token != null && !token.isEmpty()) requestBuilder.header("Authorization", "Bearer " + token);
        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Inventory>>(){});
        } else if (response.statusCode() == 404) {
            return new java.util.ArrayList<>();
        }
        throw new RuntimeException("Failed to fetch all inventory: " + response.statusCode());
    }

    public List<Inventory> getInventoryByStoreId(Long storeId, String token) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BackendConstants.BACKEND_URL + "/api/v1/inventory/store/" + storeId))
                .header("Accept", "application/json")
                .GET();
        if (token != null && !token.isEmpty()) requestBuilder.header("Authorization", "Bearer " + token);
        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Inventory>>(){});
        } else if (response.statusCode() == 404) {
            return new java.util.ArrayList<>();
        }
        throw new RuntimeException("Failed to fetch inventory for store: " + response.statusCode());
    }

    public List<Inventory> getInventoryByProductId(Long productId, String token) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BackendConstants.BACKEND_URL + "/api/v1/inventory/product/" + productId))
                .header("Accept", "application/json")
                .GET();
        if (token != null && !token.isEmpty()) requestBuilder.header("Authorization", "Bearer " + token);
        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Inventory>>(){});
        } else if (response.statusCode() == 404) {
            return new java.util.ArrayList<>();
        }
        throw new RuntimeException("Failed to fetch inventory for product: " + response.statusCode());
    }

    public void addInventory(Inventory inventory, String token) throws Exception {
        String requestBody = objectMapper.writeValueAsString(inventory);
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BackendConstants.BACKEND_URL + "/api/v1/inventory"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody));
        if (token != null && !token.isEmpty()) requestBuilder.header("Authorization", "Bearer " + token);
        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Failed to create inventory: " + response.statusCode() + " Body: " + response.body());
        }
    }

    public void updateStock(java.util.Map<String, Object> payload, boolean isAdd, String token) throws Exception {
        String endpoint = isAdd ? "/api/v1/inventory/add" : "/api/v1/inventory/reduce";
        String requestBody = objectMapper.writeValueAsString(payload);
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BackendConstants.BACKEND_URL + endpoint))
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(requestBody));
        if (token != null && !token.isEmpty()) requestBuilder.header("Authorization", "Bearer " + token);
        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Failed to update stock: " + response.statusCode() + " Body: " + response.body());
        }
    }

    public void deleteInventory(Long storeId, Long productId, String token) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BackendConstants.BACKEND_URL + "/api/v1/inventory/store/" + storeId + "/product/" + productId))
                .DELETE();
        if (token != null && !token.isEmpty()) requestBuilder.header("Authorization", "Bearer " + token);
        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Failed to delete inventory: " + response.statusCode());
        }
    }

    // --- SHIPPING SERVICE ---
    public List<com.inventory.model.Shipment> getAllShipments(String token) throws Exception {
        return fetchList(BackendConstants.BACKEND_URL + "/api/v1/shipments", token, new TypeReference<List<com.inventory.model.Shipment>>(){});
    }

    public com.inventory.model.Shipment getShipmentById(Long id, String token) throws Exception {
        return fetchObject(BackendConstants.BACKEND_URL + "/api/v1/shipments/" + id, token, com.inventory.model.Shipment.class);
    }

    public List<com.inventory.model.Shipment> getShipmentsByCustomer(Long customerId, String token) throws Exception {
        return fetchList(BackendConstants.BACKEND_URL + "/api/v1/shipments/customer/" + customerId, token, new TypeReference<List<com.inventory.model.Shipment>>(){});
    }

    public List<com.inventory.model.Shipment> getShipmentsByStore(Long storeId, String token) throws Exception {
        return fetchList(BackendConstants.BACKEND_URL + "/api/v1/shipments/store/" + storeId, token, new TypeReference<List<com.inventory.model.Shipment>>(){});
    }

    public com.inventory.model.Shipment createShipment(com.inventory.model.Shipment payload, String token) throws Exception {
        String requestBody = objectMapper.writeValueAsString(payload);
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BackendConstants.BACKEND_URL + "/api/v1/shipments"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody));
        if (token != null && !token.isEmpty()) requestBuilder.header("Authorization", "Bearer " + token);
        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return objectMapper.readValue(response.body(), com.inventory.model.Shipment.class);
        }
        String message = extractErrorMessage(response.body(), response.statusCode());
        throw new RuntimeException(message);
    }

    public com.inventory.model.Shipment updateShipmentStatus(Long id, String status, String token) throws Exception {
        // Backend takes @RequestBody ShipmentStatus Enum, quotes force string-to-enum mapping
        String requestBody = objectMapper.writeValueAsString(status);
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BackendConstants.BACKEND_URL + "/api/v1/shipments/" + id + "/status"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(requestBody));
        if (token != null && !token.isEmpty()) requestBuilder.header("Authorization", "Bearer " + token);
        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return objectMapper.readValue(response.body(), com.inventory.model.Shipment.class);
        }
        String message = extractErrorMessage(response.body(), response.statusCode());
        throw new RuntimeException(message);
    }

    public void deleteShipment(Long id, String token) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BackendConstants.BACKEND_URL + "/api/v1/shipments/" + id))
                .DELETE();
        if (token != null && !token.isEmpty()) requestBuilder.header("Authorization", "Bearer " + token);
        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 300) {
            String message = extractErrorMessage(response.body(), response.statusCode());
            throw new RuntimeException(message);
        }
    }

    private String extractErrorMessage(String body, int statusCode) {
        try {
            if (body != null && !body.isEmpty() && body.startsWith("{")) {
                com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(body);
                if (node.has("message")) {
                    return node.get("message").asText();
                }
            }
        } catch (Exception ignored) {}
        return "Failed with status: " + statusCode;
    }

    // Helper methods for clean HTTP calls returning 404 cleanly for Lists
    private <T> List<T> fetchList(String url, String token, TypeReference<List<T>> typeRef) throws Exception {
        HttpRequest.Builder rb = HttpRequest.newBuilder().uri(URI.create(url)).GET().header("Accept", "application/json");
        if (token != null && !token.isEmpty()) rb.header("Authorization", "Bearer " + token);
        HttpResponse<String> response = httpClient.send(rb.build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), typeRef);
        } else if (response.statusCode() == 404) {
            return new java.util.ArrayList<>();
        }
        throw new RuntimeException("Failed to fetch list from " + url + " - Error: " + response.statusCode());
    }

    private <T> T fetchObject(String url, String token, Class<T> clazz) throws Exception {
        HttpRequest.Builder rb = HttpRequest.newBuilder().uri(URI.create(url)).GET().header("Accept", "application/json");
        if (token != null && !token.isEmpty()) rb.header("Authorization", "Bearer " + token);
        HttpResponse<String> response = httpClient.send(rb.build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), clazz);
        } else if (response.statusCode() == 404) {
            throw new RuntimeException(String.valueOf(response.statusCode()));
        }
        throw new RuntimeException("Failed to fetch object from " + url + " - Error: " + response.statusCode());
    }
}
