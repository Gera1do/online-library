package com.project.bookstore;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.bookstore.model.Book;
import com.project.bookstore.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Book testBook1;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();

        testBook1 = new Book(null, "Spring Boot in Action", "Craig Walls", "9781617292545", 2016, "A great book about Spring Boot");
        Book testBook2 = new Book(null, "Effective Java", "Joshua Bloch", "9780134685991", 2018, "A must-read for Java developers");

        testBook1 = bookRepository.save(testBook1);
        bookRepository.save(testBook2);
    }

    @Test
    void shouldCreateBook() throws Exception {
        Book newBook = new Book(null, "Clean Code", "Robert C. Martin", "9780132350884", 2008, "A guide to writing cleaner Java code");

        mockMvc.perform(MockMvcRequestBuilders.post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newBook)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Clean Code"));
    }

    @Test
    void shouldRetrieveAllBooks() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2)); // Fix: Check "content" array length
    }

    @Test
    void shouldRetrieveBookById() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/books/{id}", testBook1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Spring Boot in Action"));
    }

    @Test
    void shouldReturnNotFoundForNonExistentBook() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/books/{id}", 999))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateBook() throws Exception {
        testBook1.setTitle("Updated Title");

        mockMvc.perform(MockMvcRequestBuilders.put("/books/{id}", testBook1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testBook1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentBook() throws Exception {
        Book updatedBook = new Book(null, "Non-Existent Book", "Unknown", "0000000000", 2020, "Does not exist");

        mockMvc.perform(MockMvcRequestBuilders.put("/books/{id}", 999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedBook)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteBook() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/books/{id}", testBook1.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.get("/books/{id}", testBook1.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentBook() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/books/{id}", 999))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldSearchBooksByTitle() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/books/search?title=Spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Spring Boot in Action"));
    }

    @Test
    void shouldSearchBooksByAuthor() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/books/search?author=Joshua"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].author").value("Joshua Bloch"));
    }

    @Test
    void shouldReturnBadRequestForInvalidBook() throws Exception {
        Book invalidBook = new Book(null, "", "", "123", 1400, "");

        mockMvc.perform(MockMvcRequestBuilders.post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBook)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Title is required"))
                .andExpect(jsonPath("$.author").value("Author is required"))
                .andExpect(jsonPath("$.isbn").value("ISBN must be between 10 and 13 characters"))
                .andExpect(jsonPath("$.publicationYear").value("Publication year must be after 1440"));
    }
}

