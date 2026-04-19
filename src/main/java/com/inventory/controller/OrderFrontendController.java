package com.inventory.controller;

import com.inventory.model.Order;
import com.inventory.model.OrderRequest;
import com.inventory.model.OrderItemRequest;
import com.inventory.service.BackendHttpClient;
import com.inventory.util.SessionJwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/frontend/orders")
public class OrderFrontendController {

    private final BackendHttpClient backendHttpClient;

    @Autowired
    public OrderFrontendController(BackendHttpClient backendHttpClient) {
        this.backendHttpClient = backendHttpClient;
    }

    @GetMapping
    public String viewOrders(HttpServletRequest request, Model model) {
        String token = SessionJwtUtil.getJwt(request);
        try {
            List<Order> orders = backendHttpClient.getAllOrders(token);
            model.addAttribute("orders", orders);
            return "orders";
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("403") || e.getMessage().contains("401"))) {
                return "redirect:/login?reqLogin=true";
            }
            model.addAttribute("error", "Failed to fetch orders: " + e.getMessage());
            return "orders";
        }
    }

    @GetMapping("/search")
    public String searchOrder(@RequestParam(value = "id", required = false) Long id,
                              @RequestParam(value = "customerId", required = false) Long customerId,
                              @RequestParam(value = "storeId", required = false) Long storeId) {
        if (id != null) {
            return "redirect:/frontend/orders/" + id;
        }
        if (customerId != null) {
            return "redirect:/frontend/orders/customer/" + customerId;
        }
        if (storeId != null) {
            return "redirect:/frontend/orders/store/" + storeId;
        }
        return "redirect:/frontend/orders";
    }

    @GetMapping("/customer/{customerId}")
    public String viewOrdersByCustomer(@PathVariable Long customerId, HttpServletRequest request, Model model, RedirectAttributes redirectAttrs) {
        String token = SessionJwtUtil.getJwt(request);
        try {
            List<Order> orders = backendHttpClient.getOrdersByCustomerId(customerId, token);
            model.addAttribute("orders", orders);
            return "orders";
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("403") || e.getMessage().contains("401"))) {
                return "redirect:/login?reqLogin=true";
            }
            redirectAttrs.addFlashAttribute("error", "Failed to fetch orders for customer " + customerId);
            return "redirect:/frontend/orders";
        }
    }

    @GetMapping("/store/{storeId}")
    public String viewOrdersByStore(@PathVariable Long storeId, HttpServletRequest request, Model model, RedirectAttributes redirectAttrs) {
        String token = SessionJwtUtil.getJwt(request);
        try {
            List<Order> orders = backendHttpClient.getOrdersByStoreId(storeId, token);
            model.addAttribute("orders", orders);
            return "orders";
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("403") || e.getMessage().contains("401"))) {
                return "redirect:/login?reqLogin=true";
            }
            redirectAttrs.addFlashAttribute("error", "Failed to fetch orders for store " + storeId);
            return "redirect:/frontend/orders";
        }
    }

    @GetMapping("/{id}")
    public String viewOrderDetails(@PathVariable Long id, HttpServletRequest request, Model model, RedirectAttributes redirectAttrs) {
        String token = SessionJwtUtil.getJwt(request);
        try {
            Order order = backendHttpClient.getOrderById(id, token);
            model.addAttribute("order", order);
            return "order-details";
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("403") || e.getMessage().contains("401"))) {
                return "redirect:/login?reqLogin=true";
            }
            redirectAttrs.addFlashAttribute("error", "Order Not Found: " + e.getMessage());
            return "redirect:/frontend/orders";
        }
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("orderReq", new OrderRequest());
        return "order-form";
    }

    @PostMapping
    public String createOrder(@ModelAttribute OrderRequest orderReq, 
                              @RequestParam(value = "productId", required = false) List<Long> productIds,
                              @RequestParam(value = "quantity", required = false) List<Integer> quantities,
                              @RequestParam(value = "unitPrice", required = false) List<Double> unitPrices,
                              HttpServletRequest request, RedirectAttributes redirectAttrs) {
        String token = SessionJwtUtil.getJwt(request);
        try {
            List<OrderItemRequest> items = new ArrayList<>();
            if (productIds != null) {
                for (int i = 0; i < productIds.size(); i++) {
                    if (productIds.get(i) != null && quantities.get(i) != null && unitPrices.get(i) != null) {
                        OrderItemRequest item = new OrderItemRequest();
                        item.setProductId(productIds.get(i));
                        item.setQuantity(quantities.get(i));
                        item.setUnitPrice(unitPrices.get(i));
                        items.add(item);
                    }
                }
            }
            orderReq.setItems(items);
            backendHttpClient.createOrder(orderReq, token);
            redirectAttrs.addFlashAttribute("successMessage", "Order placed successfully.");
            return "redirect:/frontend/orders";
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("403") || e.getMessage().contains("401"))) {
                return "redirect:/login?reqLogin=true";
            }
            redirectAttrs.addFlashAttribute("error", "Failed to create order: " + e.getMessage());
            return "redirect:/frontend/orders/new";
        }
    }

    @PostMapping("/{id}/status")
    public String updateOrderStatus(@PathVariable Long id, @RequestParam("orderStatus") String orderStatus, HttpServletRequest request, RedirectAttributes redirectAttrs) {
        String token = SessionJwtUtil.getJwt(request);
        try {
            Map<String, String> payload = new HashMap<>();
            payload.put("orderStatus", orderStatus);
            backendHttpClient.updateOrderStatus(id, payload, token);
            redirectAttrs.addFlashAttribute("successMessage", "Order status updated.");
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("403") || e.getMessage().contains("401"))) {
                return "redirect:/login?reqLogin=true";
            }
            redirectAttrs.addFlashAttribute("error", "Update failed: " + e.getMessage());
        }
        return "redirect:/frontend/orders/" + id;
    }

    @PostMapping("/{id}/shipment")
    public String updateOrderShipment(@PathVariable Long id, @RequestParam("shipmentId") Long shipmentId, HttpServletRequest request, RedirectAttributes redirectAttrs) {
        String token = SessionJwtUtil.getJwt(request);
        try {
            Map<String, Long> payload = new HashMap<>();
            payload.put("shipmentId", shipmentId);
            backendHttpClient.linkShipment(id, payload, token);
            redirectAttrs.addFlashAttribute("successMessage", "Shipment linked successfully.");
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("403") || e.getMessage().contains("401"))) {
                return "redirect:/login?reqLogin=true";
            }
            redirectAttrs.addFlashAttribute("error", "Update failed: " + e.getMessage());
        }
        return "redirect:/frontend/orders/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteOrder(@PathVariable Long id, HttpServletRequest request, RedirectAttributes redirectAttrs) {
        String token = SessionJwtUtil.getJwt(request);
        try {
            backendHttpClient.deleteOrder(id, token);
            redirectAttrs.addFlashAttribute("successMessage", "Order deleted securely.");
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("403") || e.getMessage().contains("401"))) {
                return "redirect:/login?reqLogin=true";
            }
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("Database constraint violated")) {
                errorMessage = "Cannot delete this order because it is currently linked to other system records, such as an active shipment tracker. Please cancel associated shipments first.";
            }
            redirectAttrs.addFlashAttribute("error", "Failed to delete order. " + errorMessage);
        }
        return "redirect:/frontend/orders";
    }
}
