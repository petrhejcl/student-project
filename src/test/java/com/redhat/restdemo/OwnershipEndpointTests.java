package com.redhat.restdemo;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.restdemo.model.entity.Book;
import com.redhat.restdemo.model.entity.Library;
import com.redhat.restdemo.model.entity.Ownership;
import com.redhat.restdemo.model.repository.BookRepository;
import com.redhat.restdemo.model.repository.LibraryRepository;
import com.redhat.restdemo.model.repository.OwnershipRepository;
import com.redhat.restdemo.utils.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.redhat.restdemo.utils.Utils.countIterable;
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

    private void prepareOwnerships() {
        for (Map.Entry<Book, Library> entry : TestData.ownership.entrySet()) {
            Integer bookId = bookRepository.save(entry.getKey()).getId();
            Integer libraryId = libraryRepository.save(entry.getValue()).getId();
            Ownership ownership = new Ownership(bookId, libraryId);
            ownerships.add(ownership);
        }
    }

    private void prepareOwnershipsSchema() {
        prepareOwnerships();
        ownershipRepository.saveAll(ownerships);
    }

    @BeforeEach
    public void cleanReposAndOwnership() {
        ownerships = new ArrayList<>();
        ownershipRepository.deleteAll();
    }

    @Test
    void testGetAllOwnershipsEndpoint() throws JsonProcessingException {
        prepareOwnershipsSchema();

        ResponseEntity<String> response = testRequests.get(createURLWithPort("/ownership"));

        ObjectMapper objectMapper = new ObjectMapper();
        List<Ownership> bookOwnership = objectMapper.readValue(response.getBody(), new TypeReference<>() {
        });

        assertThat(bookOwnership.size(), is(ownerships.size()));

        for (Ownership ownership : bookOwnership) {
            assertThat(ownerships.contains(ownership), is(true));
        }
    }

    @Test
    void testAddOwnershipEndpoint() throws JsonProcessingException {
        prepareOwnerships();

        ObjectMapper objectMapper = new ObjectMapper();

        String addOwnershipUrl = createURLWithPort("/ownership/add");

        Iterable<Ownership> allOwnerships = ownershipRepository.findAll();
        long allOwnershipsSize = countIterable(allOwnerships);
        assertThat(allOwnershipsSize, is(0L));

        for (Ownership ownership : ownerships) {
            ResponseEntity<String> response = testRequests.post(addOwnershipUrl, ownership);
            assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
            assertThat(countIterable(ownershipRepository.findAll()), is(allOwnershipsSize + 1));
            allOwnershipsSize += 1;
            Ownership newOwnership = objectMapper.readValue(response.getBody(), new TypeReference<>() {
            });
            ownership.setId(newOwnership.getId());
        }

        for (Ownership ownership : ownershipRepository.findAll()) {
            assertThat(ownerships.contains(ownership), is(true));
        }
    }

    @Test
    void testDeleteOwnershipEndpoint() {
        prepareOwnershipsSchema();

        String deleteOwnershipUrl = createURLWithPort("/ownership/delete");

        Iterable<Ownership> allOwnerships = ownershipRepository.findAll();
        long allOwnershipSize = countIterable(ownerships);

        for (int i = 0; i < 3; i++) {
            int nonSenseId = new Random().nextInt(50000) + 100;
            ResponseEntity<String> nonSenseResponse = testRequests.delete(deleteOwnershipUrl + "/" + nonSenseId);
            assertThat(nonSenseResponse.getStatusCode().is4xxClientError(), is(true));
            assertThat(countIterable(ownershipRepository.findAll()), is(allOwnershipSize));
        }

        for (Ownership ownership : allOwnerships) {
            Integer ownershipId = ownership.getId();
            ResponseEntity<String> response = testRequests.delete(deleteOwnershipUrl + "/" + ownershipId);
            allOwnershipSize -= 1;

            assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
            assertThat(countIterable(ownershipRepository.findAll()), is(allOwnershipSize));
            assertThat(ownershipRepository.existsById(ownership.getId()), is(false));
        }
    }
}
