package de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.repository;

import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BookRepository extends JpaRepository<Book, UUID> {
    List<Book> getBookByBookID(String bookID);
}
