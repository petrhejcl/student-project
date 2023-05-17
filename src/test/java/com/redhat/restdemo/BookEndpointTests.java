package com.redhat.restdemo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.restdemo.model.entity.Book;
import com.redhat.restdemo.model.entity.Book;
import com.redhat.restdemo.model.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
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

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(SpringRunner.class)
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookEndpointTests {
    @Value("http://localhost:${local.server.port}")
    String baseUrl;

    TestRestTemplate restTemplate = new TestRestTemplate();

    HttpHeaders headers = new HttpHeaders();

    @Autowired
    private BookRepository bookRepository;

    TestRequests testRequests = new TestRequests();

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
        String bookUrl = createURLWithPort("/book");

        ResponseEntity<String> response = testRequests.get(bookUrl);

        ObjectMapper objectMapper = new ObjectMapper();
        List<Book> books = objectMapper.readValue(response.getBody(), new TypeReference<List<Book>>() {
        });
        assertThat(books.size(), is(13));
        assertThat(books.get(0).getName(), is("Dune"));
        assertThat(books.get(0).getGenre(), is("Sci-Fi"));
        assertThat(books.get(1).getName(), is("The Lord of the Rings"));
        assertThat(books.get(1).getGenre(), is("Fantasy"));
        assertThat(books.get(2).getName(), is("The Hitchhikers Guide to the Galaxy"));
        assertThat(books.get(2).getGenre(), is("Sci-Fi"));
        assertThat(books.get(3).getName(), is("Gone Girl"));
        assertThat(books.get(3).getGenre(), is("Mystery"));
        assertThat(books.get(4).getName(), is("The Girl on the Train"));
        assertThat(books.get(4).getGenre(), is("Thriller"));
        assertThat(books.get(5).getName(), is("The Book Thief"));
        assertThat(books.get(5).getGenre(), is("Historical Fiction"));
        assertThat(books.get(6).getName(), is("Enders Game"));
        assertThat(books.get(6).getGenre(), is("Sci-Fi"));
        assertThat(books.get(7).getName(), is("The Da Vinci Code"));
        assertThat(books.get(7).getGenre(), is("Mystery"));
        assertThat(books.get(8).getName(), is("Harry Potter and the Sorcerers Stone"));
        assertThat(books.get(8).getGenre(), is("Fantasy"));
        assertThat(books.get(9).getName(), is("Pride and Prejudice"));
        assertThat(books.get(9).getGenre(), is("Romance"));
        assertThat(books.get(10).getName(), is("Neuromancer"));
        assertThat(books.get(10).getGenre(), is("Sci-Fi"));
        assertThat(books.get(11).getName(), is("The Silence of the Lambs"));
        assertThat(books.get(11).getGenre(), is("Thriller"));
        assertThat(books.get(12).getName(), is("1984"));
        assertThat(books.get(12).getGenre(), is("Dystopian"));
    }

    @Test
    void testGetBooksById() throws JsonProcessingException {
        String bookUrl = createURLWithPort("/book");

        ObjectMapper objectMapper = new ObjectMapper();

        ResponseEntity<String> goneGirlResponse = testRequests.get(bookUrl + "/" + 55);
        ResponseEntity<String> daVinciCodeResponse = testRequests.get(bookUrl + "/" + 95);
        ResponseEntity<String> neuromancerResponse = testRequests.get(bookUrl + "/" + 122);

        Book goneGirl = objectMapper.readValue(goneGirlResponse.getBody(), new TypeReference<>() {
        });
        Book daVinciCode = objectMapper.readValue(daVinciCodeResponse.getBody(), new TypeReference<>() {
        });
        Book neuromancer = objectMapper.readValue(neuromancerResponse.getBody(), new TypeReference<>() {
        });

        assertThat(goneGirl.getName(), is ("Gone Girl"));
        assertThat(goneGirl.getGenre(), is ("Mystery"));
        assertThat(daVinciCode.getName(), is ("The Da Vinci Code"));
        assertThat(daVinciCode.getGenre(), is ("Mystery"));
        assertThat(neuromancer.getName(), is ("Neuromancer"));
        assertThat(neuromancer.getGenre(), is ("Sci-Fi"));
    }

}
