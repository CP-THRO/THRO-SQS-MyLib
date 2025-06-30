## 1. Introduction And Goals

## 1.1 Purpose

MyLib is a personal library management system allowing users to search for books via the OpenLibrary API and manage a private collection. Users can:

- Search for books
- View book details (cover, title, authors, description, publish date, ISBNs, average rating by all users in the system)
- Add books to a personal library or wishlist
- Transfer books directly from wishlist to library
- Assign personal ratings (1–5) to books in the library
- Manage reading status (unread, currently reading, already read)
- View an aggregated overview of books owned amd wished for by all users (anonymized)

MyLib exposes public endpoints for searching and viewing book details, and secured endpoints for user-specific operations.


## 1.2 Stakeholders

| Stakeholder             | Description                                               |
|--------------------------|-----------------------------------------------------------|
| End Users               | Individuals managing personal book collections            |
| Me                      | Building the system, wanting to pass the course          |
| University Lecturers    | Evaluators of the system for SQS project compliance       |
| External Service Provider | OpenLibrary, providing book data via public API        |


## 1.3 Quality Goals

| Priority | Quality Goal                           | Description                                                       |
|----------|----------------------------------------|-------------------------------------------------------------------|
| High     | Security                               | All user-specific operations secured (authentication, authorization). |
| High     | Resilience                             | Fault-tolerant integration with OpenLibrary API.                  |
| High     | Maintainability                        | Modular architecture; clean code without static analysis issues. |
| Medium   | Performance                            | Reasonably fast response times for book searches and user operations. |
| Medium   | Usability                              | Simple and intuitive user interface.                             |

## 2. Constraints

Due to the requirements given by the lecuturer, the project has the following constraints:re

- Programming languages allowed:
  - Python
  - TypeScript
  - Java
  - C#
- Must provide:
  - At least one publicly accessible endpoint
  - At least one secured endpoint
  - Three-layer architecture:
    - Frontend
    - Backend
    - Persistence layer
- Backend must integrate with at least one external service
- Must use GitHub for source code and version management
- Must GitHub Actions for pipelines

- Further personal constraints:
    - Limited experience in backend development
    - No experience in frontend development

This project was developed under time pressure due to delays in my bachelors thesis.

## 3. System Scope and Context
![Context Diagram](img/Context.png)

MyLib is a web application directly accessed by users via a web browser.

Two user types interact with the system:

- **Unregistered User**
  - Can search for books via the OpenLibrary API
  - Can view details about books
  - Can view the aggregated list of books saved by all registered users (anonymized)

- **Registered User**
  - Has all capabilities of an unregistered user
  - Can add books to a personal library
  - Can add books to a wishlist
  - Can transfer books from wishlist to library
  - Can set personal ratings for books (1–5)
  - Can manage reading status for books in the library (unread, currently reading, already read)

MyLib communicates with the following external system:

- **OpenLibrary API**
  - Provides book metadata (titles, authors, covers, descriptions, etc.)
  - Accessed via REST API


## 4. Solution Strategy

### Backend

The backend is implemented in **Spring Boot** (Java). Reasons for this choice:

- Prior experience with the framework
- Widespread adoption and large community support
- Strong integration with persistence frameworks such as Hibernate
- Built-in support for security and REST API development


### Frontend

The frontend is implemented with **Vue.js** and **TypeScript**. Reasons:

- Simplicity and lower learning curve for a single-developer project
- Good documentation and tooling
- Allows type safety via TypeScript


### Database

**PostgreSQL** is used as the database system. Integrated with Spring Boot via Hibernate, enabling:

- Database interaction without custom SQL statements
- Automatic handling of database connections and transactions
- Protection against SQL injection via prepared statements
- Straightforward configuration within Spring Boot projects

### Deployment

Deployment uses **Docker Compose**, bundling all system components (frontend, backend, database) into containers. This ensures:

- Simplified deployment using a single configuration file
- System can be deployment with minimal commands
- Environment consistency across development, testing, and production


### Security

Secured endpoints are protected using JWT (JSON Web Token) authentication:
- Widespread industry use
- Straightforward integration into Spring Security
- Simplifies stateless authentication across frontend and backend

### External Service Integration
External Service Integration

The backend integrates with the OpenLibrary API to fetch book data. 

## 5. Building Block View
### 5.1 Backend
![Context Diagram](img/Components-Backend.png)
#### 5.1.1 Overview

The backend of MyLib is implemented in Spring Boot (Java). It consists of:
- Controllers exposing REST endpoints
- Services implementing business logic
- Repositories handling data persistence
- Flyweights caching external API results
- Integration with the OpenLibrary API


#### 5.1.2 Backend Components

###### Controllers

| Component         | Responsibility                                                         |
|--------------------|-------------------------------------------------------------------------|
| **AuthController** | Handles user authentication and registration endpoints.                |
| **BookController** | Exposes endpoints for book data, user libraries, and wishlists.        |
| **SearchController** | Exposes endpoint to search books via OpenLibrary API.              |


###### Services

| Component                   | Responsibility                                                                  |
|-----------------------------|---------------------------------------------------------------------------------|
| **AuthService**             | Implements business logic for user login and registration.                      |
| **BookService**             | Handles business logic for managing books, personal libraries, and wishlists.   |
| **CustomUserDetailsService** | Integrates with Spring Security to load user data and create new users.         |
| **JWTService**              | Generates and validates JWT tokens for secure endpoints.                        |
| **SearchService**           | Coordinates searches to the OpenLibrary API and manages caching of results.     |
| **ExternalBookFlyweightFactory** | Caches individual book data fetched from OpenLibrary.                       |
| **SearchResultFlyweightFactory** | Caches search results from OpenLibrary queries.                            |
| **OpenLibraryAPI**          | Handles communication with the external OpenLibrary REST API.                  |


###### Repositories

| Component               | Responsibility                                                         |
|--------------------------|-------------------------------------------------------------------------|
| **BookRepository**       | Stores book data in the database.                                      |
| **UserRepository**       | Stores user data and wishlist relations.                               |
| **LibraryBookRepository** | Manages many-to-many relations between users and books, including additional user-specific metadata such as reading status and ratings. |


### 5.2 Frontend

![Context Diagram](img/Components-Frontend.png)

#### 5.1.1 Overview

The frontend of MyLib is implemented with Vue.js and TypeScript. It consists of:

- Vue single-file components representing application pages
- Shared services and composables providing state management and reusable logic
- A centralized API service handling communication with the backend

#### 5.1.1 Overview

#### 5.1.2 Frontend Components

##### Pages

| Component              | Responsibility                                                                |
|-------------------------|-------------------------------------------------------------------------------|
| **AllBooks.vue**       | Displays all books stored in the database; serves as the application homepage. |
| **Search.vue**         | Displays the book search page for querying the OpenLibrary API.               |
| **Book.vue**           | Displays details about a specific book.                                       |
| **Library.vue**        | Shows books currently in the authenticated user's personal library.           |
| **Wishlist.vue**       | Shows books in the authenticated user's wishlist.                             |
| **Login.vue**      | Provides login and registration functionality.                                |


##### Shared Components & Services

| Component               | Responsibility                                                                 |
|--------------------------|---------------------------------------------------------------------------------|
| **ApiService**          | Handles all HTTP communication with the backend via REST.                      |
| **AuthInfoWrapper**     | Provides global authentication state for elements where it matters if a user is authenticated or not |
| **useBookActions**      | Exposes shared methods for adding/removing books in library or wishlist.        |
| **useBookList**         | Manages loading of book lists and pagination.                  |
| **usePaginationState**  | Stores and restores pagination state for each page for consistent navigation across pages.    |
| **App.vue**             | Main application entry point, contains global layout and navigation bar. |


## 6. Runtime View

This section describes some typical runtime scenarios for MyLib, illustrating how components collaborate at runtime.

### Scenario 1 – Unregistered User Searches for Books

- **Actor:** Unregistered user
- **Frontend Flow:**
  - User accesses `Search.vue` via the browser, enters keywords and clicks the search button.
  - `Search.vue` invokes `useBookList`.
  - `useBookList` calls `ApiService` to request search results.
- **Backend Flow:**
  - `SearchController` receives the request.
  - `SearchService` checks the `SearchResultFlyweightFactory` cache.
    - **On Success** (exists and not stale):
        - Return cached result to frontend.
    - **On Failure** (does not exist or stale):
        - `SearchResultFlyweightFactory` calls `OpenLibraryAPI`.
        - `OpenLibraryAPI` queries the OpenLibrary API with the search keywords and returns the result to `SearchResultFlyweightFactory`.
        - Result is cached and returned to `SearchService`.
        - Result is returned to frontend.
- **End Result:**
  - `Search.vue` displays the search results.



## Scenario 2 – Unregistered User Views Book Details

- **Actor:** Unregistered user
- **Frontend Flow:**
  - User navigates to `Book.vue` by clicking a "Details" button on `AllBooks.vue`, `Search.vue`, `Library.vue` or `Wishlist.vue`.
  - `Book.vue` calls `ApiService` for book details.
- **Backend Flow:**
  - `BookController` handles the request.
  - `BookService` attempts to load book data from `BookRepository`.
    - **On Success** (exists in database):
        - Return book to frontend.
    - **On Failure** (does not exist in database):
        - `BookService` checks the `ExternalBookFlyweightFactory` cache.
            - **On Success** (exists and not stale):
                - Return cached book to frontend.
            - **On Failure** (does not exist or stale):
                - `ExternalBookFlyweightFactory` calls `OpenLibraryAPI`.
                - `OpenLibraryAPI` queries OpenLibrary for the book and returns it to `ExternalBookFlyweightFactory`.
                - Result is cached and returned to `BookService`.
                - Result is returned to frontend.
- **End Result:**
  - `Book.vue` displays the book information.

## Scenario 3 – User Logs In

- **Actor:** Registered user
- **Frontend Flow:**
  - User submits credentials in `Login.vue` and clicks the "Login" button.
  - `ApiService` sends login request.
- **Backend Flow:**
  - `AuthController` receives the request.
  - `AuthService` validates credentials.
  - On success:
    - `JWTService` generates a JWT token.
    - Token is returned to frontend.
- **Frontend Post-login:**
  - `ApiService` stores authentication status in `AuthInfoWrapper` and JWT token in LocalStorage for future requests.
  - `Login.vue` redirects to home page or the page the user was last on.


## Scenario 4 – Registered User Adds Book to Library

- **Actor:** Registered user
- **Frontend Flow:**
  - User clicks “Add to Library” on any book component (e.g. `Book.vue`).
  - `useBookActions` calls `ApiService`.
  - JWT token is included in the request header.
- **Backend Flow:**
  - `BookController` receives the secured request.
  - Spring Security validates user identity.
  - `BookService` checks if the book exists in `BookRepository`.
    - **On Failure** (book not in database):
        - `BookService` checks the `ExternalBookFlyweightFactory` cache.
            - **On Failure** (does not exist or stale):
                - `ExternalBookFlyweightFactory` calls `OpenLibraryAPI`.
                - `OpenLibraryAPI` queries OpenLibrary for the book and returns it to `ExternalBookFlyweightFactory`.
                - Result is cached and returned to `BookService`.
    - `BookService` creates a new entry in `LibraryBookRepository`.
- **Result:**
  - Success response returned.
  - Frontend updates the page the user is on.


## Scenario 5 – Registered User Updates Book Rating

- **Actor:** Registered user
- **Frontend Flow:**
  - User updates personal rating on `Book.vue` and clicks the "Save" button.
  - `useBookActions` calls `ApiService`.
- **Backend Flow:**
  - `BookController` receives the secured request.
  - `BookService` updates the rating in `LibraryBookRepository`.
- **Result:**
  - Success response returned.
  - Frontend updates the `Book.vue` page.

## 7. Deployment View

## 8. Crosscutting Concepts

## 9. Architectural Desicions

## 10. Quality Requirements

## 11. Risks & Technical Debt

## 12. Gloassary



