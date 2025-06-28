package de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.repository;

import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

public interface BookRepository extends JpaRepository<Book, UUID> {
    @Transactional(readOnly = true)
    Optional<Book> getBookByBookID(String bookID);
}
