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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

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

	@Autowired
	private AuthorRepository userRepository;

	private static final PostgreSQLContainer postgresqlContainer;

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

	@Test
	@Sql({"/prepare_schema.sql"})
	void testGeneralEndpoint() throws JsonProcessingException {

		HttpEntity<String> entity = new HttpEntity<>(null, headers);

		ResponseEntity<String> response = restTemplate.exchange(
				createURLWithPort("/authors"),
				HttpMethod.GET, entity, String.class);
		ObjectMapper objectMapper = new ObjectMapper();
		List<Author> authors = objectMapper.readValue(response.getBody(), new TypeReference<List<Author>>(){});
		assertThat(authors.size(), is(3));
		assertThat(authors.get(0).getName(), is("Julius"));
		assertThat(authors.get(0).getSurname(), is("Ceasar"));
		assertThat(authors.get(1).getName(), is("John"));
		assertThat(authors.get(1).getSurname(), is("Rambo"));
		assertThat(authors.get(2).getName(), is("John"));
		assertThat(authors.get(2).getSurname(), is("McClain"));
	}
}
