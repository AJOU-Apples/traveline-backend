You are pair-programming on Traveline’s Spring Boot backend. Your goal is to implement features safely and quickly, following the project’s conventions, API contracts, and the product user stories. Default to minimal, composable edits and keep the system stable.

Context

Stack: Java 17, Spring Boot 3.5, Spring Web, Spring Validation, Spring Security (JWT), Spring Data JPA (PostgreSQL), WebClient (WebFlux), Lombok, Thumbnailator, metadata-extractor, JUnit 5 + H2 for tests.

Entry: org.apples.travelinebackend.TravelineBackendApplication. Package root: org.apples.travelinebackend.

Architecture (src/main/java/org/apples/travelinebackend/)
- controller/ — Thin REST controllers (request mapping, validation, response codes)
- service/ — Business logic (transactional boundaries, authorization checks)
- repository/ — Spring Data JPA repositories (JPQL/derived query methods)
- entity/ — JPA entities and relationships
- dto/ — Request/response DTOs only; no entities over the wire
- mapper/ — Deterministic, rule-free mapping between entity and DTO
- security/ — JWT, filters, config
- config/ — File storage, WebClient, CORS, etc.
- exception/ — Domain exceptions + GlobalExceptionHandler → ErrorResponse

Product/User Story Alignment (traveline-userstory.md)
- Always read and align with the user stories in traveline-userstory.md (repo root unless otherwise noted). Treat it as the authoritative source for:
  - Scope, acceptance criteria, and business rules
  - Error semantics and visibility/permission rules
  - Copy/UX intent for messages (prefer Korean strings as written)
  - Non-functional requirements called out in stories
- Traceability:
  - Reference the story ID/title in commit messages and TODOs (e.g., feat(expenses): split settlement per US-12)
  - If code and story conflict, implement per the story and leave a short code comment citing the story and rationale
- Planning from stories:
  - Derive smallest vertical slices verifiable against acceptance criteria
  - Add/adjust DTO fields and bean validations directly from the story
  - Add repository filters (e.g., SHARED vs personal visibility) and service-level authorization per the story
  - Ensure response codes/body match the story’s expectations
- If traveline-userstory.md is missing or unclear:
  - Don’t guess business behavior. Ask a concise question and implement only the safe, neutral server behavior or keep the change behind a feature flag if needed.

Absolute Must-Follow Conventions

Controllers
- Keep controllers thin; no repository access from controllers.
- Use @Valid on request DTOs. Return response DTOs; never entities.
- Use consistent status codes: 200/201/204 on success, 400 for validation/semantic issues, 403 for forbidden, 404 for not found/inaccessible when appropriate, 500 for unknown.

Services
- Enforce travel plan membership and role checks here.
- Mark read-only queries with @Transactional(readOnly = true); mutating methods with @Transactional.
- Derive additional business validations from the user story.

Repositories
- Prefer meaningful derived methods; use @Query with clear JPQL for complex/filtered queries.
- Keep visibility/ownership filters close to the query when practical (see ExpenseRepository patterns).
- Use Optional and handle not-found at service level with ResourceNotFoundException.

DTOs/Mappers
- Model DTOs to match acceptance criteria (fields, types, validation annotations).
- Keep mappers simple; no business rules inside mappers.

Security/Auth
- JWT-based auth; use security filters/config consistently.
- Apply authorization checks in service methods for plan-scoped resources.
- Follow existing 401/403 semantics; do not leak resource existence across permissions unless the story requires otherwise.

Files/Images
- Store originals under uploads/photos, thumbs under uploads/thumbnails using Thumbnailator.
- Expose files via stable URLs; never expose filesystem paths directly.

Errors and Global Handling
- Throw ResourceNotFoundException, BadRequestException, ForbiddenException as appropriate.
- GlobalExceptionHandler maps exceptions to ErrorResponse with timestamp, status, error, message, and optional validationErrors.
- Messages should align with traveline-userstory.md language and clarity.

API Contracts
- Preserve existing endpoint shapes unless the story mandates changes.
- Additive changes preferred; if breaking changes are needed, propose versioned endpoints or migration strategy.
- Keep pagination and sorting in mind for list endpoints; use Pageable where relevant.

How to Make Changes

Data flow change
- Add/edit service method(s) to implement the story’s rules; update repository queries if needed.
- Introduce/adjust DTOs and validations to match acceptance criteria.
- Update controller to expose the API contract; ensure status codes and body shapes align with the story.

New endpoint
- Add request/response DTOs, controller method, service method, repository methods/queries.
- Include validation, authorization, and visibility per story.
- Add tests: repository (if new queries), service (logic/permissions), controller (status/body/security).

External calls
- Use configured WebClient beans (config/WebClientConfig); no ad-hoc clients.
- Handle timeouts/retries per global config unless story requires otherwise.

Coding Style

- Java 17 features as appropriate; constructor injection; final where possible.
- Small, pure methods; early returns for guards.
- Lombok per existing usage (@Data/@Builder, etc.).
- Korean for user-facing error messages where applicable; comments Korean/English as present.

Platform and Environment

- Database: PostgreSQL in production; H2 in tests.
- Dev ports: Backend default 8080. Ensure CORS permits frontend dev origins (iOS http://localhost:8080 proxy usage; Android emulator http://10.0.2.2 considerations on the client are known—don’t hardcode client assumptions server-side).
- Secrets/keys: Never hardcode; use application.yml and environment variables.

Testing Scope for Each Edit

- Validation: DTO constraints trigger 400 with field-level messages.
- Auth: Unauthorized vs forbidden paths respected; no information leaks.
- Visibility: SHARED vs personal expense filters correct; members only access plan-scoped data.
- Persistence: Transactions correct; no orphaned relations; cascade rules verified.
- Performance: Avoid N+1 with fetch joins where needed; large lists paginated where appropriate.
- Files: Uploads create thumbnails; URLs resolve; headers/content types correct.

What Not to Do

- Don’t expose entities in controller responses.
- Don’t call repositories from controllers.
- Don’t bypass GlobalExceptionHandler with ad-hoc ResponseEntity unless required by the story.
- Don’t introduce new libraries/config patterns without strong reason and alignment with the story.

Useful Paths

- Entry: src/main/java/org/apples/travelinebackend/TravelineBackendApplication.java
- Errors: src/main/java/org/apples/travelinebackend/exception/GlobalExceptionHandler.java
- Expenses example filters: src/main/java/org/apples/travelinebackend/repository/ExpenseRepository.java
- Config: src/main/java/org/apples/travelinebackend/config/
- Security: src/main/java/org/apples/travelinebackend/security/

Commit Guidance

- Tie to stories: feat(expenses): implement shared/personal filter per US-12
- Separate logical changes (DTOs/mappers vs repository queries vs services vs controllers).
- Keep commits small and verifiable against acceptance criteria.

When in Doubt

- Mirror existing patterns for the same resource (look at ExpenseRepository/service/controller for visibility patterns).
- Resolve ambiguity in favor of traveline-userstory.md; otherwise, raise a question and keep the edit minimal and backward compatible.
