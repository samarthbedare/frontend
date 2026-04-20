package com.inventory.controller;

import com.inventory.service.AuthServiceClient;
import com.inventory.util.SessionJwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    private final AuthServiceClient authServiceClient;

    @Autowired
    public LoginController(AuthServiceClient authServiceClient) {
        this.authServiceClient = authServiceClient;
    }

    @GetMapping("/login")
    public String showLoginForm(Model model, HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        String redirectUrl = "/team";
        
        if (referer != null && !referer.contains("/login")) {
            try {
                java.net.URI uri = new java.net.URI(referer);
                redirectUrl = uri.getPath();
                if (uri.getQuery() != null) {
                    redirectUrl += "?" + uri.getQuery();
                }
            } catch (Exception e) {
                // Fallback to default
            }
        }
        model.addAttribute("redirectUrl", redirectUrl);
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam(value = "redirectUrl", defaultValue = "/team") String redirectUrl,
            HttpServletRequest request,
            Model model) {

        try {
            String token = authServiceClient.login(username, password);
            SessionJwtUtil.setJwt(request, token);
            return "redirect:" + redirectUrl;
        } catch (Exception e) {
            model.addAttribute("error", "Invalid credentials or backend unavailable: " + e.getMessage());
            model.addAttribute("redirectUrl", redirectUrl);
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        SessionJwtUtil.clearJwt(request);
        return "redirect:/";
    }
}
