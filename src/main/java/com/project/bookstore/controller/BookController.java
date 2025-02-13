package com.project.bookstore.controller;

import com.project.bookstore.model.Book;
import com.project.bookstore.service.AIInsightService;
import com.project.bookstore.service.BookService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/books")
public class BookController {
    private final BookService bookService;
    private final AIInsightService aiInsightService;

    public BookController(BookService bookService, AIInsightService aiInsightService) {
        this.bookService = bookService;
        this.aiInsightService = aiInsightService;
    }

    @PostMapping
    public ResponseEntity<Book> createBook(@Valid @RequestBody Book book) {
        return ResponseEntity.ok(bookService.saveBook(book));
    }

    @GetMapping
    public ResponseEntity<Page<Book>> getAllBooks(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, Pageable pageable) {
        Page<Book> books = bookService.getAllBooks(pageable);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        Book book = bookService.getBookById(id);
        return ResponseEntity.ok(book);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @Valid @RequestBody Book updatedBook) {
        Book updated = bookService.updateBook(id, updatedBook);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Book>> searchBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> books = bookService.searchBooks(title, author, pageable);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/{id}/ai-insights")
    public ResponseEntity<Map<String, String>> getAIInsights(@PathVariable Long id) {
        Book book = bookService.getBookById(id);
        String aiInsight = aiInsightService.getAIInsights(book.getDescription());
        Map<String, String> response = Map.of(
                "aiInsight", aiInsight
        );

        return ResponseEntity.ok(response);
    }
}
