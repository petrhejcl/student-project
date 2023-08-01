package com.redhat.restdemo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.restdemo.utils.TestRequests;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.junit.jupiter.Testcontainers;

@RunWith(SpringRunner.class)
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EndpointTestTemplate {

    @Value("http://localhost:${local.server.port}")
    String baseUrl;

    protected TestRequests testRequests = new TestRequests();

    protected ObjectMapper objectMapper = new ObjectMapper();

    protected String createURLWithPort(String uri) {
        return baseUrl + uri;
    }
}
