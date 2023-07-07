package com.redhat.restdemo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.restdemo.model.entity.Book;
import com.redhat.restdemo.model.entity.Library;
import com.redhat.restdemo.model.repository.LibraryRepository;
import com.redhat.restdemo.utils.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import static com.redhat.restdemo.utils.Utils.countIterable;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class LibraryEndpointTests extends EndpointTestTemplate {
    @Autowired
    LibraryRepository libraryRepository;

    @BeforeEach
    public void prepareLibraryScheme() throws IOException {
        prepareSchema(libraryRepository, createURLWithPort("/library/add"), TestData.libraries);
    }

    @Test
    void testGetAllLibrariesEndpoint() throws JsonProcessingException {
        String libraryUrl = createURLWithPort("/library");

        ResponseEntity<String> response = testRequests.get(libraryUrl);

        ObjectMapper objectMapper = new ObjectMapper();
        List<Library> libraries = objectMapper.readValue(response.getBody(), new TypeReference<List<Library>>() {
        });
        assertThat(libraries.size(), is(6));

        assertThat(libraries.get(0).getName(), is("Central Library"));
        assertThat(libraries.get(0).getCity(), is("New York"));
        assertThat(libraries.get(0).getStreet(), is("Main Street"));
        assertThat(libraries.get(0).getStreetNumber(), is(123));
        assertThat(libraries.get(0).getDescription(), is("The largest library in the city"));

        assertThat(libraries.get(1).getName(), is("Community Library"));
        assertThat(libraries.get(1).getCity(), is("Chicago"));
        assertThat(libraries.get(1).getStreet(), is("Elm Street"));
        assertThat(libraries.get(1).getStreetNumber(), is(456));
        assertThat(libraries.get(1).getDescription(), is("A community-focused library with diverse collections"));

        assertThat(libraries.get(2).getName(), is("Tech Library"));
        assertThat(libraries.get(2).getCity(), is("San Francisco"));
        assertThat(libraries.get(2).getStreet(), is("Oak Street"));
        assertThat(libraries.get(2).getStreetNumber(), is(789));
        assertThat(libraries.get(2).getDescription(), is("Specializes in technology and computer science resources"));

        assertThat(libraries.get(3).getName(), is("Historical Library"));
        assertThat(libraries.get(3).getCity(), is("London"));
        assertThat(libraries.get(3).getStreet(), is("Abbey Road"));
        assertThat(libraries.get(3).getStreetNumber(), is(10));
        assertThat(libraries.get(3).getDescription(), is("Preserves historical manuscripts and rare books"));

        assertThat(libraries.get(4).getName(), is("Children's Library"));
        assertThat(libraries.get(4).getCity(), is("Sydney"));
        assertThat(libraries.get(4).getStreet(), is("Park Street"));
        assertThat(libraries.get(4).getStreetNumber(), is(321));
        assertThat(libraries.get(4).getDescription(), is("Offers a wide range of books and activities for children"));

        assertThat(libraries.get(5).getName(), is("University Library"));
        assertThat(libraries.get(5).getCity(), is("Tokyo"));
        assertThat(libraries.get(5).getStreet(), is("University Avenue"));
        assertThat(libraries.get(5).getStreetNumber(), is(987));
        assertThat(libraries.get(5).getDescription(), is("Supports academic research and provides resources for students"));
    }

    @Test
    public void testGetLibraryById() throws IOException {
        String libraryUrl = createURLWithPort("/library");

        ObjectMapper objectMapper = new ObjectMapper();

        ResponseEntity<String> response = testRequests.get(libraryUrl);
        List<Library> libraries = objectMapper.readValue(response.getBody(), new TypeReference<List<Library>>() {
        });

        for (Library library : libraries) {
            Integer id = library.getId();
            Library testLibrary = objectMapper.readValue(testRequests.get(libraryUrl + "/" + id).getBody(), new TypeReference<>() {
            });
            assertThat(library, is(testLibrary));
        }

        int nonSenseId = new Random().nextInt(50000) + 100;
        ResponseEntity<String> nonSenseResponse = testRequests.get(libraryUrl + "/" + nonSenseId);
        assert (nonSenseResponse.getStatusCode().is4xxClientError());
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

        Library tryToChangeId = new Library(99, "New Library", "New City", "New Street", 123, "New description");
        nonSenseRequest = testRequests.put(libraryUpdateUrl + "/" + idCounter, tryToChangeId);
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
