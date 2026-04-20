package com.inventory.controller;

import com.inventory.model.Shipment;
import com.inventory.service.ShippingServiceClient;
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

    private final ShippingServiceClient shippingServiceClient;

    public ShippingFrontendController(ShippingServiceClient shippingServiceClient) {
        this.shippingServiceClient = shippingServiceClient;
    }

    // ─── LIST ────────────────────────────────────────────────────────────────

    @GetMapping
    public String viewShipments(@RequestParam(value = "customerId", required = false) Long customerId,
                                @RequestParam(value = "storeId", required = false) Long storeId,
                                HttpServletRequest request, Model model) {
        String token = SessionJwtUtil.getJwt(request);
        try {
            if (customerId != null) {
                List<Shipment> shipments = shippingServiceClient.getShipmentsByCustomer(customerId, token);
                model.addAttribute("shipments", shipments);
                model.addAttribute("filterContext", "Customer ID: " + customerId);
            } else if (storeId != null) {
                List<Shipment> shipments = shippingServiceClient.getShipmentsByStore(storeId, token);
                model.addAttribute("shipments", shipments);
                model.addAttribute("filterContext", "Store ID: " + storeId);
            } else {
                List<Shipment> shipments = shippingServiceClient.getAllShipments(token);
                model.addAttribute("shipments", shipments);
                model.addAttribute("filterContext", "All Shipments");
            }
            return "shipments/shipments";
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("403") || e.getMessage().contains("401"))) {
                return "redirect:/login?reqLogin=true";
            }
            model.addAttribute("error", "Failed to fetch shipments: " + e.getMessage());
            return "shipments/shipments";
        }
    }

    // ─── ADD FORM (GET) ───────────────────────────────────────────────────────

    @GetMapping("/add")
    public String showAddShipmentForm(Model model) {
        model.addAttribute("shipment", new Shipment());
        return "shipments/shipment-form";
    }

    // ─── ADD FORM SUBMIT (POST) ───────────────────────────────────────────────
    // Stays on the form page after submission — no redirect to the list.

 // ─── ADD FORM SUBMIT (POST) ───────────────────────────────────────────────
 // Updated to redirect to the list page on success

 @PostMapping("/add")
 public String createShipment(@ModelAttribute Shipment shipment,
                              HttpServletRequest request,
                              Model model,
                              RedirectAttributes redirectAttrs) { // 1. Add RedirectAttributes here
     String token = SessionJwtUtil.getJwt(request);
     try {
         shippingServiceClient.createShipment(shipment, token);
         
         // 2. Use FlashAttribute to pass the message to the next page
         redirectAttrs.addFlashAttribute("successMessage", "Shipment successfully registered.");
         
         // 3. Redirect to the main list page
         return "redirect:/frontend/shipments"; 
         
     } catch (Exception e) {
         if (e.getMessage() != null && (e.getMessage().contains("403") || e.getMessage().contains("401"))) {
             return "redirect:/login?reqLogin=true";
         }
         // On error, we stay on the form page to show the error and preserve user input
         model.addAttribute("shipment", shipment);
         model.addAttribute("error", "Registration Failed: " + e.getMessage());
         return "shipments/shipment-form";
     }
 }
    // ─── DETAILS ─────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    public String viewShipmentDetails(@PathVariable Long id,
                                      HttpServletRequest request,
                                      Model model,
                                      RedirectAttributes redirectAttrs) {
        String token = SessionJwtUtil.getJwt(request);
        try {
            Shipment shipment = shippingServiceClient.getShipmentById(id, token);
            model.addAttribute("shipment", shipment);
            return "shipments/shipment-details";
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("403") || e.getMessage().contains("401"))) {
                return "redirect:/login?reqLogin=true";
            }
            redirectAttrs.addFlashAttribute("error", "Shipment Not Found: " + e.getMessage());
            return "redirect:/frontend/shipments";
        }
    }

    // ─── UPDATE STATUS ────────────────────────────────────────────────────────

    @PostMapping("/{id}/update-status")
    public String updateShipmentStatus(@PathVariable Long id,
                                       @RequestParam("status") String status,
                                       HttpServletRequest request,
                                       RedirectAttributes redirectAttrs) {
        String token = SessionJwtUtil.getJwt(request);
        try {
            shippingServiceClient.updateShipmentStatus(id, status, token);
            redirectAttrs.addFlashAttribute("successMessage", "Status successfully updated to " + status + ".");
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("403") || e.getMessage().contains("401"))) {
                return "redirect:/login?reqLogin=true";
            }
            redirectAttrs.addFlashAttribute("error", "Status Update Failed: " + e.getMessage());
        }
        return "redirect:/frontend/shipments/" + id;
    }

    // ─── DELETE ───────────────────────────────────────────────────────────────

    @GetMapping("/delete")
    public String showDeleteShipmentForm(@RequestParam(value = "id", required = false) Long id, Model model) {
        // Pass the ID to the delete page if it exists in the URL
        model.addAttribute("prefilledId", id);
        return "shipments/shipment-delete";
    }

    @PostMapping("/{id}/delete")
    public String deleteShipment(@PathVariable Long id,
                                 HttpServletRequest request,
                                 RedirectAttributes redirectAttrs) {
        String token = SessionJwtUtil.getJwt(request);
        try {
            shippingServiceClient.deleteShipment(id, token);
            redirectAttrs.addFlashAttribute("successMessage", "Shipment " + id + " permanently deleted.");
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("403") || e.getMessage().contains("401"))) {
                return "redirect:/login?reqLogin=true";
            }
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("Database constraint violated")) {
                errorMessage = "Cannot delete shipment because it is currently linked to one or more active orders. " +
                               "Please cancel or update the related orders before removing this shipment record.";
            }
            redirectAttrs.addFlashAttribute("error", "Deletion Failed: " + errorMessage);
        }
        return "redirect:/frontend/shipments";
    }
}