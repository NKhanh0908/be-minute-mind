package com.be.minutemind.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityContextHelper {
    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null || !(auth.getPrincipal() instanceof CustomUserDetails)) {
            throw new AccessDeniedException("User not authenticated");
        }
        return ((CustomUserDetails) auth.getPrincipal()).getUserId();
    }
}
