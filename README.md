# Spring Boot 4 REST API Base Template (Java 21)

A clean, production-grade Spring Boot 4 starter template following modern Java & REST industry best practices. Zero Lombok dependency — clones, builds, and runs cleanly on any machine or IDE out of the box.

## Tech Stack & Industry Standards Included

- **Spring Boot 4.1.3** on **Java 21** (Spring Framework 7, Jackson 3, Hibernate ORM 7, Jakarta EE 11)
- **Complete REST CRUD + Partial Update:** `POST` (201 + `Location` header), `GET` (single & paginated/sorted/filtered with sort-field allowlist), `PUT` (200), `PATCH` (200), `DELETE` (204)
- **Clean Layered Architecture:** `Controller` -> `Service` (interface + impl) -> `Mapper` -> `Repository` -> Encapsulated `Entity`
- **Java `record` DTOs** with Jakarta Bean Validation (`@Valid`) — entities are never exposed directly
- **Consistent Envelopes:** `ApiResponse<T>`, `PagedResponse<T>`, and `ApiErrorResponse` (with `List<FieldViolation>` so multiple errors on the same field are preserved)
- **Global Exception Handling (`@RestControllerAdvice`):** Handles `400` (validation, bad request, malformed JSON, type mismatch), `404`, `405`, `409` (duplicate resource, optimistic locking, DB constraint), and `500`
- **Safe Persistence Design:** `BigDecimal` pricing, `@Version` optimistic locking, database indexes, `@PrePersist`/`@PreUpdate` timestamps, `open-in-view: false`, class-level `@Transactional(readOnly = true)`
- **Observability & Dev Experience:** Spring Boot Actuator (`/actuator/health`), H2 in-memory DB + `spring-boot-h2console` (`/h2-console`), configurable CORS, and commented PostgreSQL/MySQL/Oracle configs
- **Comprehensive Tests:** Context load (`@SpringBootTest`), Service unit tests (JUnit + Mockito + AssertJ), and Web slice tests (`@WebMvcTest` + `MockMvc` + `@MockitoBean` + Jackson 3 `JsonMapper`)

## Requirements

- JDK 21+
- Maven 3.9+

## Quick Start

```bash
# Run all unit & web slice tests
mvn test

# Start the application (http://localhost:8080)
mvn spring-boot:run
```

- **API Base URL:** `http://localhost:8080/api/v1/products`
- **Actuator Health:** `http://localhost:8080/actuator/health`
- **H2 Console:** `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:appdb`, User: `sa`, Password: *(empty)*)

## API Endpoints

| Method | Endpoint | Description | Success | Common Errors |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/products` | Create a product (returns `Location` header) | `201 Created` | `400`, `409` |
| `GET` | `/api/v1/products/{id}` | Get product by ID | `200 OK` | `400`, `404` |
| `GET` | `/api/v1/products?search=&status=&page=0&size=10&sortBy=id&sortDir=desc` | Paginated, filtered & sorted list | `200 OK` | `400` |
| `PUT` | `/api/v1/products/{id}` | Full update | `200 OK` | `400`, `404`, `409` |
| `PATCH` | `/api/v1/products/{id}` | Partial update | `200 OK` | `400`, `404`, `409` |
| `DELETE` | `/api/v1/products/{id}` | Delete product | `204 No Content` | `404` |

### Sample `curl` Commands

```bash
# 1. Create (POST)
curl -i -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{"sku":"SKU-001","name":"Mechanical Keyboard","description":"Tactile switches","price":89.99,"stockQuantity":25}'

# 2. Get All with Pagination, Filtering & Sorting (GET)
curl "http://localhost:8080/api/v1/products?page=0&size=10&status=ACTIVE&sortBy=price&sortDir=asc"

# 3. Get By ID (GET)
curl http://localhost:8080/api/v1/products/1

# 4. Full Update (PUT)
curl -X PUT http://localhost:8080/api/v1/products/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Mechanical Keyboard Pro","description":"Updated switches","price":99.99,"stockQuantity":20,"status":"ACTIVE"}'

# 5. Partial Update (PATCH)
curl -X PATCH http://localhost:8080/api/v1/products/1 \
  -H "Content-Type: application/json" \
  -d '{"price":79.50,"status":"OUT_OF_STOCK"}'

# 6. Delete (DELETE)
curl -i -X DELETE http://localhost:8080/api/v1/products/1
```

## Response Formats

### Success (`ApiResponse<T>`)
```json
{
  "success": true,
  "message": "Product created successfully",
  "data": {
    "id": 1,
    "sku": "SKU-001",
    "name": "Mechanical Keyboard",
    "description": "Tactile switches",
    "price": 89.99,
    "stockQuantity": 25,
    "status": "ACTIVE",
    "createdAt": "2026-11-25T17:30:00Z",
    "updatedAt": "2026-11-25T17:30:00Z"
  },
  "timestamp": "2026-11-25T17:30:00Z"
}
```

### Validation Error (`ApiErrorResponse`)
```json
{
  "success": false,
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/products",
  "timestamp": "2026-11-25T17:30:10Z",
  "fieldErrors": [
    { "field": "sku", "message": "SKU is required" },
    { "field": "price", "message": "Price must be greater than zero" }
  ]
}
```

## Project Structure

```text
spring-boot-rest-template/
├── pom.xml
├── .gitignore
├── LICENSE
├── README.md
└── src/
    ├── main/
    │   ├── java/com/template/api/
    │   │   ├── Application.java
    │   │   ├── config/        CorsConfig.java
    │   │   ├── controller/    ProductController.java
    │   │   ├── dto/
    │   │   │   ├── request/   CreateProductRequest, UpdateProductRequest, PatchProductRequest
    │   │   │   └── response/  ProductResponse, ApiResponse, PagedResponse, ApiErrorResponse
    │   │   ├── entity/        Product.java, ProductStatus.java
    │   │   ├── exception/     ResourceNotFoundException, DuplicateResourceException, BadRequestException, GlobalExceptionHandler
    │   │   ├── mapper/        ProductMapper.java
    │   │   ├── repository/    ProductRepository.java
    │   │   └── service/       ProductService.java, impl/ProductServiceImpl.java
    │   └── resources/
    │       └── application.yml
    └── test/
        └── java/com/template/api/
            ├── ApplicationTests.java
            ├── controller/    ProductControllerTest.java
            └── service/       ProductServiceImplTest.java
```

## Optional Production Checklist

- Add Maven Wrapper so anyone can build without local Maven installed: `mvn wrapper:wrapper`
- Replace H2 with PostgreSQL / MySQL / Oracle, set `spring.jpa.hibernate.ddl-auto: validate`, and add `spring-boot-starter-flyway`
- Restrict `app.cors.allowed-origins` to explicit frontend domains
- Add `spring-boot-starter-security` (JWT / OAuth2 Resource Server) and OpenAPI docs (`springdoc`) when needed
