package com.copilot.backend.security;

import com.copilot.backend.user.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new IllegalStateException("No authenticated user found in SecurityContext");
        }
        return user;
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}