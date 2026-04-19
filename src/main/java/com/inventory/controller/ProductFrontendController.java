package com.inventory.controller;

import com.inventory.model.Product;
import com.inventory.service.BackendHttpClient;
import com.inventory.util.SessionJwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/frontend/products")
public class ProductFrontendController {

    private final BackendHttpClient backendHttpClient;

    @Autowired
    public ProductFrontendController(BackendHttpClient backendHttpClient) {
        this.backendHttpClient = backendHttpClient;
    }

    @GetMapping
    public String viewProducts(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String name,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String brand,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String colour,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String size,
            HttpServletRequest request, Model model) {
        String token = SessionJwtUtil.getJwt(request);

        try {
            List<Product> products = backendHttpClient.getAllProducts(token, name, brand, colour, size);
            model.addAttribute("products", products);
            model.addAttribute("name", name);
            model.addAttribute("brand", brand);
            model.addAttribute("colour", colour);
            model.addAttribute("size", size);
            return "products";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to fetch products: " + e.getMessage());
            if (e.getMessage() != null && (e.getMessage().contains("403") || e.getMessage().contains("401"))) {
                return "redirect:/login?reqLogin=true";
            }
            return "products";
        }
    }

    @GetMapping("/search")
    public String searchById(@org.springframework.web.bind.annotation.RequestParam("id") Long id) {
        return "redirect:/frontend/products/" + id;
    }

    @GetMapping("/{id}")
    public String viewProductDetails(@org.springframework.web.bind.annotation.PathVariable Long id, HttpServletRequest request, Model model) {
        String token = SessionJwtUtil.getJwt(request);
        try {
            Product product = backendHttpClient.getProductById(id, token);
            model.addAttribute("product", product);
            return "product-details";
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("403") || e.getMessage().contains("401"))) {
                return "redirect:/login?reqLogin=true";
            }
            return "redirect:/frontend/products";
        }
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("product", new Product());
        return "product-form";
    }

    @org.springframework.web.bind.annotation.PostMapping
    public String createProduct(@org.springframework.web.bind.annotation.ModelAttribute Product product, HttpServletRequest request, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttrs) {
        String token = SessionJwtUtil.getJwt(request);
        try {
            backendHttpClient.createProduct(product, token);
            redirectAttrs.addFlashAttribute("successMessage", "Product created successfully.");
            return "redirect:/frontend/products";
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("403") || e.getMessage().contains("401"))) {
                return "redirect:/login?reqLogin=true";
            }
            return "redirect:/frontend/products/new?error=true";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@org.springframework.web.bind.annotation.PathVariable Long id, HttpServletRequest request, Model model) {
        String token = SessionJwtUtil.getJwt(request);
        try {
            Product product = backendHttpClient.getProductById(id, token);
            model.addAttribute("product", product);
            model.addAttribute("isEdit", true);
            return "product-form";
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("403") || e.getMessage().contains("401"))) {
                return "redirect:/login?reqLogin=true";
            }
            return "redirect:/frontend/products";
        }
    }

    @org.springframework.web.bind.annotation.PostMapping("/{id}/edit")
    public String updateProduct(
            @org.springframework.web.bind.annotation.PathVariable Long id,
            @org.springframework.web.bind.annotation.ModelAttribute Product product,
            HttpServletRequest request,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttrs) {
        String token = SessionJwtUtil.getJwt(request);
        try {
            java.util.Map<String, Object> updates = new java.util.HashMap<>();
            updates.put("productName", product.getProductName());
            updates.put("brand", product.getBrand());
            updates.put("colour", product.getColour());
            updates.put("size", product.getSize());
            updates.put("unitPrice", product.getUnitPrice());
            updates.put("rating", product.getRating());

            backendHttpClient.updateProduct(id, updates, token);
            redirectAttrs.addFlashAttribute("successMessage", "Product patched successfully.");
            return "redirect:/frontend/products";
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("403") || e.getMessage().contains("401"))) {
                return "redirect:/login?reqLogin=true";
            }
            redirectAttrs.addFlashAttribute("error", "Update failed: " + e.getMessage());
            return "redirect:/frontend/products/" + id + "/edit";
        }
    }

    @org.springframework.web.bind.annotation.PostMapping("/{id}/delete")
    public String deleteProduct(
            @org.springframework.web.bind.annotation.PathVariable Long id,
            HttpServletRequest request,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttrs) {
        String token = SessionJwtUtil.getJwt(request);
        try {
            backendHttpClient.deleteProduct(id, token);
            redirectAttrs.addFlashAttribute("successMessage", "Product removed successfully.");
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("403") || e.getMessage().contains("401"))) {
                return "redirect:/login?reqLogin=true";
            }
            redirectAttrs.addFlashAttribute("error", "Failed to delete product. " + e.getMessage());
        }
        return "redirect:/frontend/products";
    }
}
