package com.inventory.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String handleTypeMismatch(MethodArgumentTypeMismatchException ex, 
                                   HttpServletRequest request, 
                                   RedirectAttributes redirectAttributes) {
        
        String msg = "Invalid input: Please provide a valid numeric ID.";
        redirectAttributes.addFlashAttribute("error", msg);
        
        String referer = request.getHeader("Referer");
        // If we have a referer, go back there
        if (referer != null && !referer.isEmpty()) {
            return "redirect:" + referer;
        }

        // Guess the list page based on URI
        String uri = request.getRequestURI();
        if (uri.contains("/customers")) return "redirect:/frontend/customers";
        if (uri.contains("/products")) return "redirect:/frontend/products";
        if (uri.contains("/orders")) return "redirect:/frontend/orders";
        if (uri.contains("/stores")) return "redirect:/frontend/stores";
        if (uri.contains("/inventory")) return "redirect:/frontend/inventory";
        if (uri.contains("/shipments")) return "redirect:/frontend/shipments";

        return "redirect:/team";
    }
}
