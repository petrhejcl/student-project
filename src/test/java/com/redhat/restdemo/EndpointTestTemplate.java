package com.redhat.restdemo;

import com.redhat.restdemo.utils.TestRequests;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.util.Collection;

@RunWith(SpringRunner.class)
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EndpointTestTemplate {

    @Value("http://localhost:${local.server.port}")
    String baseUrl;

    static int idCounter = 0;

    TestRequests testRequests = new TestRequests();

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

    protected String createURLWithPort(String uri) {
        return baseUrl + uri;
    }

    protected <T> ResponseEntity<String> postAndIncrease(String url, T object) {
        ResponseEntity<String> response = testRequests.post(url, object);
        if (response.getStatusCode().is2xxSuccessful()) {
            idCounter++;
        }
        return response;
    }

    protected <T, ID> void prepareSchema(CrudRepository<T, ID> repository, String url, Collection<T> data) throws IOException {
        repository.deleteAll();

        for (Object object : data) {
            ResponseEntity<String> response = postAndIncrease(url, object);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new IOException("Preparing schema was not successful");
            }
        }
    }
}
