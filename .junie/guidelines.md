# Spring Boot Guidelines

## 1. Prefer Constructor Injection over Field/Setter Injection
* Declare all the mandatory dependencies as `final` fields and inject them through the constructor.
* Spring will auto-detect if there is only one constructor, no need to add `@Autowired` on the constructor.
* Avoid field/setter injection in production code.

**Explanation:**

* Making all the required dependencies as `final` fields and injecting them through constructor make sure that the object is always in a properly initialized state using the plain Java language feature itself. No need to rely on any framework-specific initialization mechanism.
* You can write unit tests without relying on reflection-based initialization or mocking.
* The constructor-based injection clearly communicates what are the dependencies of a class without having to look into the source code.
* Spring Boot provides extension points as builders such as `RestClient.Builder`, `ChatClient.Builder`, etc. Using constructor-injection, we can do the customization and initialize the actual dependency.

```java
@Service
public class OrderService {
   private final OrderRepository orderRepository;
   private final RestClient restClient;

   public OrderService(OrderRepository orderRepository, 
                       RestClient.Builder builder) {
       this.orderRepository = orderRepository;
       this.restClient = builder
               .baseUrl("http://catalog-service.com")
               .requestInterceptor(new ClientCredentialTokenInterceptor())
               .build();
   }

   //... methods
}
```

## 2. Prefer package-private over public for Spring components
* Declare Controllers, their request-handling methods, `@Configuration` classes and `@Bean` methods with default (package-private) visibility whenever possible. There's no obligation to make everything `public`.

**Explanation:**

* Keeping classes and methods package-private reinforces encapsulation and abstraction by hiding implementation details from the rest of your application.
* Spring Boot's classpath scanning will still detect and invoke package-private components (for example, invoking your `@Bean` methods or controller handlers), so you can safely restrict visibility to only what clients truly need. This approach confines your internal APIs to a single package while still allowing the framework to wire up beans and handle HTTP requests.

## 3. Organize Configuration with Typed Properties
* Group application-specific configuration properties with a common prefix in `application.properties` or `.yml`.
* Bind them to `@ConfigurationProperties` classes with validation annotations so that the application will fail fast if the configuration is invalid.
* Prefer environment variables instead of profiles for passing different configuration properties for different environments.

**Explanation:**

* By grouping and binding configuration in a single `@ConfigurationProperties` bean, you centralize both the property names and their validation rules.
  In contrast, using `@Value("${…}")` across many components forces you to update each injection point whenever a key or validation requirement changes.
* Overusing profiles to customize the application configuration may lead to unexpected issues due to the order of profiles specified.
  As you can enable multiple profiles with different combinations, making sense of the effective application configuration becomes tricky.

## 4. Define Clear Transaction Boundaries
* Define each Service-layer method as a transactional unit.
* Annotate query-only methods with `@Transactional(readOnly = true)`.
* Annotate data-modifying methods with `@Transactional`.
* Limit the code inside each transaction to the smallest necessary scope.

**Explanation:**

* **Single Unit of Work:** Group all database operations for a given use case into one atomic unit, which in Spring Boot is typically a `@Service` annotated class method. This ensures that either all operations succeed or none do.
* **Connection Reuse:** A `@Transactional` method runs on a single database connection for its entire scope, avoiding the overhead of acquiring and returning connections from the connection pool for each operation.
* **Read-only Optimizations:** Marking methods as `readOnly = true` disables unnecessary dirty-checking and flushes, improving performance for pure reads.
* **Reduced Contention:** Keeping transactions as brief as possible minimizes lock duration, lowering the chance of contention in high-traffic applications.

## 5. Disable Open Session in View Pattern
* While using Spring Data JPA, disable the Open Session in View filter by setting ` spring.jpa.open-in-view=false` in `application.properties/yml.`

**Explanation:**

* Open Session In View (OSIV) filter transparently enables loading the lazy associations while rendering the view or serializing JPA entities. This may lead to the N + 1 Select problem.
* Disabling OSIV forces you to fetch exactly the associations you need via fetch joins, entity graphs, or explicit queries, and hence you can avoid unexpected N + 1 selects and `LazyInitializationExceptions`.

## 6. Separate Web Layer from Persistence Layer
* Don't expose entities directly as responses in controllers.
* Define explicit request and response record (DTO) classes instead.
* Apply Jakarta Validation annotations on your request records to enforce input rules.

**Explanation:**

* Returning or binding directly to entities couples your public API to your database schema, making future changes riskier.
* DTOs let you clearly declare exactly which fields clients can send or receive, improving clarity and security.
* With dedicated DTOs per use case, you can annotate fields for validation without relying on complex validation groups.
* Use Java bean mapper libraries to simplify DTO conversions. Prefer MapStruct library that can generate bean mapper implementation at compile time so that there won't be runtime reflection overhead.

## 7. Follow REST API Design Principles
* **Versioned, resource-oriented URLs:** Structure your endpoints as `/api/v{version}/resources` (e.g. `/api/v1/orders`).
* **Consistent patterns for collections and sub-resources:** Keep URL conventions uniform (for example, `/posts` for posts collection and `/posts/{slug}/comments` for comments of a specific post).
* **Explicit HTTP status codes via ResponseEntity:** Use `ResponseEntity<T>` to return the correct status (e.g. 200 OK, 201 Created, 404 Not Found) along with the response body.
* Use pagination for collection resources that may contain an unbounded number of items.
* The JSON payload must use a JSON object as a top-level data structure to allow for future extension.
* Use snake_case or camelCase for JSON property names consistently.

**Explanation:**

* **Predictability and discoverability:** Adhering to well-known REST conventions makes your API intuitive. Clients can guess URLs and behaviors without extensive documentation.
* **Reliable client integrations:** Standardized URL structures, status codes, and headers enable consumers to build against your API with confidence, knowing exactly what each response will look like.
* For more comprehensive REST API Guidelines, please refer [Zalando RESTful API and Event Guidelines](https://opensource.zalando.com/restful-api-guidelines/).

## 8. Use Command Objects for Business Operations
* Create purpose-built command records (e.g., `CreateOrderCommand`) to wrap input data.
* Accept these commands in your service methods to drive creation or update workflows.

**Explanation:**

* Using the use-case specific Command and Query objects clearly communicates what input data is expected from the caller.
  Otherwise, the caller had to guess whether they should create and pass the unique key or created_date, or they will be generated by the server/database.

## 9. Centralize Exception Handling
* Define a global handler class annotated with `@ControllerAdvice` (or `@RestControllerAdvice` for REST APIs) using `@ExceptionHandler` methods to handle specific exceptions.
* Return consistent error responses. Consider using the ProblemDetails response format ([RFC 9457](https://www.rfc-editor.org/rfc/rfc9457)).

**Explanation:**

* We should always handle all possible exceptions and return a standard error response instead of throwing exceptions.
* It is better to centralize the exception handling in a `GlobalExceptionHandler` using `(Rest)ControllerAdvice` instead of duplicating the try/catch exception handling logic across the controllers.

## 10. Actuator
* Expose only essential actuator endpoints (such as `/health`, `/info`, `/metrics`) without requiring authentication. All the other actuator endpoints must be secured.

**Explanation:**

* Endpoints like `/actuator/health` and `/actuator/metrics` are critical for external health checks and metric collection (e.g., by Prometheus). Allowing these to be accessed anonymously ensures monitoring tools can function without extra credentials. All the remaining endpoints should be secured.
* In non-production environments (DEV, QA), you can expose additional actuator endpoints such as `/actuator/beans`, `/actuator/loggers` for debugging purpose.

## 11. Internationalization with ResourceBundles
* Externalize all user-facing text such as labels, prompts, and messages into ResourceBundles rather than embedding them in code.

**Explanation:**

* Hardcoded strings make it difficult to support multiple languages. By placing your labels, error messages, and other text in locale-specific ResourceBundle files, you can maintain separate translations for each language.
* At runtime, Spring can load the appropriate bundle based on the user's locale or a preference setting, making it simple to add new languages and switch between them dynamically.

## 12. Use Testcontainers for integration tests
* Spin up real services (databases, message brokers, etc.) in your integration tests to mirror production environments.

**Explanation:**

* Most of the modern applications use a wide range of technologies such as SQL/NoSQL databases, key-value stores, message brokers, etc. Instead of using in-memory variants or mocks, Testcontainers can spin up those dependencies as Docker containers and allow you to test using the same type of dependencies that you will use in the production. This reduces environment inconsistencies and increases confidence in your integration tests.
* Always use docker images with a specific version of the dependency that you are using in production instead of using the `latest` tag.

## 13. Use random port for integration tests
* When writing integration tests, start the application on a random available port to avoid port conflicts by annotating the test class with:

    ```java
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    ```

**Explanation:**

* **Avoid conflicts in CI/CD:** In your CI/CD environment, there can be multiple builds running in parallel on the same server/agent. In such cases, it is better to run the integration tests using a random available port rather than a fixed port to avoid port conflicts.

## 14. Logging
* **Use a proper logging framework.**  
  Never use `System.out.println()` for application logging. Rely on SLF4J (or a compatible abstraction) and your chosen backend (Logback, Log4j2, etc.).

* **Protect sensitive data.**  
  Ensure that no credentials, personal information, or other confidential details ever appear in log output.

* **Guard expensive log calls.**  
  When building verbose messages at `DEBUG` or `TRACE` level, especially those involving method calls or complex string concatenations, wrap them in a level check or use suppliers:

```java
if (logger.isDebugEnabled()) {
    logger.debug("Detailed state: {}", computeExpensiveDetails());
}

// using Supplier/Lambda expression
logger.atDebug()
	.setMessage("Detailed state: {}")
	.addArgument(() -> computeExpensiveDetails())
    .log();
```

**Explanation:**

* **Flexible verbosity control:** A logging framework lets you adjust what gets logged and where with the support for tuning log levels per environment (development, testing, production).

* **Rich contextual metadata:** Beyond the message itself, you can capture class/method names, thread IDs, process IDs, and any custom context via MDC, aiding diagnosis.

* **Multiple outputs and formats:** Direct logs to consoles, rolling files, databases, or remote systems, and choose formats like JSON for seamless ingestion into ELK, Loki, or other log-analysis tools.

* **Better tooling and analysis:** Structured logs and controlled log levels make it easier to filter noise, automate alerts, and visualize application behavior in real time.

## 15. Database Migrations with Flyway
* Use Flyway for database schema migrations to ensure consistent database evolution across all environments.
* Place migration scripts in the default location: `src/main/resources/db/migration`.
* Follow the Flyway version naming convention: `V<version>__<description>.sql` (e.g., `V1__create_tables.sql`, `V2__add_indexes.sql`).
* Keep migrations immutable once they've been applied to any environment.
* Use H2 compliant SQL syntax for database migrations.
* When altering tables to add a property with a foreign key constraint, add the new column first and then add the foreign 
  key constraint in a second SQL statement.

**Explanation:**

* **Consistent Schema Evolution:** Flyway ensures that database schema changes are applied consistently across all environments (development, testing, production) in the correct order.
* **Version Control for Database:** Migration scripts in the standard location are automatically detected and executed in version order when the application starts, providing version control for your database schema.
* **Repeatable Migrations:** For migrations that can be re-run (like view or stored procedure definitions), use the `R__` prefix instead of `V__` (e.g., `R__create_views.sql`).
* **Spring Boot Integration:** Spring Boot automatically configures Flyway when it's added as a dependency, detecting migration scripts in the default location without additional configuration.

## 16. OpenAPI Specification Guidelines
* Use a modular approach to organize your OpenAPI specification by splitting it into multiple files.
* Store the main OpenAPI definition in `openapi/openapi/openapi.yaml` which references other files for paths and components.
* Follow a consistent file naming convention that reflects the API path structure.
* Define reusable components in separate files under appropriate directories.

**API Documentation Structure:**

* **Main File:** The root OpenAPI specification file (`openapi/openapi/openapi.yaml`) contains the API metadata, servers, security schemes, and references to path and component files.
* **Path Files:** Individual API endpoints are defined in separate files under the `openapi/openapi/paths/` directory.
* **Component Files:** Reusable schemas, responses, parameters, etc. are defined in separate files under the `openapi/openapi/components/` directory with appropriate subdirectories (e.g., `schemas/`, `responses/`).

**File Naming Conventions:**

* **Path Files:** Name path files according to the API endpoint they represent, replacing special characters with underscores:
  * `/users/{username}` → `users_{username}.yaml`
  * `/user` → `user.yaml`
  * `/user/list` → `user-status.yaml`
* **Component Files:** Name component files according to the entity they represent:
  * User schema → `User.yaml`
  * Email schema → `Email.yaml`

**Component Definition:**

* Use `$ref` to reference components from other files:
  * From path files to schemas: `$ref: '../components/schemas/User.yaml'`
  * Between component files: `$ref: './Email.yaml'`
* Group related components in appropriate subdirectories (e.g., `schemas/`, `responses/`, `parameters/`).
* Use relative paths for references to maintain portability.

**Testing the OpenAPI Specification:**

* Run `npm test` in the `openapi` directory to validate the OpenAPI specification.
* This command executes `redocly lint` which checks for:
  * Syntax errors
  * Semantic errors
  * Adherence to OpenAPI standards
  * Broken references
* Fix any reported issues before committing changes to the API specification.
* You can also use `npm start` to preview the documentation and `npm run build` to bundle the specification into a single file.

## 17. Use Project Lombok
* Use Lombok to reduce boilerplate code.
* Enable annotation processing for your IDE to generate boilerplate code for you.
* When adding builder to a class, if the class extends another class, add `@SuperBuilder` for the builder.

## 18. Use Mapstruct for Type Conversions
* Use Mapstruct to convert between domain objects and DTOs.
* Use `@Mapper` to configure the mapping between the two classes.
* Use `@Mapping` to configure the mapping between the two fields.
* After modifying a Mapper, recompile the project to generate the new Mapper implementation.
* Use Mappers to update existing entities. 

## 19. Service Operations
* When updating existing entities, use Mappers to update existing entities. The entity should be fetched from the database 
  and then updated using the mapper prior to saving the entity back to the database.

## 20. Frontend Project Structure and Organization

* Organize the React frontend in `src/main/frontend` directory within the Spring Boot project structure.
* Use a feature-based directory structure with clear separation of concerns:
  - `src/components/` - Reusable UI components organized by category (ui, forms, tables, dialogs, etc.)
  - `src/pages/` - Page-level components representing different routes
  - `src/services/` - API service layer for backend communication
  - `src/hooks/` - Custom React hooks for shared logic
  - `src/utils/` - Utility functions and helpers
  - `src/types/` - TypeScript type definitions
  - `src/layouts/` - Layout components for different page structures

**Explanation:**

* **Clear separation of concerns:** Organizing code by feature and responsibility makes it easier to locate, maintain, and test specific functionality.
* **Scalability:** This structure supports growth as the application expands, allowing teams to work on different features without conflicts.
* **Reusability:** Separating reusable components from page-specific logic promotes code reuse and consistency across the application.

## 21. Frontend Build Integration with Maven

* Use the `frontend-maven-plugin` to integrate the React build process with the Maven lifecycle.
* Configure Node.js and npm versions explicitly in the `pom.xml` properties section.
* Set up Maven executions to install Node/npm, install dependencies, and build the frontend during the appropriate Maven phases.
* Configure the build output directory to `target/classes/static` so Spring Boot can serve the frontend assets.

**Maven Configuration Example:**

```xml
<properties>
    <node.version>v25.4.0</node.version>
    <npm.version>11.7.0</npm.version>
    <frontend-maven-plugin.version>2.0.0</frontend-maven-plugin.version>
</properties>

<plugin>
    <groupId>com.github.eirslett</groupId>
    <artifactId>frontend-maven-plugin</artifactId>
    <version>${frontend-maven-plugin.version}</version>
    <configuration>
        <workingDirectory>src/main/frontend</workingDirectory>
        <installDirectory>target</installDirectory>
    </configuration>
    <executions>
        <execution>
            <id>install-node-npm</id>
            <goals><goal>install-node-and-npm</goal></goals>
            <phase>generate-resources</phase>
        </execution>
        <execution>
            <id>npm-install</id>
            <goals><goal>npm</goal></goals>
            <phase>generate-resources</phase>
            <configuration><arguments>install</arguments></configuration>
        </execution>
        <execution>
            <id>npm-build</id>
            <goals><goal>npm</goal></goals>
            <phase>prepare-package</phase>
            <configuration><arguments>run build</arguments></configuration>
        </execution>
    </executions>
</plugin>
```

**Explanation:**

* **Unified build process:** Integrating frontend builds with Maven ensures that `mvn clean install` builds both backend and frontend, simplifying CI/CD pipelines.
* **Consistent environments:** Pinning Node.js and npm versions ensures consistent builds across different development machines and environments.
* **Automatic asset serving:** Building to `target/classes/static` allows Spring Boot to automatically serve frontend assets without additional configuration.

## 22. React Component Architecture with Shadcn/UI

* Use Shadcn/UI components built on Radix UI primitives for consistent, accessible UI components.
* Organize UI components in a dedicated `components/ui/` directory with proper exports through an index file.
* Implement component composition patterns using Radix UI's slot-based architecture.
* Use Tailwind CSS with CSS custom properties for theming and consistent design tokens.

**Component Structure Example:**

```typescript
// components/ui/button.tsx
import { Slot } from "@radix-ui/react-slot"
import { cva, type VariantProps } from "class-variance-authority"
import { cn } from "@/lib/utils"

const buttonVariants = cva(
  "inline-flex items-center justify-center rounded-md text-sm font-medium",
  {
    variants: {
      variant: {
        default: "bg-primary text-primary-foreground hover:bg-primary/90",
        destructive: "bg-destructive text-destructive-foreground hover:bg-destructive/90",
      },
      size: {
        default: "h-10 px-4 py-2",
        sm: "h-9 rounded-md px-3",
        lg: "h-11 rounded-md px-8",
      },
    },
    defaultVariants: {
      variant: "default",
      size: "default",
    },
  }
)

export interface ButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement>,
    VariantProps<typeof buttonVariants> {
  asChild?: boolean
}

const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant, size, asChild = false, ...props }, ref) => {
    const Comp = asChild ? Slot : "button"
    return (
      <Comp
        className={cn(buttonVariants({ variant, size, className }))}
        ref={ref}
        {...props}
      />
    )
  }
)
```

**Explanation:**

* **Accessibility by default:** Radix UI primitives provide keyboard navigation, focus management, and ARIA attributes out of the box.
* **Consistent design system:** Using class-variance-authority with Tailwind creates a systematic approach to component variants and styling.
* **Composition over inheritance:** The slot pattern allows flexible component composition while maintaining type safety.

## 23. API Integration and Service Layer Architecture

* Create a centralized API service using Axios with interceptors for authentication, logging, and error handling.
* Implement service modules for each domain (beers, customers, orders) with consistent patterns.
* Use TypeScript interfaces generated from OpenAPI specifications for type safety.
* Implement proper error handling with user-friendly error messages and logging.

**API Service Pattern:**

```typescript
// services/api.ts
class ApiService {
  private api: AxiosInstance;

  constructor() {
    this.api = axios.create({
      baseURL: env.API_URL || '/api',
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json',
      },
      timeout: 30000,
    });
    this.setupInterceptors();
  }

  private setupInterceptors(): void {
    // Request interceptor for authentication
    this.api.interceptors.request.use((config) => {
      const token = localStorage.getItem('auth_token');
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    });

    // Response interceptor for error handling
    this.api.interceptors.response.use(
      (response) => response,
      (error) => {
        handleApiError(error);
        return Promise.reject(error);
      }
    );
  }
}
```

**Explanation:**

* **Centralized configuration:** A single API service ensures consistent configuration, authentication, and error handling across all API calls.
* **Type safety:** Using generated TypeScript types from OpenAPI specifications prevents runtime errors and improves developer experience.
* **Separation of concerns:** Domain-specific service modules keep API logic organized and maintainable.

## 24. Frontend Testing Strategy

* Use Jest with React Testing Library for unit and integration testing.
* Configure Jest with jsdom environment for DOM testing.
* Write tests for components, hooks, and service functions.
* Use identity-obj-proxy for CSS module mocking in tests.

**Jest Configuration:**

```javascript
// jest.config.js
export default {
  testEnvironment: 'jsdom',
  setupFilesAfterEnv: ['<rootDir>/src/test/setupTests.ts'],
  moduleNameMapping: {
    '^@/(.*)$': '<rootDir>/src/$1',
    '^@components/(.*)$': '<rootDir>/src/components/$1',
    '\\.(css|less|scss|sass)$': 'identity-obj-proxy',
  },
  transform: {
    '^.+\\.(ts|tsx)$': 'ts-jest',
  },
  collectCoverageFrom: [
    'src/**/*.{ts,tsx}',
    '!src/**/*.d.ts',
    '!src/main.tsx',
    '!src/vite-env.d.ts',
  ],
};
```

**Explanation:**

* **Component testing:** React Testing Library promotes testing components from the user's perspective, focusing on behavior rather than implementation details.
* **Type safety in tests:** Using ts-jest ensures TypeScript compilation and type checking in test files.
* **Comprehensive coverage:** Collecting coverage from all source files helps identify untested code paths.

## 25. Frontend Development Workflow and Code Quality

* Use ESLint with TypeScript and React-specific rules for code quality enforcement.
* Configure Prettier for consistent code formatting across the team.
* Set up Husky with lint-staged for pre-commit hooks to ensure code quality.
* Use environment-specific configuration files for different deployment targets.

**Code Quality Configuration:**

```javascript
// eslint.config.js
export default [
  {
    files: ['**/*.{ts,tsx}'],
    languageOptions: {
      parser: '@typescript-eslint/parser',
      parserOptions: {
        ecmaVersion: 'latest',
        sourceType: 'module',
        ecmaFeatures: { jsx: true },
      },
    },
    plugins: {
      '@typescript-eslint': typescriptEslint,
      'react-hooks': reactHooks,
      'react-refresh': reactRefresh,
    },
    rules: {
      'react-hooks/rules-of-hooks': 'error',
      'react-hooks/exhaustive-deps': 'warn',
      '@typescript-eslint/no-unused-vars': 'error',
    },
  },
];
```

**Explanation:**

* **Consistent code style:** Automated formatting and linting ensure consistent code style across the team, reducing review overhead.
* **Early error detection:** Pre-commit hooks catch issues before they reach the repository, maintaining code quality.
* **React-specific rules:** React Hooks rules prevent common React pitfalls and ensure proper hook usage.

## 26. Frontend Build Optimization and Performance

* Configure Vite with proper code splitting and chunk optimization for production builds.
* Use manual chunks to separate vendor libraries from application code.
* Implement proper asset optimization with appropriate file naming and caching strategies.
* Configure environment-specific build optimizations (source maps, minification, console removal).

**Vite Build Configuration:**

```typescript
// vite.config.ts
export default defineConfig({
  build: {
    outDir: '../../../target/classes/static',
    sourcemap: process.env.NODE_ENV !== 'production',
    minify: 'terser',
    rollupOptions: {
      output: {
        manualChunks: {
          vendor: ['react', 'react-dom', 'react-router-dom'],
          ui: ['@radix-ui/react-dialog', '@radix-ui/react-dropdown-menu'],
        },
        chunkFileNames: 'assets/js/[name]-[hash].js',
        entryFileNames: 'assets/js/[name]-[hash].js',
        assetFileNames: 'assets/[ext]/[name]-[hash].[ext]',
      },
    },
  },
});
```

**Explanation:**

* **Optimized loading:** Code splitting and manual chunks reduce initial bundle size and improve loading performance.
* **Caching strategy:** Hash-based file names enable long-term caching while ensuring cache invalidation when files change.
* **Production optimization:** Environment-specific configurations ensure optimal builds for different deployment scenarios.

## 27. Frontend Environment Configuration and Deployment

* Use environment-specific configuration files (`.env`, `.env.development`, `.env.production`) for different deployment targets.
* Configure proxy settings in development to forward API requests to the Spring Boot backend.
* Set up proper path aliases in both Vite and TypeScript configurations for clean imports.
* Ensure frontend assets are properly served by Spring Boot in production.

**Environment Configuration:**

```bash
# .env.development
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_LOG_LEVEL=debug

# .env.production  
VITE_API_BASE_URL=/api/v1
VITE_LOG_LEVEL=error
```

**Explanation:**

* **Environment flexibility:** Different configurations for development, testing, and production environments ensure proper behavior in each context.
* **Development efficiency:** Proxy configuration eliminates CORS issues during development while maintaining production-like API interactions.
* **Clean imports:** Path aliases improve code readability and make refactoring easier by avoiding relative path complexity.

These additional guidelines provide comprehensive coverage of the frontend implementation patterns, build processes, and best practices that have been implemented in this project. They complement the existing Spring Boot guidelines and provide a complete development framework for full-stack Spring Boot applications with React frontends.