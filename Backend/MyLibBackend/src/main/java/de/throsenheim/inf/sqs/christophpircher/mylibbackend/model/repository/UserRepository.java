package de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.repository;

import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    User getUserByUsername(String username);
}
