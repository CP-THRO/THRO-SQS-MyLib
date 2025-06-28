package de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.repository;

import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.LibraryBook;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.LibraryBookKey;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface LibraryBookRepository extends JpaRepository<LibraryBook, LibraryBookKey> {
    @Transactional(readOnly = true)
    List<LibraryBook> getLibraryBooksByUser(User user, Pageable pageable);

    @Transactional(readOnly = true)
    Optional<LibraryBook> getLibraryBooksById(LibraryBookKey id);

    @Transactional(readOnly = true)
    long countByUser(User user);
}
