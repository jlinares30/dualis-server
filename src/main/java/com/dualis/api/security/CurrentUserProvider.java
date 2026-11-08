package com.dualis.api.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Resolves the identity of the authenticated user from the JWT-populated SecurityContext.
 * The user identity must ALWAYS come from here, never from request bodies or query params.
 */
@Component
public class CurrentUserProvider {

    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken
                || authentication.getName() == null
                || authentication.getName().isBlank()) {
            throw new AuthenticationCredentialsNotFoundException("Authentication is required");
        }
        return authentication.getName().trim().toLowerCase(Locale.ROOT);
    }
}
