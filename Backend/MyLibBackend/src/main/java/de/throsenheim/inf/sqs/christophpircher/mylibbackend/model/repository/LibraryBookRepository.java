package de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.repository;

import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.LibraryBookKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibraryBookRepository extends JpaRepository<LibraryBookRepository, LibraryBookKey> {

}
