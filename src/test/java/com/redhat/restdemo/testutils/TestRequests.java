package com.redhat.restdemo.testutils;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Getter
@Setter
public class TestRequests {
    static final RestTemplate restTemplate = new RestTemplate();
    static {
        restTemplate.setErrorHandler(new CustomResponseErrorHandler());
    }
    private final HttpHeaders headers = new HttpHeaders();

    public ResponseEntity<String> post(String url, Object object) {
        HttpEntity<Object> request = new HttpEntity<>(object, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        return response;
    }

    public ResponseEntity<String> get(String url) {
        HttpEntity<Object> request = new HttpEntity<>(null, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
        return response;
    }

    public ResponseEntity<String> put(String url, Object object) {
        HttpEntity<Object> request = new HttpEntity<>(object, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, request, String.class);
        return response;
    }

    public ResponseEntity<String> delete(String url) {
        HttpEntity<Object> request = new HttpEntity<>(null, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);
        return response;
    }
}
