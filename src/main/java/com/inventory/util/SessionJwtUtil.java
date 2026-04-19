package com.inventory.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class SessionJwtUtil {
    private static final String JWT_SESSION_KEY = "JWT_TOKEN";

    public static void setJwt(HttpServletRequest request, String token) {
        HttpSession session = request.getSession(true);
        session.setAttribute(JWT_SESSION_KEY, token);
    }

    public static String getJwt(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (String) session.getAttribute(JWT_SESSION_KEY);
        }
        return null;
    }

    public static void clearJwt(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(JWT_SESSION_KEY);
        }
    }
}
