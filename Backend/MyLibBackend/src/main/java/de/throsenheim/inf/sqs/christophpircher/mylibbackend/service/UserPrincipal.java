package de.throsenheim.inf.sqs.christophpircher.mylibbackend.service;

import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Custom implementation of {@link UserDetails} for Spring Security.
 * <p>
 * Wraps the {@link User} entity and adapts it to the Spring Security framework.
 * </p>
 */
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    /**
     * The underlying {@link User} entity. Marked transient to avoid serialization issues.
     */
    @Getter
    private transient User user;

    /**
     * Returns the authorities granted to the user. This implementation returns an empty list,
     * meaning the user has no roles or permissions.
     *
     * @return an empty list of authorities
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    /**
     * Returns the user's password hash.
     *
     * @return the bcrypt-hashed password
     */
    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    /**
     * Returns the username of the user.
     *
     * @return the username
     */
    @Override
    public String getUsername() {
        return user.getUsername();
    }

    /**
     * Returns the unique user ID.
     *
     * @return UUID of the user
     */
    public UUID getUserID() {
        return user.getId();
    }
}
