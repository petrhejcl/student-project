package com.redhat.restdemo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.restdemo.model.entity.Author;
import com.redhat.restdemo.model.repository.AuthorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static com.redhat.restdemo.utils.Utils.countGetResult;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class AuthorEndpointTests extends EndpointTestTemplate {

	@Autowired
	private AuthorRepository authorRepository;

	@BeforeEach
	public void prepareAuthorSchema() throws IOException {
		authorRepository.deleteAll();

		String authorAddUrl = createURLWithPort("/author/add");

		LinkedList<Author> authors = new LinkedList<>();

		authors.add(new Author("J.K.", "Rowling", 1965));
		authors.add(new Author("George", "Orwell", 1903));
		authors.add(new Author("Jane", "Austen", 1775));
		authors.add(new Author("Ernest", "Hemingway", 1899));
		authors.add(new Author("Maya", "Angelou", 1928));
		authors.add(new Author("Charles", "Bukowski", 1920));

		for (Author author : authors) {
			ResponseEntity<String> response = postAndIncrease(authorAddUrl, author);
			if (!response.getStatusCode().is2xxSuccessful()) {
				throw new IOException("Preparing schema was not successful");
			}
		}
	}

	public void prepareBookSchema() throws IOException {
		//TODO after authorship tests done
	}

	@Test
	void testGetAllAuthorsEndpoint() throws IOException {
		String authorUrl = createURLWithPort("/author");

		ResponseEntity<String> response = testRequests.get(authorUrl);

		ObjectMapper objectMapper = new ObjectMapper();

		List<Author> authors = objectMapper.readValue(response.getBody(), new TypeReference<List<Author>>() {
		});
		assertThat(authors.size(), is(6));

		assertThat(authors.get(0).getName(), is("J.K."));
		assertThat(authors.get(0).getSurname(), is("Rowling"));
		assertThat(authors.get(0).getYearOfBirth(), is(1965));

		assertThat(authors.get(1).getName(), is("George"));
		assertThat(authors.get(1).getSurname(), is("Orwell"));
		assertThat(authors.get(1).getYearOfBirth(), is(1903));

		assertThat(authors.get(2).getName(), is("Jane"));
		assertThat(authors.get(2).getSurname(), is("Austen"));
		assertThat(authors.get(2).getYearOfBirth(), is(1775));

		assertThat(authors.get(3).getName(), is("Ernest"));
		assertThat(authors.get(3).getSurname(), is("Hemingway"));
		assertThat(authors.get(3).getYearOfBirth(), is(1899));

		assertThat(authors.get(4).getName(), is("Maya"));
		assertThat(authors.get(4).getSurname(), is("Angelou"));
		assertThat(authors.get(4).getYearOfBirth(), is(1928));

		assertThat(authors.get(5).getName(), is("Charles"));
		assertThat(authors.get(5).getSurname(), is("Bukowski"));
		assertThat(authors.get(5).getYearOfBirth(), is(1920));
	}

	@Test
	void testGetAuthorById() throws IOException {
		String authorUrl = createURLWithPort("/author");

		ObjectMapper objectMapper = new ObjectMapper();

		ResponseEntity<String> response = testRequests.get(authorUrl);
		List<Author> authors = objectMapper.readValue(response.getBody(), new TypeReference<List<Author>>() {
		});

		for (Author author : authors) {
			Integer id = author.getId();
			Author testAuthor = objectMapper.readValue(testRequests.get(authorUrl + "/" + id).getBody(), new TypeReference<>() {
			});
			assertThat(author.getId(), is(testAuthor.getId()));
			assertThat(author.getName(), is(testAuthor.getName()));
			assertThat(author.getSurname(), is(testAuthor.getSurname()));
			assertThat(author.getYearOfBirth(), is(testAuthor.getYearOfBirth()));
		}
	}

	@Test
	void testGetAuthorsByBookEndpoint() throws IOException {
		//TODO after authorship tests done
	}

	@Test
	void testAddAuthorEndpoint() throws IOException {
		String authorUrl = createURLWithPort("/author");

		ResponseEntity<String> response = testRequests.get(authorUrl);

		ObjectMapper objectMapper = new ObjectMapper();

		List<Author> authors = objectMapper.readValue(response.getBody(), new TypeReference<List<Author>>() {
		});

		int expectedSize = 6;

		assertThat(authors.size(), is(expectedSize));

		for (Author author : authors) {
			expectedSize--;
			assertThat(author.getId(), is(idCounter - expectedSize));
		}
	}

	@Test
	void testUpdateAuthorEndpoint() throws IOException {
		String authorDeleteUrl = createURLWithPort("/author/put");

		ObjectMapper objectMapper = new ObjectMapper();

		for (Author author : authorRepository.findAll()) {
			String newName = "Josef";
			testRequests.put(authorDeleteUrl + "/" + author.getId(), new Author(newName, null, null));
			Author updatedAuthor = objectMapper.readValue(testRequests.get(createURLWithPort("/author/" + author.getId())).getBody(), new TypeReference<>() {
			});
			assertThat(updatedAuthor.getName(), is(newName));
			assertThat(updatedAuthor.getSurname(), is(author.getSurname()));
			assertThat(updatedAuthor.getYearOfBirth(), is(author.getYearOfBirth()));
		}

		for (Author author : authorRepository.findAll()) {
			String name = author.getName();
			String newSurname = "Novak";
			testRequests.put(authorDeleteUrl + "/" + author.getId(), new Author(null, newSurname, null));
			Author updatedAuthor = objectMapper.readValue(testRequests.get(createURLWithPort("/author/" + author.getId())).getBody(), new TypeReference<>() {
			});
			assertThat(updatedAuthor.getName(), is(author.getName()));
			assertThat(updatedAuthor.getSurname(), is(newSurname));
			assertThat(updatedAuthor.getYearOfBirth(), is(author.getYearOfBirth()));
		}

		for (Author author : authorRepository.findAll()) {
			int newYearOfBirth = 1850;
			testRequests.put(authorDeleteUrl + "/" + author.getId(), new Author(null, null, newYearOfBirth));
			Author updatedAuthor = objectMapper.readValue(testRequests.get(createURLWithPort("/author/" + author.getId())).getBody(), new TypeReference<>() {
			});
			assertThat(updatedAuthor.getName(), is(author.getName()));
			assertThat(updatedAuthor.getSurname(), is(author.getSurname()));
			assertThat(updatedAuthor.getYearOfBirth(), is(newYearOfBirth));
		}

		for (Author author : authorRepository.findAll()) {
			String newName = "Ferda";
			String newSurname = "Mravenec";
			int newYearOfBirth = 1935;
			testRequests.put(authorDeleteUrl + "/" + author.getId(), new Author(newName, newSurname, newYearOfBirth));
			Author updatedAuthor = objectMapper.readValue(testRequests.get(createURLWithPort("/author/" + author.getId())).getBody(), new TypeReference<>() {
			});
			assertThat(updatedAuthor.getName(), is(newName));
			assertThat(updatedAuthor.getSurname(), is(newSurname));
			assertThat(updatedAuthor.getYearOfBirth(), is(newYearOfBirth));
		}

		ResponseEntity<String> nonSenseRequest;

		int nonSenseId = new Random().nextInt(50000) + 100;
		nonSenseRequest = testRequests.put(authorDeleteUrl + "/" + nonSenseId, new Author("Karel", "Hynek Macha", 1750));
		assert(nonSenseRequest.getStatusCode().is4xxClientError());

		Author tryToChangeId = new Author(99, "Karel", "Hynek Macha", 1750);
		nonSenseRequest = testRequests.put(authorDeleteUrl + "/" + idCounter, tryToChangeId);
		assert(nonSenseRequest.getStatusCode().is4xxClientError());
	}

	@Test
	void testDeleteAuthorEndpoint() throws IOException {
		String authorDeleteUrl = createURLWithPort("/author/delete");

		Iterable<Author> authors = authorRepository.findAll();

		Long authorsCounter = countGetResult(authors);

		for (int i = 0; i < 5; i++) {
			int nonSenseId = new Random().nextInt(50000) + 100;
			ResponseEntity<String> response = testRequests.delete(
					authorDeleteUrl + "/" + nonSenseId);
			assert(response.getStatusCode().is4xxClientError());
			assertThat(countGetResult(authorRepository.findAll()), is(authorsCounter));
		}

		for (Author author : authors) {
			Integer authorId = author.getId();
			String deleteAuthorUrl = authorDeleteUrl + "/" + authorId;
			testRequests.delete(deleteAuthorUrl);
			authorsCounter--;

			assertThat(authorsCounter, is(countGetResult(authorRepository.findAll())));
			ResponseEntity<String> getResponse = testRequests.get(createURLWithPort("/author/" + author.getId()));
			assert(getResponse.getStatusCode().is4xxClientError());
		}

		for (int i = 0; i <= idCounter; i++) {
			ResponseEntity<String> response = testRequests.delete(
					authorDeleteUrl + "/" + i);
			assert(response.getStatusCode().is4xxClientError());
			assertThat(countGetResult(authorRepository.findAll()), is(authorsCounter));
		}

		assertThat(countGetResult(authorRepository.findAll()), is(0L));
	}
}

