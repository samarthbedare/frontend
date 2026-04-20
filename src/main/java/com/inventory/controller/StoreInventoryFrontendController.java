package com.inventory.controller;

import com.inventory.model.Inventory;
import com.inventory.model.Store;
import com.inventory.service.InventoryServiceClient;
import com.inventory.service.StoreServiceClient;
import com.inventory.util.SessionJwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/frontend/stores")
public class StoreInventoryFrontendController {

    private final StoreServiceClient storeServiceClient;
    private final InventoryServiceClient inventoryServiceClient;

    @Autowired
    public StoreInventoryFrontendController(StoreServiceClient storeServiceClient, InventoryServiceClient inventoryServiceClient) {
        this.storeServiceClient = storeServiceClient;
        this.inventoryServiceClient = inventoryServiceClient;
    }

    @GetMapping
    public String viewStores(HttpServletRequest request, Model model) {
        String token = SessionJwtUtil.getJwt(request);
        try {
            List<Store> stores = storeServiceClient.getAllStores(token);
            model.addAttribute("stores", stores);
            return "stores/stores";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to fetch stores: " + e.getMessage());
            if (e.getMessage() != null && (e.getMessage().contains("403") || e.getMessage().contains("401"))) {
                return "redirect:/login?reqLogin=true";
            }
            return "stores/stores";
        }
    }

    @GetMapping("/search")
    public String searchStoreByAddress(@RequestParam(value = "address") String address, HttpServletRequest request, Model model) {
        String token = SessionJwtUtil.getJwt(request);
        try {
            List<Store> stores = storeServiceClient.searchStoresByAddress(address, token);
            model.addAttribute("stores", stores);
            return "stores/stores";
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("403") || e.getMessage().contains("401"))) {
                return "redirect:/login?reqLogin=true";
            }
            model.addAttribute("error", "Failed to search stores: " + e.getMessage());
            return "stores/stores";
        }
    }

    @GetMapping("/{id}")
    public String viewStoreDetails(@PathVariable Long id, HttpServletRequest request, Model model, RedirectAttributes redirectAttrs) {
        String token = SessionJwtUtil.getJwt(request);
        try {
            Store store = storeServiceClient.getStoreById(id, token);
            List<Inventory> inventoryList = inventoryServiceClient.getInventoryByStoreId(id, token);
            model.addAttribute("store", store);
            model.addAttribute("inventoryList", inventoryList);
            return "stores/store-details";
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("403") || e.getMessage().contains("401"))) {
                return "redirect:/login?reqLogin=true";
            }
            redirectAttrs.addFlashAttribute("error", "Store Not Found: " + e.getMessage());
            return "redirect:/frontend/stores";
        }
    }

    @PostMapping
    public String createStore(@ModelAttribute Store store, HttpServletRequest request, RedirectAttributes redirectAttrs) {
        String token = SessionJwtUtil.getJwt(request);
        try {
            storeServiceClient.createStore(store, token);
            redirectAttrs.addFlashAttribute("successMessage", "Store created successfully.");
            return "redirect:/frontend/stores";
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("403") || e.getMessage().contains("401"))) {
                return "redirect:/login?reqLogin=true";
            }
            redirectAttrs.addFlashAttribute("error", "Failed to create store: " + e.getMessage());
            return "redirect:/frontend/stores";
        }
    }

    @PostMapping("/{id}/update")
    public String updateStore(@PathVariable Long id, @ModelAttribute Store store, HttpServletRequest request, RedirectAttributes redirectAttrs) {
        String token = SessionJwtUtil.getJwt(request);
        try {
            storeServiceClient.updateStore(id, store, token);
            redirectAttrs.addFlashAttribute("successMessage", "Store updated successfully.");
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("403") || e.getMessage().contains("401"))) {
                return "redirect:/login?reqLogin=true";
            }
            redirectAttrs.addFlashAttribute("error", "Failed to update store: " + e.getMessage());
        }
        return "redirect:/frontend/stores/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteStore(@PathVariable Long id, HttpServletRequest request, RedirectAttributes redirectAttrs) {
        String token = SessionJwtUtil.getJwt(request);
        try {
            storeServiceClient.deleteStore(id, token);
            redirectAttrs.addFlashAttribute("successMessage", "Store deleted securely.");
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("403") || e.getMessage().contains("401"))) {
                return "redirect:/login?reqLogin=true";
            }
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("Database constraint violated")) {
                errorMessage = "Cannot delete this store because it is currently linked to active inventory records or historical order data. Please remove all stock and resolve linked orders first.";
            }
            redirectAttrs.addFlashAttribute("error", "Failed to delete store: " + errorMessage);
        }
        return "redirect:/frontend/stores";
    }

    // -- INVENTORY ACTIONS (Routed through store view typically) --
    
    @PostMapping("/{storeId}/inventory")
    public String addInventory(@PathVariable Integer storeId, @ModelAttribute Inventory inventory, HttpServletRequest request, RedirectAttributes redirectAttrs) {
        String token = SessionJwtUtil.getJwt(request);
        try {
            inventory.setStoreId(storeId);
            inventoryServiceClient.addInventory(inventory, token);
            redirectAttrs.addFlashAttribute("successMessage", "Inventory initialized successfully.");
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("403") || e.getMessage().contains("401"))) {
                return "redirect:/login?reqLogin=true";
            }
            redirectAttrs.addFlashAttribute("error", "Failed to add inventory: " + e.getMessage());
        }
        return "redirect:/frontend/stores/" + storeId;
    }

    @PostMapping("/{storeId}/inventory/updateStock")
    public String updateStock(@PathVariable Integer storeId, 
                              @RequestParam("productId") Integer productId, 
                              @RequestParam("quantity") Integer quantity, 
                              @RequestParam("action") String action, // "add" or "reduce"
                              HttpServletRequest request, RedirectAttributes redirectAttrs) {
        String token = SessionJwtUtil.getJwt(request);
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("storeId", storeId);
            payload.put("productId", productId);
            payload.put("quantity", quantity);
            boolean isAdd = "add".equalsIgnoreCase(action);
            
            inventoryServiceClient.updateStock(payload, isAdd, token);
            redirectAttrs.addFlashAttribute("successMessage", "Stock updated successfully.");
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("403") || e.getMessage().contains("401"))) {
                return "redirect:/login?reqLogin=true";
            }
            redirectAttrs.addFlashAttribute("error", "Failed to update stock: " + e.getMessage());
        }
        return "redirect:/frontend/stores/" + storeId;
    }

    @PostMapping("/{storeId}/inventory/delete")
    public String deleteInventory(@PathVariable Long storeId, @RequestParam("productId") Long productId, HttpServletRequest request, RedirectAttributes redirectAttrs) {
        String token = SessionJwtUtil.getJwt(request);
        try {
            inventoryServiceClient.deleteInventory(storeId, productId, token);
            redirectAttrs.addFlashAttribute("successMessage", "Inventory item removed securely.");
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("403") || e.getMessage().contains("401"))) {
                return "redirect:/login?reqLogin=true";
            }
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("Database constraint violated")) {
                errorMessage = "Cannot remove this inventory item because it is referenced in one or more order line items. Please resolve the linked orders before removing this stock record.";
            }
            redirectAttrs.addFlashAttribute("error", "Failed to delete inventory: " + errorMessage);
        }
        return "redirect:/frontend/stores/" + storeId;
    }
}
