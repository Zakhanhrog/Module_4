package com.example.lunchapp.interceptor;

import com.example.lunchapp.model.dto.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AdminAuthInterceptor.class);
    private static final String LOGGED_IN_USER_SESSION_KEY = "loggedInUser";
    private static final String ADMIN_ROLE_NAME = "ROLE_ADMIN";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute(LOGGED_IN_USER_SESSION_KEY) == null) {
            logger.warn("Admin access attempt without login. Redirecting to login page. URI: {}", request.getRequestURI());
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return false;
        }

        UserDto loggedInUser = (UserDto) session.getAttribute(LOGGED_IN_USER_SESSION_KEY);
        if (loggedInUser.getRoles() == null || !loggedInUser.getRoles().contains(ADMIN_ROLE_NAME)) {
            logger.warn("Non-admin user {} attempting to access admin page. URI: {}. Redirecting to home.", loggedInUser.getUsername(), request.getRequestURI());
            response.sendRedirect(request.getContextPath() + "/"); // Hoặc trang lỗi 403
            return false;
        }

        logger.debug("Admin user {} granted access to admin page. URI: {}", loggedInUser.getUsername(), request.getRequestURI());
        return true; // Cho phép request tiếp tục
    }
}