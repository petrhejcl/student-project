package com.redhat.restdemo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.restdemo.model.entity.*;
import com.redhat.restdemo.model.repository.BookRepository;
import com.redhat.restdemo.model.repository.LibraryRepository;
import com.redhat.restdemo.model.repository.OwnershipRepository;
import com.redhat.restdemo.utils.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.stream.StreamSupport;

import static com.redhat.restdemo.utils.TestUtils.countIterable;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class LibraryEndpointTests extends EndpointTestTemplate {
    @Autowired
    LibraryRepository libraryRepository;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    OwnershipRepository ownershipRepository;

    private String baseLibraryUrl;
    private String getLibrariesByBookUrl;
    private String addLibraryUrl;
    private String putLibraryUrl;
    private String deleteLibraryUrl;

    @PostConstruct
    public void initializeUrls() {
        baseLibraryUrl = createURLWithPort("/library");
        getLibrariesByBookUrl = baseLibraryUrl + "/book/";
        addLibraryUrl = baseLibraryUrl + "/add";
        putLibraryUrl = baseLibraryUrl + "/put/";
        deleteLibraryUrl = baseLibraryUrl + "/delete/";
    }

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
    public void shouldListAllLibraries() throws JsonProcessingException {
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
    public void shouldListLibraryById() throws JsonProcessingException {
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
    public void shouldListLibrariesByBook() throws JsonProcessingException {
        List<Ownership> ownerships = prepareOwnershipSchema();

        for (Ownership ownership : ownerships) {
            Integer bookId = ownership.getBookId();
            Integer libraryId = ownership.getLibraryId();
            ResponseEntity<String> response = testRequests.get(getLibrariesByBookUrl + bookId);
            assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
            List<Library> libraries = objectMapper.readValue(response.getBody(), new TypeReference<>() {
            });
            int librariesCount = libraries.size();
            assertThat(libraries.stream()
                    .anyMatch(library -> Objects.equals(library, libraryRepository.findById(libraryId).get())), is(true));

            Library testLibrary = libraryRepository.save(new Library("Test Library", "Brno", "Sumavska", 123,"Great Library"));
            ownershipRepository.save(new Ownership(testLibrary.getId(), bookId));
            response = testRequests.get(getLibrariesByBookUrl + bookId);
            assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
            libraries = objectMapper.readValue(response.getBody(), new TypeReference<>() {
            });
            assertThat(libraries.size(), is(librariesCount + 1));
            assertThat(libraries.stream()
                    .anyMatch(library -> Objects.equals(library, testLibrary)), is(true));
        }

        ownershipRepository.deleteAll();

        for (Book book : bookRepository.findAll()) {
            Integer bookId = book.getId();
            ResponseEntity<String> response = testRequests.get(getLibrariesByBookUrl + bookId);
            assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
            List<Book> books = objectMapper.readValue(response.getBody(), new TypeReference<>() {
            });
            assertThat(books.size(), is(0));
        }
    }

    @Test
    void shouldAddLibrary() throws IOException {
        long libraryCount = countIterable(libraryRepository.findAll());

        for (Library library : TestData.libraries) {
            ResponseEntity<String> response = testRequests.post(addLibraryUrl, library);
            assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
            Library newLibrary = objectMapper.readValue(response.getBody(), new TypeReference<>() {
            });
            assertThat(libraryRepository.findById(newLibrary.getId()).get(), is(library));
            assertThat(countIterable(bookRepository.findAll()), is(libraryCount + 1));
            libraryCount++;
        }
    }

    @Test
    void shouldUpdateLibraryName() {
        prepareLibraryScheme();

        for (Library library : libraryRepository.findAll()) {
            String newName = "Test Library";
            ResponseEntity<String> response = testRequests.put( putLibraryUrl + library.getId(), new Library(newName, null, null, null, null));
            assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
            Library updatedLibrary = libraryRepository.findById(library.getId()).get();
            Library referenceLibrary = new Library(newName, library.getCity(), library.getStreet(), library.getStreetNumber(), library.getDescription());
            assertThat(updatedLibrary, is(referenceLibrary));
        }
    }

    //TODO: Update tests to match code style from here down

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
