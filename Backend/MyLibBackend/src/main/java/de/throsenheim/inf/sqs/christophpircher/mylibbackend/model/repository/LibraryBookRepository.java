package de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.repository;

import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.Book;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.LibraryBook;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.LibraryBookKey;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LibraryBookRepository extends JpaRepository<LibraryBook, LibraryBookKey> {
    List<LibraryBook> getLibraryBooksByUser(User user, Pageable pageable);

    Optional<LibraryBook> getLibraryBooksById(LibraryBookKey id);

}
