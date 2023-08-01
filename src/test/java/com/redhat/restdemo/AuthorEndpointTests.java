package com.redhat.restdemo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.redhat.restdemo.model.entity.Author;
import com.redhat.restdemo.model.entity.Authorship;
import com.redhat.restdemo.model.entity.Book;
import com.redhat.restdemo.model.repository.AuthorRepository;
import com.redhat.restdemo.model.repository.AuthorshipRepository;
import com.redhat.restdemo.model.repository.BookRepository;
import com.redhat.restdemo.utils.TestData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

import static com.redhat.restdemo.utils.TestUtils.countIterable;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class AuthorEndpointTests extends EndpointTestTemplate {
	@Container
	private static PostgreSQLContainer postgresqlContainer;

	static {
		postgresqlContainer = new PostgreSQLContainer("postgres:14")
				.withDatabaseName("postgres")
				.withUsername("compose-postgres")
				.withPassword("compose-postgres");
		postgresqlContainer.start();
	}

	@DynamicPropertySource
	protected static void setDatasourceProperties(final DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
		registry.add("spring.datasource.username", postgresqlContainer::getUsername);
		registry.add("spring.datasource.password", postgresqlContainer::getPassword);
	}

	@Autowired
	private AuthorRepository authorRepository;

	@Autowired
	private BookRepository bookRepository;

	@Autowired
	private AuthorshipRepository authorshipRepository;


	private String baseAuthorUrl;
	private String getAuthorByBookUrl;
	private String addAuthorUrl;
	private String putAuthorUrl;
	private String deleteAuthorUrl;

	@PostConstruct
	public void initializeUrls() {
		baseAuthorUrl = createURLWithPort("/author");
		getAuthorByBookUrl = baseAuthorUrl + "/book/";
		addAuthorUrl = baseAuthorUrl + "/add";
		putAuthorUrl = baseAuthorUrl + "/put/";
		deleteAuthorUrl = baseAuthorUrl + "/delete/";
	}

	private void prepareAuthorSchema() {
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
	void shouldListAllAuthors() throws IOException {
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
	void shouldListAuthorById() throws IOException {
		prepareAuthorSchema();

		for (Author author : authorRepository.findAll()) {
			Integer id = author.getId();
			ResponseEntity<String> response = testRequests.get(baseAuthorUrl + "/" + id);
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
	void shouldListAuthorsByBook() throws IOException {
		List<Authorship> authorships = prepareAuthorshipSchema();

		for (Authorship authorship : authorships) {
			Integer bookId = authorship.getBookId();
			Integer authorId = authorship.getAuthorId();
			ResponseEntity<String> response = testRequests.get(getAuthorByBookUrl + bookId);
			assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
			List<Author> authors = objectMapper.readValue(response.getBody(), new TypeReference<>() {
			});
			int authorsCount = authors.size();
			assertThat(authors.stream()
					.anyMatch(author -> Objects.equals(author, authorRepository.findById(authorId).get())), is(true));

			Author testAuthor = authorRepository.save(new Author("Test", "Author", 1900));
			authorshipRepository.save(new Authorship(bookId, testAuthor.getId()));
			response = testRequests.get(getAuthorByBookUrl + bookId);
			assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
			authors = objectMapper.readValue(response.getBody(), new TypeReference<>() {
			});
			assertThat(authors.size(), is(authorsCount + 1));
			assertThat(authors.stream()
					.anyMatch(author -> Objects.equals(author, testAuthor)), is(true));
		}

		authorshipRepository.deleteAll();

		for (Book book : bookRepository.findAll()) {
			Integer bookId = book.getId();
			ResponseEntity<String> response = testRequests.get(getAuthorByBookUrl + bookId);
			assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
			List<Author> authors = objectMapper.readValue(response.getBody(), new TypeReference<>() {
			});
			assertThat(authors.size(), is(0));
		}
	}

	@Test
	void shouldAddAuthor() throws IOException {
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
	void shouldUpdateAuthorName() {
		prepareAuthorSchema();

		for (Author author : authorRepository.findAll()) {
			String newName = "Josef";
			ResponseEntity<String> response = testRequests.put(putAuthorUrl + author.getId(), new Author(newName, null, null));
			assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
			Author updatedAuthor = authorRepository.findById(author.getId()).get();
			Author referenceAuthor = new Author(newName, author.getSurname(), author.getYearOfBirth());
			assertThat(referenceAuthor, is(updatedAuthor));
		}
	}

	@Test
	void shouldUpdateAuthorSurname() {
		prepareAuthorSchema();

		for (Author author : authorRepository.findAll()) {
			String newSurname = "Novak";
			ResponseEntity<String> response = testRequests.put(putAuthorUrl + author.getId(), new Author(null, newSurname, null));
			assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
			Author updatedAuthor = authorRepository.findById(author.getId()).get();
			Author referenceAuthor = new Author(author.getName(), newSurname, author.getYearOfBirth());
			assertThat(referenceAuthor, is(updatedAuthor));
		}
	}

	@Test
	void shouldUpdateAuthorYearOfBirth() {
		prepareAuthorSchema();

		for (Author author : authorRepository.findAll()) {
			Integer newYearOfBirth = 1900;
			ResponseEntity<String> response = testRequests.put(putAuthorUrl + author.getId(), new Author(null, null, newYearOfBirth));
			assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
			Author updatedAuthor = authorRepository.findById(author.getId()).get();
			Author referenceAuthor = new Author(author.getName(), author.getSurname(), newYearOfBirth);
			assertThat(referenceAuthor, is(updatedAuthor));
		}
	}

	@Test
	void shouldUpdateWholeAuthor() {
		prepareAuthorSchema();

		for (Author author : authorRepository.findAll()) {
			String newName = "Ferda";
			String newSurname = "Mravenec";
			Integer newYearOfBirth = 1935;
			ResponseEntity<String> response = testRequests.put(putAuthorUrl + author.getId(), new Author(newName, newSurname, newYearOfBirth));
			assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
			Author updatedAuthor = authorRepository.findById(author.getId()).get();
			Author referenceAuthor = new Author(newName, newSurname, newYearOfBirth);
			assertThat(referenceAuthor, is(updatedAuthor));
		}
	}

	@Test
	void shouldNotUpdateAnythingWhenTryingToUpdateInvalidId() {
		prepareAuthorSchema();

		Iterable<Author> beforeRequestAuthors = authorRepository.findAll();

		int nonSenseId = new Random().nextInt(50000) + 100;
		ResponseEntity<String> nonSenseRequest = testRequests.put(putAuthorUrl + "/" + nonSenseId, new Author("Karel", "Hynek Macha", 1750));
		assertThat(nonSenseRequest.getStatusCode().is4xxClientError(), is(true));

		Iterable<Author> afterRequestAuthors = authorRepository.findAll();

		assertThat(beforeRequestAuthors, is(afterRequestAuthors));
	}

	@Test
	void shouldNotDeleteAnythingWhenTryingToDeleteInvalidId() {
		prepareAuthorSchema();

		Iterable<Author> authors = authorRepository.findAll();

		for (int i = 0; i < 5; i++) {
			int nonSenseId = new Random().nextInt(50000) + 100;
			ResponseEntity<String> response = testRequests.delete(
					deleteAuthorUrl + nonSenseId);
			assertThat(response.getStatusCode().is4xxClientError(), is(true));
			assertThat(authors, is(authorRepository.findAll()));
		}
	}

	@Test
	void shouldDeleteAuthor() {
		prepareAuthorSchema();

		Iterable<Author> authors = authorRepository.findAll();

		long authorsCounter = countIterable(authors);

		for (Author author : authors) {
			Integer authorId = author.getId();
			ResponseEntity<String> response = testRequests.delete(deleteAuthorUrl + authorId);
			assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
			authorsCounter--;

			assertThat(authorsCounter, is(countIterable(authorRepository.findAll())));
			assertThat(authorRepository.existsById(authorId), is(false));
		}
	}

	@Test
	void shouldDeleteAllConnectedAuthorshipsWhenDeletingAuthor() {
		List<Authorship> authorships = prepareAuthorshipSchema();

		for (Authorship authorship : authorships) {
			assertThat(authorshipRepository.existsById(authorship.getId()), is(true));
			Integer authorId = authorship.getAuthorId();
			testRequests.delete(deleteAuthorUrl + authorId);
			assertThat(authorshipRepository.existsById(authorship.getId()), is(false));
		}
	}
}

