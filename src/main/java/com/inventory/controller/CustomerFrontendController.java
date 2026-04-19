package com.inventory.controller;

import com.inventory.model.Customer;
import com.inventory.service.BackendHttpClient;
import com.inventory.util.SessionJwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/frontend/customers")
public class CustomerFrontendController {

    private final BackendHttpClient backendHttpClient;

    @Autowired
    public CustomerFrontendController(BackendHttpClient backendHttpClient) {
        this.backendHttpClient = backendHttpClient;
    }

    @GetMapping
    public String viewCustomers(HttpServletRequest request, Model model) {
        String token = SessionJwtUtil.getJwt(request);
        try {
            List<Customer> customers = backendHttpClient.getAllCustomers(token);
            model.addAttribute("customers", customers);
            return "customers";
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("403") || e.getMessage().contains("401"))) {
                return "redirect:/login?reqLogin=true";
            }
            model.addAttribute("error", "Failed to fetch customers: " + e.getMessage());
            return "customers";
        }
    }

    @GetMapping("/search")
    public String searchCustomer(@RequestParam(value = "id", required = false) Long id,
                                 @RequestParam(value = "email", required = false) String email) {
        if (id != null) {
            return "redirect:/frontend/customers/" + id;
        }
        if (email != null && !email.trim().isEmpty()) {
            return "redirect:/frontend/customers/email/" + email;
        }
        return "redirect:/frontend/customers";
    }

    @GetMapping("/{id}")
    public String viewCustomerDetails(@PathVariable Long id, HttpServletRequest request, Model model, RedirectAttributes redirectAttrs) {
        String token = SessionJwtUtil.getJwt(request);
        try {
            Customer customer = backendHttpClient.getCustomerById(id, token);
            model.addAttribute("customer", customer);
            return "customer-details";
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("403") || e.getMessage().contains("401"))) {
                return "redirect:/login?reqLogin=true";
            }
            redirectAttrs.addFlashAttribute("error", "Customer Not Found");
            return "redirect:/frontend/customers";
        }
    }

    @GetMapping("/email/{email}")
    public String viewCustomerByEmail(@PathVariable String email, HttpServletRequest request, Model model, RedirectAttributes redirectAttrs) {
        String token = SessionJwtUtil.getJwt(request);
        try {
            Customer customer = backendHttpClient.getCustomerByEmail(email, token);
            model.addAttribute("customer", customer);
            // Reusing details view
            return "customer-details";
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("403") || e.getMessage().contains("401"))) {
                return "redirect:/login?reqLogin=true";
            }
            redirectAttrs.addFlashAttribute("error", "Customer Not Found for Email: " + email);
            return "redirect:/frontend/customers";
        }
    }

    @GetMapping("/validate")
    public String validateCustomer(@RequestParam("id") Long id, HttpServletRequest request, RedirectAttributes redirectAttrs) {
        String token = SessionJwtUtil.getJwt(request);
        try {
            boolean isValid = backendHttpClient.validateCustomer(id, token);
            if (isValid) {
                redirectAttrs.addFlashAttribute("successMessage", "Validation Passed: Customer ID " + id + " exists.");
            } else {
                redirectAttrs.addFlashAttribute("error", "Validation Failed: Customer ID " + id + " does not exist.");
            }
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("403") || e.getMessage().contains("401"))) {
                return "redirect:/login?reqLogin=true";
            }
            redirectAttrs.addFlashAttribute("error", "Validation error: " + e.getMessage());
        }
        return "redirect:/frontend/customers";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "customer-form";
    }

    @PostMapping
    public String createCustomer(@ModelAttribute Customer customer, HttpServletRequest request, RedirectAttributes redirectAttrs) {
        String token = SessionJwtUtil.getJwt(request);
        try {
            backendHttpClient.createCustomer(customer, token);
            redirectAttrs.addFlashAttribute("successMessage", "Customer profile created successfully.");
            return "redirect:/frontend/customers";
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("403") || e.getMessage().contains("401"))) {
                return "redirect:/login?reqLogin=true";
            }
            redirectAttrs.addFlashAttribute("error", "Create failed. " + e.getMessage());
            return "redirect:/frontend/customers/new";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, HttpServletRequest request, Model model, RedirectAttributes redirectAttrs) {
        String token = SessionJwtUtil.getJwt(request);
        try {
            Customer customer = backendHttpClient.getCustomerById(id, token);
            model.addAttribute("customer", customer);
            model.addAttribute("isEdit", true);
            return "customer-form";
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("403") || e.getMessage().contains("401"))) {
                return "redirect:/login?reqLogin=true";
            }
            redirectAttrs.addFlashAttribute("error", "Unable to load customer for edit.");
            return "redirect:/frontend/customers";
        }
    }

    @PostMapping("/{id}/edit")
    public String updateCustomer(@PathVariable Long id, @ModelAttribute Customer customer, HttpServletRequest request, RedirectAttributes redirectAttrs) {
        String token = SessionJwtUtil.getJwt(request);
        try {
            backendHttpClient.updateCustomer(id, customer, token);
            redirectAttrs.addFlashAttribute("successMessage", "Customer profile updated flawlessly.");
            return "redirect:/frontend/customers";
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("403") || e.getMessage().contains("401"))) {
                return "redirect:/login?reqLogin=true";
            }
            redirectAttrs.addFlashAttribute("error", "Update failed: " + e.getMessage());
            return "redirect:/frontend/customers/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteCustomer(@PathVariable Long id, HttpServletRequest request, RedirectAttributes redirectAttrs) {
        String token = SessionJwtUtil.getJwt(request);
        try {
            backendHttpClient.deleteCustomer(id, token);
            redirectAttrs.addFlashAttribute("successMessage", "Customer record safely erased.");
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("403") || e.getMessage().contains("401"))) {
                return "redirect:/login?reqLogin=true";
            }
            redirectAttrs.addFlashAttribute("error", "Failed to delete customer. " + e.getMessage());
        }
        return "redirect:/frontend/customers";
    }
}
