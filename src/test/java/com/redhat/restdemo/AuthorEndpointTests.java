package com.redhat.restdemo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.restdemo.controllers.AuthorController;
import com.redhat.restdemo.model.entity.Author;
import com.redhat.restdemo.model.repository.AuthorRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.transaction.Transactional;
import java.io.IOException;
import java.sql.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import static com.redhat.restdemo.utils.utils.countGetResult;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


@RunWith(SpringRunner.class)
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthorEndpointTests {

	@Value("http://localhost:${local.server.port}")
	String baseUrl;

	TestRequests testRequests = new TestRequests();

	@Autowired
	private AuthorRepository authorRepository;

	private static PostgreSQLContainer postgresqlContainer;

	static {
		postgresqlContainer = new PostgreSQLContainer("postgres:14")
				.withDatabaseName("postgres")
				.withUsername("compose-postgres")
				.withPassword("compose-postgres");
		postgresqlContainer.start();
	}

	@DynamicPropertySource
	public static void setDatasourceProperties(final DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
		registry.add("spring.datasource.username", postgresqlContainer::getUsername);
		registry.add("spring.datasource.password", postgresqlContainer::getPassword);
	}

	private String createURLWithPort(String uri) {
		return baseUrl + uri;
	}

	@BeforeEach
	public void prepareSchema() throws IOException {
		try (Connection connection = DriverManager.getConnection(
				postgresqlContainer.getJdbcUrl(),
				postgresqlContainer.getUsername(),
				postgresqlContainer.getPassword()
		)) {
			try (Statement statement = connection.createStatement()) {
				statement.executeUpdate("TRUNCATE TABLE author");
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		String authorAddUrl = createURLWithPort("/author/add");

		LinkedList<Author> authors = new LinkedList<>();

		authors.add(new Author("J.K.", "Rowling", 1965));
		authors.add(new Author("George", "Orwell", 1903));
		authors.add(new Author("Jane", "Austen", 1775));
		authors.add(new Author("Ernest", "Hemingway", 1899));
		authors.add(new Author("Maya", "Angelou", 1928));
		authors.add(new Author("Charles", "Bukowski", 1920));

		for (Author author : authors) {
			ResponseEntity<String> response = testRequests.post(authorAddUrl, author);
			if (!response.getStatusCode().is2xxSuccessful()) {
				throw new IOException("Preparing schema was not successful");
			}
		}
	}

	@Test
	void testGeneralEndpoint() throws IOException {
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

		assertThat(authors.get(0).getId(), is(1));
		assertThat(authors.get(1).getId(), is(2));
		assertThat(authors.get(2).getId(), is(3));
		assertThat(authors.get(3).getId(), is(4));
		assertThat(authors.get(4).getId(), is(5));
		assertThat(authors.get(5).getId(), is(6));
	}

	@Test
	void testPostAuthorEndpoint() throws IOException {
		String authorUrl = createURLWithPort("/author");

		ResponseEntity<String> response = testRequests.get(authorUrl);

		ObjectMapper objectMapper = new ObjectMapper();
		List<Author> authors = objectMapper.readValue(response.getBody(), new TypeReference<List<Author>>() {
		});
		assertThat(authors.size(), is(6));

		assertThat(authors.get(0).getId(), is(0));
		assertThat(authors.get(1).getId(), is(1));
		assertThat(authors.get(2).getId(), is(2));
		assertThat(authors.get(3).getId(), is(3));
		assertThat(authors.get(4).getId(), is(4));
		assertThat(authors.get(5).getId(), is(5));
	}

	@Test
	void testGetAuthorById() throws IOException {
		String authorUrl = createURLWithPort("/author");

		ObjectMapper objectMapper = new ObjectMapper();

		Author rowling = objectMapper.readValue(testRequests.get(authorUrl + "/" + 1).getBody(), new TypeReference<>() {
		});
		Author orwell = objectMapper.readValue(testRequests.get(authorUrl + "/" + 2).getBody(), new TypeReference<>() {
		});
		Author austen = objectMapper.readValue(testRequests.get(authorUrl + "/" + 3).getBody(), new TypeReference<>() {
		});
		Author hemingway = objectMapper.readValue(testRequests.get(authorUrl + "/" + 4).getBody(), new TypeReference<>() {
		});
		Author angelou = objectMapper.readValue(testRequests.get(authorUrl + "/" + 5).getBody(), new TypeReference<>() {
		});
		Author bukowski = objectMapper.readValue(testRequests.get(authorUrl + "/" + 6).getBody(), new TypeReference<>() {
		});

		assertThat(rowling.getId(), is(1));
		assertThat(rowling.getName(), is("J.K."));
		assertThat(rowling.getSurname(), is("Rowling"));
		assertThat(rowling.getYearOfBirth(), is(1965));

		assertThat(orwell.getId(), is(2));
		assertThat(orwell.getName(), is("George"));
		assertThat(orwell.getSurname(), is("Orwell"));
		assertThat(orwell.getYearOfBirth(), is(1903));

		assertThat(austen.getId(), is(3));
		assertThat(austen.getName(), is("Jane"));
		assertThat(austen.getSurname(), is("Austen"));
		assertThat(austen.getYearOfBirth(), is(1775));

		assertThat(hemingway.getId(), is(4));
		assertThat(hemingway.getName(), is("Ernest"));
		assertThat(hemingway.getSurname(), is("Hemingway"));
		assertThat(hemingway.getYearOfBirth(), is(1899));

		assertThat(angelou.getId(), is(5));
		assertThat(angelou.getName(), is("Maya"));
		assertThat(angelou.getSurname(), is("Angelou"));
		assertThat(angelou.getYearOfBirth(), is(1928));

		assertThat(bukowski.getId(), is(6));
		assertThat(bukowski.getName(), is("Charles"));
		assertThat(bukowski.getSurname(), is("Bukowski"));
		assertThat(bukowski.getYearOfBirth(), is(1920));
	}

	@Test
	void testDeleteAuthorEndpoint() throws IOException {
		String authorDeleteUrl = createURLWithPort("/author/delete");

		Iterable<Author> authors = authorRepository.findAll();

		Long authorsCounter = countGetResult(authors);

		testRequests.delete(authorDeleteUrl + "/" + 1);
		authorsCounter--;
		assertThat(authorsCounter, is(countGetResult(authorRepository.findAll())));

		testRequests.delete(authorDeleteUrl + "/" + 3);
		authorsCounter--;
		assertThat(authorsCounter, is(countGetResult(authorRepository.findAll())));

		for (Author author : authorRepository.findAll()) {
			Integer authorId = author.getId();
			String deleteAuthorUrl = authorDeleteUrl + "/" + authorId;
			testRequests.delete(deleteAuthorUrl);
			authorsCounter--;
			assertThat(authorsCounter, is(countGetResult(authorRepository.findAll())));
		}

		assertThat(countGetResult(authorRepository.findAll()), is(0L));
	}
}

