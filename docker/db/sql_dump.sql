--
-- PostgreSQL database dump
--

-- Dumped from database version 14.3
-- Dumped by pg_dump version 14.3

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: public; Type: SCHEMA; Schema: -; Owner: compose-postgres
--

CREATE SCHEMA public;


ALTER SCHEMA public OWNER TO "compose-postgres";

--
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: compose-postgres
--

COMMENT ON SCHEMA public IS 'standard public schema';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: author; Type: TABLE; Schema: public; Owner: compose-postgres
--

CREATE TABLE public.author (
    id integer NOT NULL,
    name character varying(60),
    surname character varying(80),
    yearOfBirth integer
);


ALTER TABLE public.author OWNER TO "compose-postgres";

--
-- Name: authorship; Type: TABLE; Schema: public; Owner: compose-postgres
--

CREATE TABLE public.authorship (
    authorId integer NOT NULL,
    bookISBN character varying(13) NOT NULL,
    authorshipOrder integer
);


ALTER TABLE public.authorship OWNER TO "compose-postgres";

--
-- Name: book; Type: TABLE; Schema: public; Owner: compose-postgres
--

CREATE TABLE public.book (
    isbn character varying(13) NOT NULL,
    name character varying(150),
    release date,
    genre character varying(30)
);


ALTER TABLE public.book OWNER TO "compose-postgres";

--
-- Name: genre; Type: TABLE; Schema: public; Owner: compose-postgres
--

CREATE TABLE public.genre (
    genre character varying(30) NOT NULL,
    description character varying(100)
);


ALTER TABLE public.genre OWNER TO "compose-postgres";

--
-- Name: library; Type: TABLE; Schema: public; Owner: compose-postgres
--

CREATE TABLE public.library (
    id integer NOT NULL,
    name character varying(80),
    city character varying(80),
    street character varying(80),
    streetNumber integer,
    description character varying(150)
);


ALTER TABLE public.library OWNER TO "compose-postgres";

--
-- Name: ownership; Type: TABLE; Schema: public; Owner: compose-postgres
--

CREATE TABLE public.ownership (
    bookISBN character varying(13) NOT NULL,
    libraryId integer NOT NULL
);


ALTER TABLE public.ownership OWNER TO "compose-postgres";

--
-- Data for Name: author; Type: TABLE DATA; Schema: public; Owner: compose-postgres
--

COPY public.author (id, name, surname, year_of_birth) FROM stdin;
3	Charles	Bukowski	1920
1	Frank	Herbert	1920
2	Mark	Twain	1910
\.


--
-- Data for Name: authorship; Type: TABLE DATA; Schema: public; Owner: compose-postgres
--

COPY public.authorship (author_id, book_isbn, authorship_order) FROM stdin;
1	9780441172719	1
\.


--
-- Data for Name: book; Type: TABLE DATA; Schema: public; Owner: compose-postgres
--

COPY public.book (isbn, name, release, genre) FROM stdin;
9780441172719	Dune	1965-08-01	Sci-Fi
\.


--
-- Data for Name: genre; Type: TABLE DATA; Schema: public; Owner: compose-postgres
--

COPY public.genre (genre, description) FROM stdin;
Sci-Fi	Scince fictional stories
\.


--
-- Data for Name: library; Type: TABLE DATA; Schema: public; Owner: compose-postgres
--

COPY public.library (id, name, city, street, street_number, description) FROM stdin;
1	Knihovna FI MUNI	Brno	Botanická	554	\N
\.


--
-- Data for Name: ownership; Type: TABLE DATA; Schema: public; Owner: compose-postgres
--

COPY public.ownership (book_isbn, library_id) FROM stdin;
9780441172719	1
\.


--
-- Name: author author_pk; Type: CONSTRAINT; Schema: public; Owner: compose-postgres
--

ALTER TABLE ONLY public.author
    ADD CONSTRAINT author_pk PRIMARY KEY (id);


--
-- Name: authorship authorship_pk; Type: CONSTRAINT; Schema: public; Owner: compose-postgres
--

ALTER TABLE ONLY public.authorship
    ADD CONSTRAINT authorship_pk PRIMARY KEY (author_id, book_isbn);


--
-- Name: book book_pk; Type: CONSTRAINT; Schema: public; Owner: compose-postgres
--

ALTER TABLE ONLY public.book
    ADD CONSTRAINT book_pk PRIMARY KEY (isbn);


--
-- Name: genre genre_pk; Type: CONSTRAINT; Schema: public; Owner: compose-postgres
--

ALTER TABLE ONLY public.genre
    ADD CONSTRAINT genre_pk PRIMARY KEY (genre);


--
-- Name: library library_pk; Type: CONSTRAINT; Schema: public; Owner: compose-postgres
--

ALTER TABLE ONLY public.library
    ADD CONSTRAINT library_pk PRIMARY KEY (id);


--
-- Name: ownership ownership_pk; Type: CONSTRAINT; Schema: public; Owner: compose-postgres
--

ALTER TABLE ONLY public.ownership
    ADD CONSTRAINT ownership_pk PRIMARY KEY (library_id, book_isbn);


--
-- Name: authorship authorship_author_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: compose-postgres
--

ALTER TABLE ONLY public.authorship
    ADD CONSTRAINT authorship_author_id_fk FOREIGN KEY (author_id) REFERENCES public.author(id);


--
-- Name: authorship authorship_book_isbn_fk; Type: FK CONSTRAINT; Schema: public; Owner: compose-postgres
--

ALTER TABLE ONLY public.authorship
    ADD CONSTRAINT authorship_book_isbn_fk FOREIGN KEY (book_isbn) REFERENCES public.book(isbn);


--
-- Name: book book_genre_genre_fk; Type: FK CONSTRAINT; Schema: public; Owner: compose-postgres
--

ALTER TABLE ONLY public.book
    ADD CONSTRAINT book_genre_genre_fk FOREIGN KEY (genre) REFERENCES public.genre(genre) ON DELETE CASCADE;


--
-- Name: ownership ownership_book_isbn_fk; Type: FK CONSTRAINT; Schema: public; Owner: compose-postgres
--

ALTER TABLE ONLY public.ownership
    ADD CONSTRAINT ownership_book_isbn_fk FOREIGN KEY (book_isbn) REFERENCES public.book(isbn);


--
-- Name: ownership ownership_library_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: compose-postgres
--

ALTER TABLE ONLY public.ownership
    ADD CONSTRAINT ownership_library_id_fk FOREIGN KEY (library_id) REFERENCES public.library(id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--