package com.redhat.restdemo;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.restdemo.model.entity.Author;
import com.redhat.restdemo.model.entity.Authorship;
import com.redhat.restdemo.model.entity.Book;
import com.redhat.restdemo.model.entity.Library;
import com.redhat.restdemo.model.entity.Ownership;
import com.redhat.restdemo.model.repository.BookRepository;
import com.redhat.restdemo.model.repository.LibraryRepository;
import com.redhat.restdemo.model.repository.OwnershipRepository;
import com.redhat.restdemo.utils.TestData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class OwnershipEndpointTests extends EndpointTestTemplate {
    @Autowired
    private OwnershipRepository ownershipRepository;

    @Autowired
    private LibraryRepository libraryRepository;

    @Autowired
    private BookRepository bookRepository;

    private List<Ownership> ownerships;

    private void prepareLibraryBookSchemas() {
        for (Map.Entry<Book, Library> entry : TestData.ownership.entrySet()) {
            Integer bookId = bookRepository.save(entry.getKey()).getId();
            Integer libraryId = libraryRepository.save(entry.getValue()).getId();
            Ownership ownership = new Ownership(bookId, libraryId);
            ownershipRepository.save(ownership);
            ownerships.add(ownership);
        }
    }

    @BeforeEach
    public void cleanReposAndOwnership() {
        ownerships = new ArrayList<>();
        ownershipRepository.deleteAll();
    }

    @Test
    void testGetOwnershipsEndpoint() throws JsonProcessingException {
        prepareLibraryBookSchemas();

        String ownershipUrl = createURLWithPort("/ownership");

        ResponseEntity<String> response = testRequests.get(ownershipUrl);

        ObjectMapper objectMapper = new ObjectMapper();
        List<Ownership> bookOwnership = objectMapper.readValue(response.getBody(), new TypeReference<>() {
        });

        assertThat(bookOwnership.size(), is(ownerships.size()));

        for (Ownership ownership : bookOwnership) {
            assertThat(ownerships.contains(ownership), is(true));
        }
    }

}
