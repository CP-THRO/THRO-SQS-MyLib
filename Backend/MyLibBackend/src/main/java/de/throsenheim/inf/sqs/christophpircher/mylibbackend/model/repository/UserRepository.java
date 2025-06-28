package de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.repository;

import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    @Transactional(readOnly = true)
    User getUserByUsername(String username);

    @Transactional(readOnly = true)
    User getUserById(UUID id);
}
