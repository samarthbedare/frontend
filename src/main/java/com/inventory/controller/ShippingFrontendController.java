package com.inventory.controller;

import com.inventory.model.Shipment;
import com.inventory.service.BackendHttpClient;
import com.inventory.util.SessionJwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/frontend/shipments")
public class ShippingFrontendController {

    private final BackendHttpClient backendHttpClient;

    public ShippingFrontendController(BackendHttpClient backendHttpClient) {
        this.backendHttpClient = backendHttpClient;
    }

    @GetMapping
    public String viewShipments(@RequestParam(value = "customerId", required = false) Long customerId,
                                @RequestParam(value = "storeId", required = false) Long storeId,
                                HttpServletRequest request, Model model) {
        String token = SessionJwtUtil.getJwt(request);
        try {
            if (customerId != null) {
                List<Shipment> shipments = backendHttpClient.getShipmentsByCustomer(customerId, token);
                model.addAttribute("shipments", shipments);
                model.addAttribute("filterContext", "Customer ID: " + customerId);
            } else if (storeId != null) {
                List<Shipment> shipments = backendHttpClient.getShipmentsByStore(storeId, token);
                model.addAttribute("shipments", shipments);
                model.addAttribute("filterContext", "Store ID: " + storeId);
            } else {
                List<Shipment> shipments = backendHttpClient.getAllShipments(token);
                model.addAttribute("shipments", shipments);
                model.addAttribute("filterContext", "All Shipments"); // default message
            }
            return "shipments";
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("403") || e.getMessage().contains("401"))) {
                return "redirect:/login?reqLogin=true";
            }
            model.addAttribute("error", "Failed to fetch shipments: " + e.getMessage());
            return "shipments";
        }
    }

    @PostMapping
    public String createShipment(@ModelAttribute Shipment shipment, HttpServletRequest request, RedirectAttributes redirectAttrs) {
        String token = SessionJwtUtil.getJwt(request);
        try {
            backendHttpClient.createShipment(shipment, token);
            redirectAttrs.addFlashAttribute("successMessage", "Shipment successfully registered.");
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("403") || e.getMessage().contains("401"))) {
                return "redirect:/login?reqLogin=true";
            }
            redirectAttrs.addFlashAttribute("error", "Creation Failed: " + e.getMessage());
        }
        return "redirect:/frontend/shipments";
    }

    @GetMapping("/{id}")
    public String viewShipmentDetails(@PathVariable Long id, HttpServletRequest request, Model model, RedirectAttributes redirectAttrs) {
        String token = SessionJwtUtil.getJwt(request);
        try {
            Shipment shipment = backendHttpClient.getShipmentById(id, token);
            model.addAttribute("shipment", shipment);
            return "shipment-details";
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("403") || e.getMessage().contains("401"))) {
                return "redirect:/login?reqLogin=true";
            }
            redirectAttrs.addFlashAttribute("error", "Shipment Not Found: " + e.getMessage());
            return "redirect:/frontend/shipments";
        }
    }

    @PostMapping("/{id}/update-status")
    public String updateShipmentStatus(@PathVariable Long id, @RequestParam("status") String status, HttpServletRequest request, RedirectAttributes redirectAttrs) {
        String token = SessionJwtUtil.getJwt(request);
        try {
            backendHttpClient.updateShipmentStatus(id, status, token);
            redirectAttrs.addFlashAttribute("successMessage", "Status successfully updated to " + status + ".");
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("403") || e.getMessage().contains("401"))) {
                return "redirect:/login?reqLogin=true";
            }
            redirectAttrs.addFlashAttribute("error", "Status Update Failed: " + e.getMessage());
        }
        return "redirect:/frontend/shipments/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteShipment(@PathVariable Long id, HttpServletRequest request, RedirectAttributes redirectAttrs) {
        String token = SessionJwtUtil.getJwt(request);
        try {
            backendHttpClient.deleteShipment(id, token);
            redirectAttrs.addFlashAttribute("successMessage", "Shipment " + id + " permanently deleted.");
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("403") || e.getMessage().contains("401"))) {
                return "redirect:/login?reqLogin=true";
            }
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("Database constraint violated")) {
                errorMessage = "Cannot delete shipment because it is currently linked to one or more active orders. Please cancel or update the related orders before removing this shipment record.";
            }
            redirectAttrs.addFlashAttribute("error", "Deletion Failed: " + errorMessage);
        }
        return "redirect:/frontend/shipments";
    }
}
