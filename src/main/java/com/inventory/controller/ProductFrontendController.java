package com.inventory.controller;

import com.inventory.model.Product;
import com.inventory.service.ProductServiceClient;
import com.inventory.util.SessionJwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/frontend/products")
public class ProductFrontendController {

    private final ProductServiceClient productServiceClient;

    @Autowired
    public ProductFrontendController(ProductServiceClient productServiceClient) {
        this.productServiceClient = productServiceClient;
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
            List<Product> products = productServiceClient.getAllProducts(token, name, brand, colour, size);
            model.addAttribute("products", products);
            model.addAttribute("name", name);
            model.addAttribute("brand", brand);
            model.addAttribute("colour", colour);
            model.addAttribute("size", size);
            return "products/products";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to fetch products: " + e.getMessage());
            if (e.getMessage() != null && (e.getMessage().contains("403") || e.getMessage().contains("401"))) {
                return "redirect:/login?reqLogin=true";
            }
            return "products/products";
        }
    }

    @GetMapping("/search")
    public String searchById(@org.springframework.web.bind.annotation.RequestParam(value = "id", required = false) Long id) {
        if (id != null) {
            return "redirect:/frontend/products/" + id;
        }
        return "redirect:/frontend/products";
    }

    @GetMapping("/{id}")
    public String getProductById(@PathVariable Long id,
                                HttpServletRequest request,
                                Model model,
                                RedirectAttributes redirectAttrs) {

        String token = SessionJwtUtil.getJwt(request);

        try {
            Product product = productServiceClient.getProductById(id, token);
            model.addAttribute("product", product);
            return "products/product-details";

        } catch (Exception e) {

            redirectAttrs.addFlashAttribute("error",
                    "Product Not Found: Product not found with id: " + id);

            return "redirect:/frontend/products";
        }
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("product", new Product());
        return "products/product-form";
    }

    @org.springframework.web.bind.annotation.PostMapping
    public String createProduct(@org.springframework.web.bind.annotation.ModelAttribute Product product, HttpServletRequest request, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttrs) {
        String token = SessionJwtUtil.getJwt(request);
        try {
            productServiceClient.createProduct(product, token);
            redirectAttrs.addFlashAttribute("successMessage", "Product created successfully.");
            return "redirect:/frontend/products";
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("403") || e.getMessage().contains("401"))) {
                return "redirect:/login?reqLogin=true";
            }
            redirectAttrs.addFlashAttribute("error", "Create failed: " + e.getMessage());
            return "redirect:/frontend/products/new";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@org.springframework.web.bind.annotation.PathVariable Long id, HttpServletRequest request, Model model, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttrs) {
        String token = SessionJwtUtil.getJwt(request);
        try {
            Product product = productServiceClient.getProductById(id, token);
            model.addAttribute("product", product);
            model.addAttribute("isEdit", true);
            return "products/product-form";
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("403") || e.getMessage().contains("401"))) {
                return "redirect:/login?reqLogin=true";
            }
            redirectAttrs.addFlashAttribute("error", "Product Not Found: " + e.getMessage());
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

            productServiceClient.updateProduct(id, updates, token);
            redirectAttrs.addFlashAttribute("successMessage", "Product Updated successfully.");
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
            productServiceClient.deleteProduct(id, token);
            redirectAttrs.addFlashAttribute("successMessage", "Product removed successfully.");
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("403") || e.getMessage().contains("401"))) {
                return "redirect:/login?reqLogin=true";
            }
            redirectAttrs.addFlashAttribute("error", "Product Not Found: " + e.getMessage());
        }
        return "redirect:/frontend/products";
    }
}
