package com.inventory.controller;

import com.inventory.model.Inventory;
import com.inventory.service.InventoryServiceClient;
import com.inventory.util.SessionJwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/frontend/inventory")
public class InventoryFrontendController {

    private final InventoryServiceClient inventoryServiceClient;

    @Autowired
    public InventoryFrontendController(InventoryServiceClient inventoryServiceClient) {
        this.inventoryServiceClient = inventoryServiceClient;
    }

    @GetMapping
    public String viewInventory(@RequestParam(value = "productId", required = false) Long productId,
                                @RequestParam(value = "storeId", required = false) Long storeId,
                                HttpServletRequest request, Model model) {
        String token = SessionJwtUtil.getJwt(request);
        try {
            if (productId != null) {
                List<Inventory> inventoryList = inventoryServiceClient.getInventoryByProductId(productId, token);
                model.addAttribute("inventoryList", inventoryList);
            } else if (storeId != null) {
                List<Inventory> inventoryList = inventoryServiceClient.getInventoryByStoreId(storeId, token);
                model.addAttribute("inventoryList", inventoryList);
            } else {
                List<Inventory> inventoryList = inventoryServiceClient.getAllInventory(token);
                model.addAttribute("inventoryList", inventoryList);
            }
            return "inventory/inventory";
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("403") || e.getMessage().contains("401"))) {
                return "redirect:/login?reqLogin=true";
            }
            model.addAttribute("error", "Failed to fetch inventory: " + e.getMessage());
            return "inventory/inventory";
        }
    }
}
