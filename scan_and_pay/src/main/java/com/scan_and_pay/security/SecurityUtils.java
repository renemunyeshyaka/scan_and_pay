package com.scan_and_pay.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.UUID;

public final class SecurityUtils {

    private SecurityUtils() {
        // Utility class
    }

    /**
     * Get the login of the current user.
     *
     * @return the current user login
     */
    public static Optional<String> getCurrentUserLogin() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(extractPrincipal(securityContext.getAuthentication()));
    }

    /**
     * Get the ID of the current user.
     *
     * @return the current user ID
     */
    public static Optional<UUID> getCurrentUserId() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            return Optional.of(userPrincipal.getId());
        }
        
        return Optional.empty();
    }

    /**
     * Get the JWT of the current user.
     *
     * @return the current user JWT
     */
    public static Optional<String> getCurrentUserJWT() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(securityContext.getAuthentication())
            .filter(authentication -> authentication.getCredentials() instanceof String)
            .map(authentication -> (String) authentication.getCredentials());
    }

    /**
     * Check if a user is authenticated.
     *
     * @return true if the user is authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        
        return authentication != null &&
            authentication.getAuthorities().stream()
                .noneMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ANONYMOUS"));
    }

    /**
     * Check if the current user has a specific authority (role).
     *
     * @param authority the authority to check
     * @return true if the current user has the authority, false otherwise
     */
    public static boolean hasCurrentUserThisAuthority(String authority) {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        
        return authentication != null &&
            authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(authority));
    }

    /**
     * Check if the current user has any of the specified authorities.
     *
     * @param authorities the authorities to check
     * @return true if the current user has any of the authorities, false otherwise
     */
    public static boolean hasCurrentUserAnyOfAuthorities(String... authorities) {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        
        if (authentication != null) {
            for (String authority : authorities) {
                if (authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(authority))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if the current user has none of the specified authorities.
     *
     * @param authorities the authorities to check
     * @return true if the current user has none of the authorities, false otherwise
     */
    public static boolean hasCurrentUserNoneOfAuthorities(String... authorities) {
        return !hasCurrentUserAnyOfAuthorities(authorities);
    }

    /**
     * Extract the principal from the authentication object.
     *
     * @param authentication the authentication object
     * @return the principal as string
     */
    private static String extractPrincipal(Authentication authentication) {
        if (authentication == null) {
            return null;
        } else if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails springSecurityUser = (UserDetails) authentication.getPrincipal();
            return springSecurityUser.getUsername();
        } else if (authentication.getPrincipal() instanceof String) {
            return (String) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * Get the current user's role.
     *
     * @return the current user's role
     */
    public static Optional<String> getCurrentUserRole() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            return Optional.of(userPrincipal.getRole());
        }
        
        return Optional.empty();
    }

    /**
     * Check if the current user is an admin.
     *
     * @return true if the current user is an admin, false otherwise
     */
    public static boolean isAdmin() {
        return hasCurrentUserThisAuthority("ROLE_ADMIN");
    }

    /**
     * Check if the current user is a merchant.
     *
     * @return true if the current user is a merchant, false otherwise
     */
    public static boolean isMerchant() {
        return hasCurrentUserThisAuthority("ROLE_MERCHANT");
    }

    /**
     * Check if the current user is a regular user.
     *
     * @return true if the current user is a regular user, false otherwise
     */
    public static boolean isUser() {
        return hasCurrentUserThisAuthority("ROLE_USER");
    }
}