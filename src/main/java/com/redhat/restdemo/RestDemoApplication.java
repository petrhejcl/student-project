package com.redhat.restdemo;

import com.redhat.restdemo.model.entity.*;
import com.redhat.restdemo.model.repository.*;
import com.redhat.restdemo.utils.TestRequests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.reactive.filter.OrderedWebFilter;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class RestDemoApplication {
	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(RestDemoApplication.class, args);

		BookRepository bookRepository = context.getBean(BookRepository.class);
		LibraryRepository libraryRepository = context.getBean(LibraryRepository.class);
		AuthorRepository authorRepository = context.getBean(AuthorRepository.class);
		AuthorshipRepository authorshipRepository = context.getBean(AuthorshipRepository.class);
		OwnershipRepository ownershipRepository = context.getBean(OwnershipRepository.class);

		Author charles = new Author("Charles", "Bukowski", 1920);
		Author mark = new Author("Mark", "Twain", 1835);
		Author frank = new Author("Frank", "Herbert", 1920);

		authorRepository.save(charles);
		authorRepository.save(mark);
		authorRepository.save(frank);

		Book dune = new Book(9780441172719L, "Dune", 1965, "Sci-Fi");
		Book postOffice = new Book(9780061177576L, "Post Office", 1971, "Novel");

		bookRepository.save(dune);
		bookRepository.save(postOffice);

		Library brno = new Library("Brnenska knihovna", "Brno", "Sumavska", 111, "Skvela knihhovna");
		Library praha = new Library("Prazska knihovna", "Praha", "Vodickova", 222, "Skvela knihhovna v Praze");

		libraryRepository.save(brno);
		libraryRepository.save(praha);

		authorshipRepository.save(new Authorship(postOffice.getId(), charles.getId()));
		authorshipRepository.save(new Authorship(dune.getId(), mark.getId()));

		ownershipRepository.save(new Ownership(brno.getId(), postOffice.getId()));
		ownershipRepository.save(new Ownership(brno.getId(), dune.getId()));
		ownershipRepository.save(new Ownership(praha.getId(), postOffice.getId()));

		/*
		String url = "http://localhost:8080";
		String author = "/author";
		String books = "/book";
		String library = "/library";
		String postAuthorUri = author + "/add";
		String postBookUri = books + "/add";
		String postLibrary = library + "/add";


		TestRequests testRequests = new TestRequests();

		testRequests.post(url + postAuthorUri, frank);
		testRequests.post(url + postAuthorUri, mark);
		testRequests.post(url + postAuthorUri, charles);

		testRequests.post(url + postBookUri, dune);
		testRequests.post(url + postBookUri, postOffice);
		*/
	}
}
