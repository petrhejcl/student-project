package com.redhat.restdemo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.restdemo.model.entity.Author;
import com.redhat.restdemo.model.repository.AuthorRepository;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Collections;
import java.util.List;

import static com.redhat.restdemo.utils.utils.countGetResult;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


@RunWith(SpringRunner.class)
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthorEndpointTests {

	@Value("http://localhost:${local.server.port}")
	String baseUrl;

	TestRestTemplate restTemplate = new TestRestTemplate();

	HttpHeaders headers = new HttpHeaders();

	TestRequests testRequests = new TestRequests();

	@Autowired
	private AuthorRepository authorRepository;

	private static final PostgreSQLContainer postgresqlContainer;

	static {
		postgresqlContainer = new PostgreSQLContainer("postgres:14")
				.withDatabaseName("postgres")
				.withUsername("compose-postgres")
				.withPassword("compose-postgres");
		postgresqlContainer.start();
	}

	private String createURLWithPort(String uri) {
		return baseUrl + uri;
	}

	@DynamicPropertySource
	public static void setDatasourceProperties(final DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
		registry.add("spring.datasource.username", postgresqlContainer::getUsername);
		registry.add("spring.datasource.password", postgresqlContainer::getPassword);
	}

	@Test
	@Sql({"/prepare_schema.sql"})
	void testGeneralEndpoint() throws JsonProcessingException {
		String authorUrl = createURLWithPort("/author");

		ResponseEntity<String> response = testRequests.get(authorUrl);

		ObjectMapper objectMapper = new ObjectMapper();
		List<Author> authors = objectMapper.readValue(response.getBody(), new TypeReference<List<Author>>() {
		});
		assertThat(authors.size(), is(3));
		assertThat(authors.get(0).getName(), is("Julius"));
		assertThat(authors.get(0).getSurname(), is("Ceasar"));
		assertThat(authors.get(1).getName(), is("John"));
		assertThat(authors.get(1).getSurname(), is("Rambo"));
		assertThat(authors.get(2).getName(), is("John"));
		assertThat(authors.get(2).getSurname(), is("McClain"));
	}

	@Test
	void testPostAuthorEndpoint() throws JsonProcessingException {
		String postAuthorUrl = createURLWithPort("/author/add");

		Author charles = new Author("Charles", "Bukowski", 1920);
		Author mark = new Author("Mark", "Twain", 1835);
		Author frank = new Author("Frank", "Herbert", 1920);

		testRequests.post(postAuthorUrl, charles);
		testRequests.post(postAuthorUrl, mark);
		testRequests.post(postAuthorUrl, frank);

		String authorUrl = createURLWithPort("/author");

		ResponseEntity<String> response = testRequests.get(authorUrl);

		ObjectMapper objectMapper = new ObjectMapper();

		List<Author> authors = objectMapper.readValue(response.getBody(), new TypeReference<List<Author>>() {
		});
		assertThat(authors.size(), is(6));
		assertThat(authors.get(3).getName(), is("Charles"));
		assertThat(authors.get(3).getSurname(), is("Bukowski"));
		assertThat(authors.get(3).getYearOfBirth(), is(1920));
		assertThat(authors.get(4).getName(), is("Mark"));
		assertThat(authors.get(4).getSurname(), is("Twain"));
		assertThat(authors.get(4).getYearOfBirth(), is(1835));
		assertThat(authors.get(5).getName(), is("Frank"));

		assertThat(authors.get(5).getSurname(), is("Herbert"));
		assertThat(authors.get(5).getYearOfBirth(), is(1920));
	}

	@Test
	void testDeleteAuthorEndpoint() throws JsonProcessingException {
		String authorUrl = createURLWithPort("/author");

		Iterable<Author> authors = authorRepository.findAll();

		Long authorsCounter = countGetResult(Collections.singleton(authors));

		testRequests.delete(authorUrl + "/" + 1);

		assertThat(authorsCounter - 1, is(countGetResult(Collections.singleton(authorRepository.findAll()))));

		testRequests.delete(authorUrl + "/" + 2);

		assertThat(authorsCounter - 2, is(countGetResult(Collections.singleton(authorRepository.findAll()))));

		for (Author author : authorRepository.findAll()) {
			Integer authorId = author.getId();
			String deleteAuthorUrl = authorUrl + "/" + authorId;
			testRequests.delete(deleteAuthorUrl);
		}

		assertThat(countGetResult(Collections.singleton(authorRepository.findAll())), is(0L));

	}

	@Test
	void testGetAuthorById() throws JsonProcessingException {
		String authorUrl = createURLWithPort("/author");

		ObjectMapper objectMapper = new ObjectMapper();

		ResponseEntity<String> ceasarResponse = testRequests.get(authorUrl + "/" + 20);
		ResponseEntity<String> ramboResponse = testRequests.get(authorUrl + "/" + 50);
		ResponseEntity<String> mcclainResponse = testRequests.get(authorUrl + "/" + 30);

		Author ceasar = objectMapper.readValue(ceasarResponse.getBody(), new TypeReference<>() {
		});
		Author rambo = objectMapper.readValue(ramboResponse.getBody(), new TypeReference<>() {
		});
		Author mcclain = objectMapper.readValue(mcclainResponse.getBody(), new TypeReference<>() {
		});

		assertThat(ceasar.getName(), is ("Julius"));
		assertThat(ceasar.getSurname(), is ("Ceasar"));
		assertThat(rambo.getName(), is ("John"));
		assertThat(rambo.getSurname(), is ("Rambo"));
		assertThat(mcclain.getName(), is ("John"));
		assertThat(mcclain.getSurname(), is ("McClain"));
	}
}
