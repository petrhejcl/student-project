# Project Spring Boot, Rest, PostgreSQL, Docker, Github

Project is specifically created to help solvers of this learn Java, Rest, Docker and other
technologies. Task assignment is situated as complete project which includes development, testing, dev ops, ...

In this project you should create Rest API for library. Via Rest API you should be able to create
Book, Authors and Libraries. 
## Assignment

1. Create data model for your library Rest API
    - Deploy PostgreSQL database in container
        - To deploy this database you should get familiar with Docker and Postgres
        - These links should help you to get familiar with these technologies
            1. [Docker documentation](https://docs.docker.com/get-started/overview/)
            2. [PostgreSQL tutorial](https://www.postgresqltutorial.com/) 
            3. [Example project](https://github.com/obabec/iis-projekt/tree/main/docker/db)
        - All necessary files for the database deployment should live in docker/db folder. 
          sql_dump.sql script should contain database dump that will be used to initialize the database
          container.
        - Create `docker-compose.yml` file that will allow to deploy database using [docker-compose](https://learn.microsoft.com/cs-cz/dotnet/architecture/microservices/multi-container-microservice-net-applications/multi-container-applications-docker-compose).
          Later in this project you will add also this rest API to this compose.
    - Create database entries for Book, Author and Library. You can imagine all relations
      between these these tables. Author should have all necessary attributes (name, surname, date of
      birth and other attributes), Book (name, release date, isbn, author, ...), Library (name, address, 
      books, ...)
    - Create Java classes that should map these database entries. All these classes should live in `model`
      module where you can also find some examples.
2. Create Rest endpoints and database services
   - Once all database mapping is done you should create all necessary endpoints. How these endpoints
     will look like is entirely up to you. You should just keep in mind that you should allow only operations
     that are necessary for end user.
   - Also keep in mind that you should validate all the values that are handed to the application.
     (For example, forbidden characters should not be passed. Name has some rules (a-zA-Z),...)
   - You can find examples of services in `AuthorServiceImpl`. All endpoints that should exec some operation
     will receive object via [JSON](https://www.baeldung.com/spring-mvc-send-json-parameters) format.
