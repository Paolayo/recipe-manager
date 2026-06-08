# Recipe Manager API

A production-ready REST API for managing favourite recipes, built with **Java 21** and **Spring Boot 3**.

---

## Table of Contents
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [Running in Production](#running-in-production)
- [API Documentation](#api-documentation)
- [Running Tests](#running-tests)
- [Design Decisions](#design-decisions)

---

## Architecture

The application follows a **classic layered architecture** (Controller → Service → Repository), chosen for its clarity, testability, and alignment with standard Spring Boot conventions:

```
┌──────────────────────────────────┐
│        REST Controller           │  HTTP, validation, request/response mapping
├──────────────────────────────────┤
│          Service Layer           │  Business logic, transactions
├──────────────────────────────────┤
│    Repository (Spring Data JPA)  │  Data access, dynamic filtering via Specifications
├──────────────────────────────────┤
│          PostgreSQL              │  Persistent storage (H2 for tests)
└──────────────────────────────────┘
```

### Package structure

```
com.abnamro.recipes/
├── controller/       RecipeController
├── service/          RecipeService (interface) + RecipeServiceImpl
├── repository/       RecipeRepository
├── specification/    RecipeSpecification  ← dynamic filtering
├── model/            Recipe, Ingredient (JPA entities)
├── dto/              RecipeRequest, RecipeResponse, RecipeFilter
├── mapper/           RecipeMapper (MapStruct)
├── exception/        RecipeNotFoundException, GlobalExceptionHandler
└── config/           OpenApiConfig
```

---

## Tech Stack

| Technology | Version | Reason |
|---|---|---|
| Java | 21 | LTS version, virtual threads (Project Loom), records |
| Spring Boot | 3.5 | Industry standard for REST APIs |
| Spring Data JPA + Hibernate | – | ORM + dynamic queries via Specifications |
| PostgreSQL | 15 | Production-grade relational database |
| Flyway | – | Versioned, repeatable DB schema migrations |
| MapStruct | 1.6 | Compile-time DTO ↔ Entity mapping (no reflection overhead) |
| Springdoc OpenAPI | 2.x | Auto-generated, interactive Swagger UI |
| H2 | – | In-memory DB for local dev and tests — no external dependency needed |
| Docker + Docker Compose | – | Containerised local development |
| Lombok | – | Reduces boilerplate |

---

## Getting Started

The default profile uses **H2 in-memory database** — no Docker or external database required for local development.

### Prerequisites
- Java 21+
- Maven 3.8+

### Option 1 — Docker Compose (recommended)

```bash
git clone  https://github.com/Paolayo/recipe-manager.git
cd recipe-manager

# Create your local credentials file from the template (gitignored — never committed)
cp .env.example .env
# Edit .env and set your own values for POSTGRES_PASSWORD, DB_PASSWORD, etc.

docker-compose up --build
```

The API will be available at `http://localhost:8080`
Swagger UI: `http://localhost:8080/swagger-ui.html`

> **Active profile:** `docker-compose.yml` sets `SPRING_PROFILES_ACTIVE=prod`, so the application connects to PostgreSQL and runs Flyway migrations automatically. No additional flags are needed.

> **Credentials:** all sensitive values (`POSTGRES_USER`, `POSTGRES_PASSWORD`, `DB_USERNAME`, `DB_PASSWORD`) are read from the `.env` file at runtime. Never hardcode them in `docker-compose.yml` or commit the `.env` file. In CI/CD pipelines inject them as platform secrets (GitHub Actions secrets, GitLab CI variables, etc.) instead of using a `.env` file.

### Option 2 — Run locally with Maven

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

The API will be available at `http://localhost:8080`  
H2 console (inspect in-memory data): `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:recipedb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL`
- Username: `sa`
- Password: *(leave blank)*

---

## Running in Production

The `prod` profile switches to **PostgreSQL** with Flyway-managed schema migrations. The H2 console is disabled and log levels are reduced to `WARN`.

### Prerequisites
- Java 21+
- PostgreSQL 15+

### Option 1 — JAR with prod profile

```bash
# Build
mvn clean package -DskipTests

# Run
java -jar target/recipe-manager-1.0.0.jar \
  --spring.profiles.active=prod \
  --DB_URL=jdbc:postgresql://<host>:5432/recipedb \
  --DB_USERNAME=<user> \
  --DB_PASSWORD=<password>
```

### Option 2 — Docker Compose with prod profile

```bash
SPRING_PROFILES_ACTIVE=prod \
DB_URL=jdbc:postgresql://<host>:5432/recipedb \
DB_USERNAME=<user> \
DB_PASSWORD=<password> \
docker-compose up --build
```

### Option 3 — Maven with prod profile

```bash
# Start PostgreSQL first
docker run -d \
  --name recipe-db \
  -e POSTGRES_DB=recipedb \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:15-alpine

# Run with prod profile
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Environment variables

| Variable | Default | Description |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/recipedb` | JDBC connection URL |
| `DB_USERNAME` | `postgres` | Database username |
| `DB_PASSWORD` | `postgres` | Database password |
| `SPRING_PROFILES_ACTIVE` | *(none — uses H2)* | Set to `prod` to activate PostgreSQL |

### What changes between profiles

| | Default (dev) | `prod` |
|---|---|---|
| Database | H2 in-memory | PostgreSQL |
| Schema management | Hibernate `create-drop` | Flyway migrations |
| H2 console | Enabled | Disabled |
| Log level | INFO / DEBUG | WARN |

---

## API Documentation

Interactive docs available at **`http://localhost:8080/swagger-ui.html`**  
OpenAPI spec (JSON): **`http://localhost:8080/api-docs`**

### Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/recipes` | Create a recipe |
| `GET` | `/api/v1/recipes` | Get all recipes |
| `GET` | `/api/v1/recipes/{id}` | Get recipe by ID |
| `PUT` | `/api/v1/recipes/{id}` | Update a recipe |
| `DELETE` | `/api/v1/recipes/{id}` | Delete a recipe |
| `GET` | `/api/v1/recipes/search` | Filter/search recipes |

### Search & filter examples

```bash
# All vegetarian recipes
GET /api/v1/recipes/search?vegetarian=true

# Recipes for 4 persons that include potatoes
GET /api/v1/recipes/search?servings=4&includeIngredients=potatoes

# Recipes without salmon that mention "oven" in the instructions
GET /api/v1/recipes/search?excludeIngredients=salmon&instructionSearch=oven

# Combine any filters freely
GET /api/v1/recipes/search?vegetarian=true&servings=2&includeIngredients=pasta
```

### Sample request body

```json
POST /api/v1/recipes
{
  "name": "Potato Gratin",
  "vegetarian": true,
  "servings": 4,
  "ingredients": ["potatoes", "cream", "gruyere", "garlic"],
  "instructions": "Preheat oven to 180°C. Layer sliced potatoes with cream and cheese. Bake for 45 minutes."
}
```

### Error format (RFC 9457)

```json
{
  "title": "Recipe Not Found",
  "status": 404,
  "detail": "Recipe not found with id: 99"
}
```

```json
{
  "title": "Validation Error",
  "status": 400,
  "detail": "Validation failed for one or more fields",
  "errors": {
    "name": "Recipe name is required",
    "servings": "Servings must be at least 1"
  }
}
```

---

## Running Tests

Tests use H2 in-memory database — no Docker or external database required.

```bash
# All tests
./mvnw test

# Unit tests only
./mvnw test -Dtest="RecipeServiceImplTest,RecipeControllerTest,RecipeSpecificationTest"

# Integration tests only
./mvnw test -Dtest="RecipeIntegrationTest"
```

### Test coverage

| Test class | Type | What it covers |
|---|---|---|
| `RecipeServiceImplTest` | Unit | Business logic, exception paths |
| `RecipeControllerTest` | Unit (MockMvc) | HTTP layer, status codes, JSON structure |
| `RecipeSpecificationTest` | Unit (DataJpaTest) | Dynamic filter combinations |
| `RecipeIntegrationTest` | Integration (SpringBootTest) | Full HTTP → DB roundtrip, all filter scenarios |

---

## Design Decisions

### Layered architecture
A classic Controller → Service → Repository structure was chosen for its readability and alignment with Spring Boot conventions. The business logic is cleanly separated from HTTP concerns and data access, making each layer easy to test in isolation.

### JPA Specification pattern for filtering
Rather than writing a repository method for every possible filter combination (which grows exponentially), the `Specification` pattern allows composing dynamic `WHERE` clauses at runtime from any mix of criteria. This keeps the repository interface lean and the filtering logic centralised and extensible.

### Profile-based database strategy
H2 in-memory is used for local development and tests (default profile) — no Docker or external database required. PostgreSQL is activated via the `prod` profile for production deployments. Tests run against a separate H2 instance in PostgreSQL-compatibility mode, keeping CI fast and self-contained.

### Flyway for schema management
Database schema changes are version-controlled alongside the application code. Flyway ensures the schema is always in the expected state across all environments — local, staging, production — without manual intervention.

### MapStruct for DTO mapping
MapStruct generates mapping code at compile time, avoiding the overhead and runtime errors associated with reflection-based mappers. DTOs decouple the API contract from the internal domain model, allowing both to evolve independently.

### RFC 9457 Problem Details for errors
All error responses follow the HTTP Problem Details standard (RFC 9457, which supersedes RFC 7807). This gives API consumers consistent, machine-readable error information regardless of what went wrong.

### Ingredient as a first-class entity
Ingredients were initially modelled as an `@ElementCollection(List<String>)` on `Recipe`. This was replaced with a dedicated `Ingredient` entity and a `@OneToMany` relationship for two reasons: Hibernate 6 no longer allows joining basic element collections in Criteria API subqueries, and a proper entity enables join-based filtering in `RecipeSpecification` without workarounds. The collection is fetched lazily and cascades all operations from `Recipe`.

### Instruction search performance — pg_trgm
The `instructionSearch` filter uses `LIKE '%text%'` on the `instructions` column. Normally a leading `%` wildcard prevents B-tree indexes from being used, causing a full-table scan. This is addressed in `V4__add_fts_index.sql` by enabling the `pg_trgm` extension and creating a GIN trigram index on `instructions`. PostgreSQL automatically uses this index for `LIKE`/`ILIKE` patterns, making substring searches efficient at scale — with no changes to the Specification query logic. The H2 test database is unaffected because Flyway is disabled in the test profile.

### `@Transactional(readOnly = true)` by default
The service class defaults to read-only transactions, with write methods explicitly annotated with `@Transactional`. This allows Hibernate and the database driver to apply read-path optimisations (no dirty checking, potential read replicas) without any extra effort.
