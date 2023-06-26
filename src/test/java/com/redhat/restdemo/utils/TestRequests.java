package com.redhat.restdemo.utils;

import com.redhat.restdemo.model.entity.Author;
import com.redhat.restdemo.utils.CustomResponseErrorHandler;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

@Getter
@Setter
public class TestRequests {
    static final RestTemplate restTemplate = new RestTemplate();
    static {
        restTemplate.setErrorHandler(new CustomResponseErrorHandler());
    }
    private final HttpHeaders headers = new HttpHeaders();
    public void assertStatus(HttpStatus httpStatus) {
        assert (httpStatus.value() >= 200);
        assert (httpStatus.value() < 300);
    }

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
