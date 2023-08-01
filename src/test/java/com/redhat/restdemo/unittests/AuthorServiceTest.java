package com.redhat.restdemo.unittests;

import com.redhat.restdemo.model.service.AuthorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
<<<<<<< HEAD
=======
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.shaded.org.bouncycastle.est.jcajce.JsseDefaultHostnameAuthorizer;
>>>>>>> d8e328c851a89f177371bad21eef79970594128e

import static org.mockito.Mockito.mock;

class AuthorServiceTest {

    private AuthorService authorService;

    @BeforeEach
    void setUp() {
        authorService = mock(AuthorService.class);
    }

    @Test
    void findAll() {

    }

}
