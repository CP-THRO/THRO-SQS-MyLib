package de.throsenheim.inf.sqs.christophpircher.mylibbackend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration class for exposing a {@link PasswordEncoder} bean.
 * <p>
 * This is defined separately to prevent circular dependency issues between
 * {@code SecurityConfig} and {@code CustomUserDetailsService}.
 * </p>
 *
 * <p>
 * The {@link BCryptPasswordEncoder} is used as the implementation to securely hash and verify passwords.
 * </p>
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * Creates a {@link PasswordEncoder} bean that uses the BCrypt hashing algorithm.
     *
     * @return a {@link PasswordEncoder} for password hashing and verification
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
