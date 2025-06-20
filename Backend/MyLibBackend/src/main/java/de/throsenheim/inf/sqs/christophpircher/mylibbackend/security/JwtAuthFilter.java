package de.throsenheim.inf.sqs.christophpircher.mylibbackend.security;

import de.throsenheim.inf.sqs.christophpircher.mylibbackend.service.CustomUserDetailsService;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.service.JwtService;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.service.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
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
    protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String token = null;
        UUID userID = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            userID = jwtService.extractUserID(token);
        }

        if (userID != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserPrincipal userDetails = (UserPrincipal) userDetailsService.loadByUserID(userID);
            if (Boolean.TRUE.equals(jwtService.validateToken(token, userDetails))) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
