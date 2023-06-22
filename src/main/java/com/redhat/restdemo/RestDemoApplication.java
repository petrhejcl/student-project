package com.redhat.restdemo;

import com.redhat.restdemo.model.entity.Author;
import com.redhat.restdemo.model.entity.Book;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RestDemoApplication {
	public static void main(String[] args) {
		SpringApplication.run(RestDemoApplication.class, args);

		System.out.println("hello world, I have just started up");
		Author charles = new Author("Charles", "Bukowski", 1920);
		Author mark = new Author("Mark", "Twain", 1835);
		Author frank = new Author("Frank", "Herbert", 1920);

		Book dune = new Book(9780441172719L, "Dune", 1965, "Sci-Fi");
		Book postOffice = new Book(9780061177576L, "Post Office", 1971, "Novel");

		String url = "http://localhost:8080";
		String author = "/author";
		String books = "/book";
		String postAuthorUri = author + "/add";
		String postBookUri = books + "/add";

		TestRequests testRequests = new TestRequests();

		testRequests.post(url + postAuthorUri, frank);
		testRequests.post(url + postAuthorUri, mark);
		testRequests.post(url + postAuthorUri, charles);

		testRequests.post(url + postBookUri, dune);
		testRequests.post(url + postBookUri, postOffice);

		System.out.println("hellou");
	}
}
