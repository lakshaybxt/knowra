# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 1. Common Development Commands

### Build and Run
- `./mvnw clean package` - Build the application
- `./mvnw clean install` - Build and install into local repository
- `./mvnw spring-boot:run` - Run the application in development mode
- `mvnw compile` - Just compile the project

### Testing
- `./mvnw test` - Run all unit tests
- `./mvnw -Dtest=KnowraApplicationTests` - Run the main application tests
- `./mvnw -Dspring-boot.run.arguments=--spring.profiles.active=test` - Run with test profile

### Database
- `./mvnw flyway:migrate` - Run Flyway database migrations
- `./mvnw flyway:repair` - Repair the Flyway schema history
- `./mvnw flyway:info` - Show Flyway migration info

### Docker
- `docker-compose up -d` - Start the PostgreSQL database with pgvector
- `docker-compose down` - Stop all services

### Code Quality
- `./mvnw spotless:apply` - Apply code formatting (if configured)
- `./mvnw checkstyle:check` - Check code style

## 2. Project Architecture

### Overall Structure

The knowra project is a Retrieval-Augmented Generation (RAG) backend built with Java 21 and Spring Boot 4.1.1. It allows users to upload documents and ask questions about them through AI-powered conversational interfaces.

### Key Components

**Core Backend Structure (`io.github.lakshaybxt.knowra` package):**
- **Controller Layer** (`controller/`): REST API endpoints
  - `DocumentController`: Document upload and processing
  - `ChatController`: Conversational Q&A with RAG

- **Service Layer** (`service/`): Business logic
  - `DocumentMetadataService`: Interface for document operations
  - `DocumentIngestionService`: Vector document ingestion
  - `DocumentParserService`: Document parsing

- **Service Implementations** (`service/impl/`):
  - `DocumentMetadataServiceImpl`: Main service implementation
  - `DocumentIngestionServiceImpl`: Vector embedding and storage
  - `DocumentParserServiceImpl`: Document format handling

- **Data Layer** (`repository/`, `entity/`):
  - `DocumentMetadataRepo`: Spring Data JPA repository
  - `DocumentMetadata`: Document metadata entity
  - `DocumentStatus`: Enum for document processing states

- **DTO Layer** (`dto/`): Data transfer objects
  - `DocumentResponseDto`: Document upload response
  - `ApiEnvelope`: Generic response wrapper
  - `ApiErrorEnvelope`: Error response wrapper

- **Exception Handling** (`exception/`):
  - `ResourceNotFoundException`: For missing resources
  - `DocumentProcessingException`: For document processing errors
  - `GlobalExceptionHandler`: Centralized error handling

- **Configuration** (`configuration/`):
  - `AppConfigProperties`: Application configuration
  - `OpenApiConfig`: OpenAPI/Swagger configuration

### Technology Stack

**Dependencies (pom.xml highlights):**
- Spring Boot 4.1.1 with Spring AI integration
- PostgreSQL with pgvector extension for vector storage
- Spring Data JPA for database access
- ModelMapper for DTO mapping
- SpringDoc for OpenAPI documentation
- Flyway for database migrations

**Key Configuration (`src/main/resources/application.yaml`):**
- PostgreSQL connection with environment variables
- OpenAI integration for embeddings and chat
- RAG-specific properties (chunking, similarity search)
- Multipart file upload limits

**Database Schema:**
- `vector_store` table (created by Flyway V1 migration)
- `document_metadata` table (planned but not yet implemented)

### Architectural Notes

**Current Status:**
- ✅ Core Spring Boot application structure
- ✅ Document upload and processing infrastructure
- ✅ Vector storage with PostgreSQL/pgvector
- ❌ Security configuration (Spring Security + JWT)
- ❌ User management and authentication
- ❌ Chat service implementation
- ❌ Persistent chat memory
- ❌ Complete OpenAI integration (config present, beans not written)

**Security Considerations:**
- Spring Security is already in the classpath but not configured
- JWT authentication is planned but not implemented
- All endpoints will return 401 until SecurityConfig is added

**Migration Strategy:**
- Uses Flyway with `ddl-auto: validate`
- Schema changes require migration scripts (`V2__`, `V3__`, etc.)
- PostgreSQL with pgvector extension required for vector operations

### Development Workflow

1. **Database Setup:** Start with `docker-compose up -d`
2. **Run Migrations:** `./mvnw flyway:migrate`
3. **Implement Missing Components:**
   - Security configuration with JWT
   - Chat service for RAG operations
   - User management
   - Persistent chat memory

4. **Test Changes:** Always run `./mvnw test` to ensure no regressions

### Code Review Guidelines

Based on the project's Spring Boot best practices configuration:
- Use constructor injection for dependencies
- Keep controllers thin (HTTP only, no business logic)
- Map entities to DTOs to avoid data exposure
- Use `@Valid` on request bodies for validation
- Apply `@Transactional` on service methods, not controllers
- Log with parameterized messages
- Use proper exception handling with centralized error responses

### Common Pitfalls to Avoid

1. **Security starter locks everything:** With Spring Security in classpath, add permissive SecurityConfig early, then tighten it
2. **Schema validation:** Use Flyway migrations, never `ddl-auto=update` in production
3. **N+1 queries:** Make associations lazy, use JOIN FETCH or EntityGraph when needed
4. **Lazy loading in services:** Ensure transactions cover all entity access
5. **Secrets in code:** Use environment variables for configuration

### References

- Architecture documentation: `docs/ARCHITECTURE.md`
- Spring Boot best practices: `.claude/skills/spring-boot-best-practices/SKILL.md`
- Code review guidelines: `.claude/agents/spring-boot-reviewer.md`