# Demo Spring Boot Application

This is a demo Spring Boot application showcasing a RESTful API with user management, authentication, and other common features.

## Features

*   **RESTful API**: Exposes endpoints for managing users.
*   **Spring Security**: Secure API endpoints using JWT (JSON Web Tokens) for authentication and authorization.
*   **H2 Database**: In-memory database for development and testing purposes.
*   **JPA & Hibernate**: Data persistence using Spring Data JPA.
*   **Validation**: Request payload validation using `jakarta.validation`.
*   **Lombok**: Reduces boilerplate code (getters, setters, constructors, etc.).
*   **MapStruct**: Simplifies mapping between DTOs and entity objects.
*   **Actuator**: Provides production-ready features like monitoring and management.
*   **OpenAPI/Swagger UI**: Automatic API documentation and an interactive interface to test endpoints.

## Technologies Used

*   **Java 17**
*   **Spring Boot 3.x**
*   **Gradle**
*   **Spring Data JPA**
*   **Spring Security**
*   **JWT (JSON Web Tokens)**
*   **H2 Database**
*   **Lombok**
*   **MapStruct**
*   **Springdoc OpenAPI UI**

## Getting Started

### Prerequisites

*   Java Development Kit (JDK) 17 or higher
*   Gradle (usually bundled with Spring Boot projects)

### Installation

1.  **Clone the repository:**
    ```bash
    git clone <repository-url>
    cd demo
    ```

2.  **Build the project:**
    ```bash
    ./gradlew build
    ```

### Running the Application

You can run the application using the Spring Boot Gradle plugin:

```bash
./gradlew bootRun
```

The application will start on `http://localhost:8080`.

## API Endpoints

The API documentation is available via Swagger UI:

*   **Swagger UI**: `http://localhost:8080/swagger-ui.html`
*   **API Docs (JSON)**: `http://localhost:8080/api-docs`

### Key Endpoints (Base URL: `/api/v1/users`)

*   `POST /register`: Register a new user.
*   `POST /login`: Login and obtain a JWT token.
*   `GET /`: Retrieve all users (Requires authentication).
*   `GET /{id}`: Get user by ID.
*   `GET /username/{username}`: Get user by username.
*   `PUT /{id}`: Update user details.
*   `PATCH /{id}/status`: Update user status.
*   `DELETE /{id}`: Delete a user.

## Database Access

This application uses an H2 in-memory database for development. You can access the H2 console to view and manage the database:

*   **H2 Console**: `http://localhost:8080/h2-console`
    *   **JDBC URL**: `jdbc:h2:mem:testdb` (If not specified, check console output or application.properties)
    *   **Username**: `sa`
    *   **Password**: (leave blank)

## Security

The application uses JWT for securing its REST endpoints.

*   To access protected resources, you will need to obtain a JWT token by authenticating with the login endpoint (`/api/v1/users/login`).
*   Include the obtained JWT token in the `Authorization` header of your requests as a Bearer token: `Authorization: Bearer <your-jwt-token>`.
