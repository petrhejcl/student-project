package com.redhat.restdemo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.restdemo.model.entity.Author;
import com.redhat.restdemo.model.entity.Authorship;
import com.redhat.restdemo.model.entity.Book;
import com.redhat.restdemo.model.repository.AuthorRepository;
import com.redhat.restdemo.model.repository.AuthorshipRepository;
import com.redhat.restdemo.model.repository.BookRepository;
import com.redhat.restdemo.utils.TestData;
import org.junit.jupiter.api.AfterEach;
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

class AuthorEndpointTests extends EndpointTestTemplate {
	private final String baseAuthorUrl = createURLWithPort("/author");

	@Autowired
	private AuthorRepository authorRepository;

	@Autowired
	private BookRepository bookRepository;

	@Autowired
	private AuthorshipRepository authorshipRepository;

	public void prepareAuthorSchema() {
		authorRepository.saveAll(TestData.authors);
		assertThat(countIterable(authorRepository.findAll()), is((long) TestData.authors.size()));
	}

	private List<Authorship> prepareAuthorshipSchema() {
		List<Authorship> authorships = new ArrayList<>();
		for (Map.Entry<Author, Book> entry : TestData.authorship.entrySet()) {
			Integer authorId = authorRepository.save(entry.getKey()).getId();
			Integer bookId = bookRepository.save(entry.getValue()).getId();
			Authorship authorship = new Authorship(bookId, authorId);
			authorships.add(authorshipRepository.save(authorship));
		}
		assertThat(countIterable(authorshipRepository.findAll()), is((long) TestData.authorship.size()));
		return authorships;
	}

	@AfterEach
	void clearRepos() {
		authorRepository.deleteAll();
		bookRepository.deleteAll();
		authorRepository.deleteAll();
	}

	@Test
	void testGetAllAuthorsEndpoint() throws IOException {
		prepareAuthorSchema();

		ResponseEntity<String> response = testRequests.get(baseAuthorUrl);

		assertThat(response.getStatusCode().is2xxSuccessful(), is(true));

		List<Author> authors = objectMapper.readValue(response.getBody(), new TypeReference<>() {
		});

		assertThat(authors.size(), is(TestData.authors.size()));

		for (Author author : TestData.authors) {
			assertThat(authors.contains(author), is(true));
		}
	}

	@Test
	void testGetAuthorById() throws IOException {
		prepareAuthorSchema();

		for (Author author : authorRepository.findAll()) {
			Integer id = author.getId();
			ResponseEntity<String> response = testRequests.get(baseAuthorUrl + id);
			assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
			Author testAuthor = objectMapper.readValue(response.getBody(), new TypeReference<>() {
			});
			assertThat(author, is(testAuthor));
		}

		int nonSenseId = new Random().nextInt(50000) + 100;
		ResponseEntity<String> nonSenseResponse = testRequests.get(baseAuthorUrl + "/" + nonSenseId);
		assertThat(nonSenseResponse.getStatusCode().is4xxClientError(), is(true));
	}

	@Test
	void testGetAuthorsByBookEndpoint() throws IOException {
		List<Authorship> authorships = prepareAuthorshipSchema();

		String bookUrl = baseAuthorUrl + "/book/";

		for (Authorship authorship : authorships) {
			Integer bookId = authorship.getBookId();
			Integer authorId = authorship.getAuthorId();
			ResponseEntity<String> response = testRequests.get(bookUrl + bookId);
			assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
			List<Author> authors = objectMapper.readValue(response.getBody(), new TypeReference<>() {
			});
			int authorsCount = authors.size();
			assertThat(authors.contains(authorRepository.findById(authorId).get()), is(true));

			Author testAuthor = authorRepository.save(new Author("Test", "Author", 1900));
			authorshipRepository.save(new Authorship(bookId, testAuthor.getId()));
			response = testRequests.get(bookUrl + bookId);
			assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
			authors = objectMapper.readValue(response.getBody(), new TypeReference<>() {
			});
			assertThat(authors.size(), is(authorsCount + 1));
			assertThat(authors.contains(testAuthor), is(true));
		}

		authorshipRepository.deleteAll();

		for (Book book : bookRepository.findAll()) {
			Integer bookId = book.getId();
			ResponseEntity<String> response = testRequests.get(bookUrl + bookId);
			assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
			List<Author> authors = objectMapper.readValue(response.getBody(), new TypeReference<>() {
			});
			assertThat(authors.size(), is(0));
		}
	}

	@Test
	void testAddAuthorEndpoint() throws IOException {
		String addAuthorUrl = createURLWithPort("/author/add");

		long authorCount = countIterable(authorRepository.findAll());

		for (Author author : TestData.authors) {
			ResponseEntity<String> response = testRequests.post(addAuthorUrl, author);
			assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
			Author newAuthor = objectMapper.readValue(response.getBody(), new TypeReference<>() {
			});
			assertThat(authorRepository.findById(newAuthor.getId()).get(), is(author));
			assertThat(countIterable(authorRepository.findAll()), is(authorCount + 1));
			authorCount++;
		}
	}

	@Test
	void testUpdateAuthorName() {
		prepareAuthorSchema();

		String authorPutUrl = createURLWithPort("/author/put/");

		for (Author author : authorRepository.findAll()) {
			String newName = "Josef";
			testRequests.put(authorPutUrl + author.getId(), new Author(newName, null, null));
			Author updatedAuthor = authorRepository.findById(author.getId()).get();
			Author referenceAuthor = new Author(newName, author.getSurname(), author.getYearOfBirth());
			assertThat(referenceAuthor, is(updatedAuthor));
		}
	}

	@Test
	void testUpdateAuthorSurname() {
		prepareAuthorSchema();

		String authorPutUrl = createURLWithPort("/author/put/");

		for (Author author : authorRepository.findAll()) {
			String newSurname = "Novak";
			testRequests.put(authorPutUrl + author.getId(), new Author(null, newSurname, null));
			Author updatedAuthor = authorRepository.findById(author.getId()).get();
			Author referenceAuthor = new Author(author.getName(), newSurname, author.getYearOfBirth());
			assertThat(referenceAuthor, is(updatedAuthor));
		}
	}


	@Test
	void testUpdateAuthorYearOfBirth() {
		prepareAuthorSchema();

		String authorPutUrl = createURLWithPort("/author/put/");

		for (Author author : authorRepository.findAll()) {
			Integer newYearOfBirth = 1900;
			testRequests.put(authorPutUrl + author.getId(), new Author(null, null, newYearOfBirth));
			Author updatedAuthor = authorRepository.findById(author.getId()).get();
			Author referenceAuthor = new Author(author.getName(), author.getSurname(), newYearOfBirth);
			assertThat(referenceAuthor, is(updatedAuthor));
		}
	}

	@Test
	void testUpdateAuthorEndpoint() throws IOException {
		prepareAuthorSchema();

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
	}

	@Test
	void testDeleteAuthorEndpoint() {
		prepareAuthorSchema();

		String authorDeleteUrl = createURLWithPort("/author/delete");

		Iterable<Author> authors = authorRepository.findAll();

		Long authorsCounter = countIterable(authors);

		for (int i = 0; i < 5; i++) {
			int nonSenseId = new Random().nextInt(50000) + 100;
			ResponseEntity<String> response = testRequests.delete(
					authorDeleteUrl + "/" + nonSenseId);
			assert(response.getStatusCode().is4xxClientError());
			assertThat(countIterable(authorRepository.findAll()), is(authorsCounter));
		}

		for (Author author : authors) {
			Integer authorId = author.getId();
			String deleteAuthorUrl = authorDeleteUrl + "/" + authorId;
			testRequests.delete(deleteAuthorUrl);
			authorsCounter--;

			assertThat(authorsCounter, is(countIterable(authorRepository.findAll())));
			ResponseEntity<String> getResponse = testRequests.get(createURLWithPort("/author/" + author.getId()));
			assert(getResponse.getStatusCode().is4xxClientError());
		}

		for (int i = 0; i <= idCounter; i++) {
			ResponseEntity<String> response = testRequests.delete(
					authorDeleteUrl + "/" + i);
			assert(response.getStatusCode().is4xxClientError());
			assertThat(countIterable(authorRepository.findAll()), is(authorsCounter));
		}

		assertThat(countIterable(authorRepository.findAll()), is(0L));
	}
}

