package de.throsenheim.inf.sqs.christophpircher.mylibbackend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Extra class for the password encoder, to avoid a circular redundancy between SecurityConfig and CustomUserDetailsService
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * Password encoder bean (uses BCrypt hashing)
     * Critical for secure password storage
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
