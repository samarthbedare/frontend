package com.inventory.controller;

import com.inventory.model.Member;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
public class TeamController {

    // ── Team data (replace with DB/Service later) ──────────────────────────
    private static final List<Member> MEMBERS = Arrays.asList(
            new Member(1, "Vishal Gavali", "Customer Service", "vishal", "VG", "/img/vishal.jpeg",
                    "Specializes in customer identity management and profile orchestration, ensuring secure and valid customer data across the system.",
                    Arrays.asList("Implemented customer validation logic", "Designed profile management API",
                            "Integrated identity verification patterns"),
                    "Customer Management",
                    Arrays.asList("GET /api/v1/customers", "GET /api/v1/customers/{id}",
                            "GET /api/v1/customers/email/{email}", "POST /api/v1/customers",
                            "PUT /api/v1/customers/{id}", "DELETE /api/v1/customers/{id}",
                            "GET /api/v1/customers/validate/{id}")),

            new Member(2, "Narayani Gupta", "Product Service", "narayani", "NG", "/img/narayani.jpeg",
                    "Expert in robust catalog management systems, focusing on performance-optimized product filtering and advanced attribute tracking.",
                    Arrays.asList("Developed product filtering by brand and size",
                            "Created robust catalog management system",
                            "Implemented PATCH updates for product attributes"),
                    "Product Catalog",
                    Arrays.asList("GET /api/v1/products", "GET /api/v1/products/{id}", "POST /api/v1/products",
                            "PATCH /api/v1/products/{id}", "DELETE /api/v1/products/{id}")),

            new Member(3, "Samarth Bedare", "Order Service", "samarth", "SB", "/img/samarth.png",
                    "Coordinates the transition of items from inventory to customer ownership, managing the entire complex order lifecycle.",
                    Arrays.asList("Designed Atomic Checkout logic", "Managed complex order status transitions",
                            "Implemented historical order retrieval"),
                    "Order Orchestration",
                    Arrays.asList("GET /api/v1/orders", "GET /api/v1/orders/{id}", "GET /api/v1/orders/customer/{cid}",
                            "GET /api/v1/orders/store/{sid}", "POST /api/v1/orders", "PATCH /api/v1/orders/{id}/status",
                            "PATCH /api/v1/orders/{id}/shipment", "DELETE /api/v1/orders/{id}")),

            new Member(4, "Priya Chavan", "Store & Inventory Service", "priya", "PC", "/img/priya.jpeg",
                    "Orchestrates stock levels across multiple physical locations, supporting atomic stock operations during the fulfillment process.",
                    Arrays.asList("Implemented multi-store inventory tracking",
                            "Atomic stock reduction during checkout", "Developed Replenishment logic"),
                    "Inventory Management",
                    Arrays.asList("GET /api/v1/stores", "GET /api/v1/stores/{id}", "GET /api/v1/stores/search/address",
                            "POST /api/v1/stores", "PUT /api/v1/stores/{id}", "DELETE /api/v1/stores/{id}",
                            "GET /api/v1/inventory", "GET /api/v1/inventory/product/{pid}", "GET /api/v1/inventory/store/{sid}",
                            "POST /api/v1/inventory", "PATCH /api/v1/inventory/add", "PATCH /api/v1/inventory/reduce",
                            "DELETE /api/v1/inventory/store/{sid}/product/{pid}")),

            new Member(5, "Rohan Kumbhar", "Shipping Service", "rohan", "RK", "/img/rohan.jpeg",
                    "Tracks the logistics component of orders, providing real-time status updates from creation to final delivery.",
                    Arrays.asList("Implemented real-time shipment tracking", "Designed delivery status update logic",
                            "Integrated shipment logistics with order system"),
                    "Shipment Tracking",
                    Arrays.asList("GET /api/v1/shipments", "GET /api/v1/shipments/{id}",
                            "GET /api/v1/shipments/customer/{cid}", "GET /api/v1/shipments/store/{sid}",
                            "POST /api/v1/shipments", "PATCH /api/v1/shipments/{id}/status",
                            "DELETE /api/v1/shipments/{id}")));

    private static final Map<String, Member> MEMBER_MAP = Map.of(
            "vishal", MEMBERS.get(0),
            "narayani", MEMBERS.get(1),
            "samarth", MEMBERS.get(2),
            "priya", MEMBERS.get(3),
            "rohan", MEMBERS.get(4));

    // ── Page 1 : Landing ───────────────────────────────────────────────────
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("projectName", "Order Inventory Management");
        model.addAttribute("members", MEMBERS);
        return "index"; // → templates/index.html
    }

    // ── Page 2 : Team overview ─────────────────────────────────────────────
    @GetMapping("/team")
    public String team(Model model) {
        model.addAttribute("projectName", "Order Inventory Management");
        model.addAttribute("members", MEMBERS);
        return "team"; // → templates/team.html
    }

    // ── Individual work page ───────────────────────────────────────────────
    @GetMapping("/work/{slug}")
    public String work(@PathVariable String slug, Model model) {
        Member member = MEMBER_MAP.get(slug);
        if (member == null) {
            return "redirect:/team";
        }
        model.addAttribute("member", member);
        model.addAttribute("projectName", "Order Inventory Management");
        return "work"; // → templates/work.html
    }
}