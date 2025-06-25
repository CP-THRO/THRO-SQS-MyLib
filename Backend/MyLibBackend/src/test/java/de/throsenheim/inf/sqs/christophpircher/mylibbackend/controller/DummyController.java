package de.throsenheim.inf.sqs.christophpircher.mylibbackend.controller;

import de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions.BookNotFoundException;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions.BookNotInLibraryException;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions.UnexpectedStatusException;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions.UsernameExistsException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/dummy")
class DummyController {

    @GetMapping("/io")
    public void throwIOException() throws IOException {
        throw new IOException("Downstream API error");
    }

    @GetMapping("/unexpected")
    public void throwUnexpectedStatusException() throws UnexpectedStatusException {
        throw new UnexpectedStatusException("Unexpected status");
    }

    @GetMapping("/conflict")
    public void throwUsernameExistsException() throws UsernameExistsException {
        throw new UsernameExistsException("Username already exists");
    }

    @GetMapping("/notfound")
    public void throwBookNotFoundException() throws BookNotFoundException {
        throw new BookNotFoundException("Book not found");
    }

    @GetMapping("/notinlibrary")
    public void throwBookNotInLibraryException() throws BookNotInLibraryException {
        throw new BookNotInLibraryException("Not in library");
    }

    @PostMapping("/validation")
    public void throwValidation(@Valid @RequestBody DummyRequest req) {
        // will fail automatically if invalid
    }

    static class DummyRequest {
        @jakarta.validation.constraints.NotBlank
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}