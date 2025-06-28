package de.throsenheim.inf.sqs.christophpircher.mylibbackend.security;

import de.throsenheim.inf.sqs.christophpircher.mylibbackend.service.CustomUserDetailsService;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.service.JwtService;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.service.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter that intercepts each HTTP request to handle JWT-based authentication.
 * This filter extracts the JWT from the Authorization header, validates it,
 * and if valid, sets the authentication in the Spring Security context.
 *
 */
@AllArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;


    /**
     * Filters incoming HTTP requests to authenticate users based on JWT tokens.
     *
     * @param request     the HTTP request to filter
     * @param response    the HTTP response
     * @param filterChain the filter chain
     * @throws ServletException if an error occurs during request filtering
     * @throws IOException      if an I/O error occurs during request processing
     */

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        String token = null;
        UUID userID = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                userID = jwtService.extractUserID(token);
                log.debug("JWT extracted from header for user ID: {}", userID);
            } catch (Exception e) {
                log.warn("Failed to extract user ID from JWT: {}", e.getMessage());
            }
        } else {
            log.debug("No valid JWT found in Authorization header");
        }

        if (userID != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserPrincipal userDetails = (UserPrincipal) userDetailsService.loadByUserID(userID);
                if (Boolean.TRUE.equals(jwtService.validateToken(token, userDetails))) { //because Sonar...
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.info("JWT authentication successful for user ID: {}", userID);
                } else {
                    log.warn("JWT validation failed for user ID: {}", userID);
                }
            } catch (Exception e) {
                log.warn("Authentication setup failed for user ID {}: {}", userID, e.getMessage());
            }
        } else if (userID != null) {
            log.debug("Authentication context already set for this request");
        }


        filterChain.doFilter(request, response);

    }
}
