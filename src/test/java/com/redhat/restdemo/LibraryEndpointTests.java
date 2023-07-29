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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.redhat.restdemo.utils.TestUtils.countIterable;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class LibraryEndpointTests extends EndpointTestTemplate {
    private final String baseLibraryUrl = createURLWithPort("/library");

    @Autowired
    LibraryRepository libraryRepository;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    OwnershipRepository ownershipRepository;

    public void prepareLibraryScheme() {
        libraryRepository.saveAll(TestData.libraries);
        assertThat(countIterable(libraryRepository.findAll()), is((long) TestData.books.size()));
    }

    private List<Ownership> prepareOwnershipSchema() {
        List<Ownership> ownerships = new ArrayList<>();
        for (Map.Entry<Book, Library> entry : TestData.ownership.entrySet()) {
            Integer bookId = bookRepository.save(entry.getKey()).getId();
            Integer libraryId = libraryRepository.save(entry.getValue()).getId();
            Ownership ownership = new Ownership(libraryId, bookId);
            ownerships.add(ownershipRepository.save(ownership));
        }
        assertThat(countIterable(ownershipRepository.findAll()), is((long) TestData.ownership.size()));
        return ownerships;
    }

    @Test
    void testGetAllLibrariesEndpoint() throws JsonProcessingException {
        prepareLibraryScheme();

        ResponseEntity<String> response = testRequests.get(baseLibraryUrl);

        ObjectMapper objectMapper = new ObjectMapper();

        List<Library> libraries = objectMapper.readValue(response.getBody(), new TypeReference<>() {
        });

        assertThat(libraries.size(), is(TestData.libraries.size()));

        for (Library library : TestData.libraries) {
            assertThat(libraries.contains(library), is(true));
        }
    }

    @Test
    public void testGetLibraryById() throws JsonProcessingException {
        prepareLibraryScheme();

        for (Library library : libraryRepository.findAll()) {
            Integer id = library.getId();
            ResponseEntity<String> response = testRequests.get(baseLibraryUrl + "/" + id);
            assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
            Library testLibrary = objectMapper.readValue(response.getBody(), new TypeReference<>() {
            });
            assertThat(library, is(testLibrary));
        }

        int nonSenseId = new Random().nextInt(50000) + 100;
        ResponseEntity<String> nonSenseResponse = testRequests.get(baseLibraryUrl + "/" + nonSenseId);
        assertThat(nonSenseResponse.getStatusCode().is4xxClientError(), is(true));
    }

    @Test
    public void testGetLibrariesByBookEndpoint() {
        //TODO after endpoint is created
    }

    @Test
    public void testAddLibraryEndpoint() throws IOException {
        String libraryUrl = createURLWithPort("/library");

        ResponseEntity<String> response = testRequests.get(libraryUrl);

        ObjectMapper objectMapper = new ObjectMapper();

        List<Library> libraries = objectMapper.readValue(response.getBody(), new TypeReference<>() {
        });

        int expectedSize = 6;

        assertThat(libraries.size(), is(expectedSize));

        for (Library library : libraries) {
            expectedSize--;
            assertThat(library.getId(), is(idCounter - expectedSize));
        }
    }

    @Test
    public void testUpdateLibraryEndpoint() throws IOException {
        String libraryUpdateUrl = createURLWithPort("/library/put");

        ObjectMapper objectMapper = new ObjectMapper();

        for (Library library : libraryRepository.findAll()) {
            String newCity = "New City";
            ResponseEntity<String> response = testRequests.put(libraryUpdateUrl + "/" + library.getId(), new Library(library.getName(), newCity, library.getStreet(), library.getStreetNumber(), library.getDescription()));
            assert(response.getStatusCode().is2xxSuccessful());
            Library updatedLibrary = objectMapper.readValue(testRequests.get(createURLWithPort("/library/" + library.getId())).getBody(), new TypeReference<>() {
            });
            assertThat(updatedLibrary.getName(), is(library.getName()));
            assertThat(updatedLibrary.getCity(), is(newCity));
            assertThat(updatedLibrary.getStreet(), is(library.getStreet()));
            assertThat(updatedLibrary.getStreetNumber(), is(library.getStreetNumber()));
            assertThat(updatedLibrary.getDescription(), is(library.getDescription()));
        }

        for (Library library : libraryRepository.findAll()) {
            String newStreet = "New Street";
            testRequests.put(libraryUpdateUrl + "/" + library.getId(), new Library(library.getName(), library.getCity(), newStreet, library.getStreetNumber(), library.getDescription()));
            Library updatedLibrary = objectMapper.readValue(testRequests.get(createURLWithPort("/library/" + library.getId())).getBody(), new TypeReference<>() {
            });
            assertThat(updatedLibrary.getName(), is(library.getName()));
            assertThat(updatedLibrary.getCity(), is(library.getCity()));
            assertThat(updatedLibrary.getStreet(), is(newStreet));
            assertThat(updatedLibrary.getStreetNumber(), is(library.getStreetNumber()));
            assertThat(updatedLibrary.getDescription(), is(library.getDescription()));
        }

        for (Library library : libraryRepository.findAll()) {
            Integer newStreetNumber = 999;
            testRequests.put(libraryUpdateUrl + "/" + library.getId(), new Library(library.getName(), library.getCity(), library.getStreet(), newStreetNumber, library.getDescription()));
            Library updatedLibrary = objectMapper.readValue(testRequests.get(createURLWithPort("/library/" + library.getId())).getBody(), new TypeReference<>() {
            });
            assertThat(updatedLibrary.getName(), is(library.getName()));
            assertThat(updatedLibrary.getCity(), is(library.getCity()));
            assertThat(updatedLibrary.getStreet(), is(library.getStreet()));
            assertThat(updatedLibrary.getStreetNumber(), is(newStreetNumber));
            assertThat(updatedLibrary.getDescription(), is(library.getDescription()));
        }

        for (Library library : libraryRepository.findAll()) {
            String newDescription = "New description";
            testRequests.put(libraryUpdateUrl + "/" + library.getId(), new Library(library.getName(), library.getCity(), library.getStreet(), library.getStreetNumber(), newDescription));
            Library updatedLibrary = objectMapper.readValue(testRequests.get(createURLWithPort("/library/" + library.getId())).getBody(), new TypeReference<>() {
            });
            assertThat(updatedLibrary.getName(), is(library.getName()));
            assertThat(updatedLibrary.getCity(), is(library.getCity()));
            assertThat(updatedLibrary.getStreet(), is(library.getStreet()));
            assertThat(updatedLibrary.getStreetNumber(), is(library.getStreetNumber()));
            assertThat(updatedLibrary.getDescription(), is(newDescription));
        }

        for (Library library : libraryRepository.findAll()) {
            String newName = "Great Library";
            String newCity = "Horni Dolni";
            String newStreet = "Pricna";
            Integer newNumber = 666;
            String newDescription = "Perfect place";
            testRequests.put(libraryUpdateUrl + "/" + library.getId(), new Library(newName, newCity, newStreet, newNumber, newDescription));
            Library updatedLibrary = objectMapper.readValue(testRequests.get(createURLWithPort("/library/" + library.getId())).getBody(), new TypeReference<>() {
            });
            assertThat(updatedLibrary.getName(), is(newName));
            assertThat(updatedLibrary.getCity(), is(newCity));
            assertThat(updatedLibrary.getStreet(), is(newStreet));
            assertThat(updatedLibrary.getStreetNumber(), is(newNumber));
            assertThat(updatedLibrary.getDescription(), is(newDescription));
        }

        ResponseEntity<String> nonSenseRequest;

        int nonSenseId = new Random().nextInt(50000) + 100;
        nonSenseRequest = testRequests.put(libraryUpdateUrl + "/" + nonSenseId, new Library("New Library", "New City", "New Street", 123, "New description"));
        assert (nonSenseRequest.getStatusCode().is4xxClientError());
    }

    @Test
    public void testDeleteLibraryEndpoint() throws IOException {
        String libraryDeleteUrl = createURLWithPort("/library/delete");

        Iterable<Library> libraries = libraryRepository.findAll();

        Long librariesCounter = countIterable(libraries);

        for (int i = 0; i < 5; i++) {
            int nonSenseId = new Random().nextInt(50000) + 100;
            ResponseEntity<String> response = testRequests.delete(
                    libraryDeleteUrl + "/" + nonSenseId);
            assert (response.getStatusCode().is4xxClientError());
            assertThat(countIterable(libraryRepository.findAll()), is(librariesCounter));
        }

        for (Library library : libraries) {
            Integer libraryId = library.getId();
            String deleteLibraryUrl = libraryDeleteUrl + "/" + libraryId;
            testRequests.delete(deleteLibraryUrl);
            librariesCounter--;

            assertThat(librariesCounter, is(countIterable(libraryRepository.findAll())));
            ResponseEntity<String> getResponse = testRequests.get(createURLWithPort("/library/" + library.getId()));
            assert (getResponse.getStatusCode().is4xxClientError());
        }

        for (int i = 0; i <= idCounter; i++) {
            ResponseEntity<String> response = testRequests.delete(
                    libraryDeleteUrl + "/" + i);
            assert (response.getStatusCode().is4xxClientError());
            assertThat(countIterable(libraryRepository.findAll()), is(librariesCounter));
        }

        assertThat(countIterable(libraryRepository.findAll()), is(0L));
    }
}
