# Testing Strategy

This document outlines the testing capabilities, execution commands, and recommended patterns for the MinuteMind backend.

---

## 1. Running Existing Tests

Currently, the project contains a standard context-loading sanity test [MinutemindApplicationTests.java](file:///d:/Working/Project/personal-project/minute-mind/be-minute-mind/minutemind/src/test/java/com/be/minutemind/MinutemindApplicationTests.java). This test ensures the Spring ApplicationContext is properly configured, dependencies can be autowired, and properties are correctly resolved.

To run the test suite, execute the following command from the project root:
```bash
./mvnw test
```

---

## 2. Test Configuration & Dependencies

The project's [pom.xml](file:///d:/Working/Project/personal-project/minute-mind/be-minute-mind/minutemind/pom.xml) has dedicated testing modules pre-installed:
- `spring-boot-starter-data-jpa-test`: Slice-testing utility for repositories.
- `spring-boot-starter-security-test`: Helper library for mocking authentication contexts and security rules.
- `spring-boot-starter-validation-test`: Asserts validation constraints.
- `spring-boot-starter-webmvc-test`: MockMvc helper to test controller routes without firing up the full HTTP server.

---

## 3. Recommended Testing Practices

As the project grows, contributors should adhere to the following testing patterns:

### Unit Testing Services (Mocking)
- Use JUnit 5 and Mockito.
- Mock all repository and helper dependencies.
- Avoid booting up the Spring Context (`@SpringBootTest`) for pure business logic tests to keep tests fast.
```java
@ExtendWith(MockitoExtension.class)
class GoalServiceImplTest {
    @Mock
    private GoalRepository goalRepository;

    @InjectMocks
    private GoalServiceImpl goalService;

    @Test
    void testCreateGoal() {
        // Given...
        // When...
        // Then...
    }
}
```

### Controller Testing (WebMvc slice tests)
- Use `@WebMvcTest` to restrict context initialization to the controller layer.
- Use `MockMvc` to trigger mock requests and assert HTTP statuses, JSON payloads, and validation constraints.
- Use `@MockBean` to inject mock service implementations.
```java
@WebMvcTest(GoalController.class)
class GoalControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GoalService goalService;

    // Test auth rules and REST output...
}
```

### Database & Redis Integration Testing
- Because the repository queries use PostgreSQL-specific dialects and native SQL queries (such as interval math for streaks and daily ranking aggregations), we recommend using **Testcontainers** to spin up actual lightweight PostgreSQL and Redis Docker instances during the integration test lifecycle.
