package de.throsenheim.inf.sqs.christophpircher.mylibbackend.security;

import de.throsenheim.inf.sqs.christophpircher.mylibbackend.service.CustomUserDetailsService;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.service.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;

/**
 * Configuration class for Spring Security setup.
 * <p>
 * Defines security filters, endpoint access rules, authentication providers,
 * and integrates JWT-based authentication into the Spring Security filter chain.
 * </p>
 *
 * <p>Key features:</p>
 * <ul>
 *     <li>Permits access to Swagger/OpenAPI and public endpoints</li>
 *     <li>Protects all other endpoints using JWT authentication</li>
 *     <li>Disables CSRF and sets session policy to stateless</li>
 *     <li>Registers custom JWT filter and authentication provider</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @NonNull
    private final CustomUserDetailsService userDetailsService;
    @NonNull
    private final JwtService jwtService;
    @NonNull
    private final PasswordEncoder passwordEncoder;

    @Value("${frontend.url}")
    private String frontendURL;

    @Value("${backend.url}")
    private String backendURL;

    private static final String[] SWAGGER_WHITELIST = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
    };

    /**
     * Configures the main Spring Security filter chain.
     * <p>
     * Sets up endpoint authorization, disables CSRF, configures stateless sessions,
     * and adds a custom JWT authentication filter.
     * </p>
     *
     * @param http the HttpSecurity object to configure
     * @return the configured SecurityFilterChain bean
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF (not needed for stateless JWT)
                .csrf(AbstractHttpConfigurer::disable)

                .cors(Customizer.withDefaults())

                // Configure endpoint authorization
                .authorizeHttpRequests(auth -> auth
                        // Permit public access to OpenAPI documentation, h2 console, the auth endpoints, the search endpoints, the get book by ID endpoint and the get all known books endpoint
                        .requestMatchers(SWAGGER_WHITELIST).permitAll().requestMatchers(toH2Console()).permitAll().requestMatchers("/api/v1/auth/**", "/api/v1/search/**", "/api/v1/books/get/byID/**", "/api/v1/books/get/all").permitAll()

                        .requestMatchers("/error").permitAll()

                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )

                // Stateless session (required for JWT)
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Set custom authentication provider
                .authenticationProvider(authenticationProvider())

                // Add JWT filter before Spring Security's default filter
                .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class)

                // to enable H2 console
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));

        return http.build();
    }

    /**
     * Configures the {@link CorsFilter} used to filter for the origin of requests.
     * It is configured to allow all requests from the frontend and swagger-ui.
     * The configuration for this is set via environment variables.
     *
     * @return The configured CorsFilter bean
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(List.of(frontendURL,backendURL));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }


    /**
     * Configures the {@link AuthenticationProvider} used for authenticating users.
     * <p>
     * Uses a {@link DaoAuthenticationProvider} with the application's custom
     * {@link CustomUserDetailsService} and {@link PasswordEncoder}.
     * </p>
     *
     * @return the configured AuthenticationProvider bean
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    /**
     * Provides an instance of the custom JWT authentication filter.
     *
     * @return a new {@link JwtAuthFilter} configured with required services
     */
    @Bean
    public JwtAuthFilter jwtAuthFilter() {
        return new JwtAuthFilter(userDetailsService, jwtService);
    }

    /**
     * Provides an {@link AuthenticationManager} for programmatic authentication use,
     * such as login endpoints or token generation.
     *
     * @param config the authentication configuration
     * @return the AuthenticationManager bean
     * @throws Exception if configuration fails
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173")); // Your Vue app's origin
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}