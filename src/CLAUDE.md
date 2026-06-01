# Coding Style Guide

## Language
- Java

## Paradigm
- Prefer functional style (streams, lambdas) where appropriate
- Use OOP principles (encapsulation, single responsibility)

## Naming Conventions
- Variables and methods: `camelCase`
- Classes: `PascalCase`
- Constants: `UPPER_SNAKE_CASE`
- Packages: `all.lowercase.nodashes` — e.g. `com.example.app.service.impl`

## Lombok
- Use `@Data` on DTO classes — generates getter, setter, equals, hashCode, toString
- Use `@Builder` on Entity classes — enables builder pattern, avoid `new Entity()` + setters
- Use `@RequiredArgsConstructor` on Service and Controller classes — injects `final` fields via constructor

### When to use each annotation

| Annotation | Use on | Generates |
|---|---|---|
| `@Data` | DTO, Entity | getter + setter + equals + hashCode + toString |
| `@Builder` | Entity | builder pattern (requires `@NoArgsConstructor` + `@AllArgsConstructor`) |
| `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` | Entity | safe equals/hashCode using only `@EqualsAndHashCode.Include` fields |
| `@RequiredArgsConstructor` | Service, Controller | constructor for all `final` fields |
| `@Slf4j` | Service, Controller | injects `log` field for logging |

### DTO example
```java
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRequest {
    @JsonProperty("user_name")
    private String username;

    @JsonProperty("pass_word")
    private String password;
}
```

### Entity example
Use `@Data` with `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` to avoid Hibernate lazy-loading issues.
`@Builder` requires `@NoArgsConstructor` + `@AllArgsConstructor` when used with `@Data`.

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "username", nullable = false, length = 50)
    private String username;
}
```

### Service / Controller example
```java
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserReadRepository readRepo;
    private final UserWriteRepository writeRepo;
    private final UserMapper mapper;
}
```

## JSON Serialization
- Field names in JSON: `snake_case`
- Always annotate every field with `@JsonProperty("snake_case")` for JSON mapping
- `@JsonIgnoreProperties(ignoreUnknown = true)` — ใส่เฉพาะ **Request DTO** เท่านั้น (deserialize JSON เข้า) ห้ามใส่บน Response DTO

### Request DTO example
```java
@Data
@JsonIgnoreProperties(ignoreUnknown = true)   // ✅ Request รับ JSON จากภายนอก → ต้องมี
public class UserRequest {
    @JsonProperty("user_name")
    private String username;

    @JsonProperty("pass_word")
    private String password;
}
```

### Response DTO example
```java
@Data
@Builder
// ❌ ไม่ใส่ @JsonIgnoreProperties — Response DTO แค่ serialize ออกไป ไม่ได้รับ JSON เข้า
public class UserResponse {
    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("user_name")
    private String username;

    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}
```

## Package Structure
Organize by layer under the root package:

```
com.example.app
├── config
│   ├── AppConfig.java                           ← RestTemplate, ObjectMapper beans
│   ├── PostgresqlSourceReadConfiguration.java
│   ├── PostgresqlSourceWriteConfiguration.java
│   ├── OracleSourceReadConfiguration.java       ← เพิ่มเมื่อใช้ Oracle
│   ├── OracleSourceWriteConfiguration.java
│   ├── SqlServerSourceReadConfiguration.java    ← เพิ่มเมื่อใช้ SQL Server
│   └── SqlServerSourceWriteConfiguration.java
├── controller
│   └── UserController.java                      ← @RestController, inject interface only
├── service
│   ├── UserService.java                         ← interface
│   └── impl
│       └── UserServiceImpl.java                 ← @Slf4j @Service implements UserService
├── repository
│   ├── postgres
│   │   ├── read
│   │   │   ├── UserReadRepository.java          ← JpaRepository + @Transactional(readOnly=true)
│   │   │   └── UserJdbcReadRepository.java      ← NamedParameterJdbcTemplate (read)
│   │   └── write
│   │       ├── UserWriteRepository.java         ← JpaRepository + @Transactional
│   │       └── UserJdbcWriteRepository.java     ← NamedParameterJdbcTemplate (write)
│   ├── oracle                                   ← เพิ่มเมื่อใช้ Oracle
│   │   ├── read
│   │   │   ├── UserReadRepository.java
│   │   │   └── UserJdbcReadRepository.java
│   │   └── write
│   │       ├── UserWriteRepository.java
│   │       └── UserJdbcWriteRepository.java
│   └── sqlserver                                ← เพิ่มเมื่อใช้ SQL Server
│       ├── read
│       │   ├── UserReadRepository.java
│       │   └── UserJdbcReadRepository.java
│       └── write
│           ├── UserWriteRepository.java
│           └── UserJdbcWriteRepository.java
├── model
│   ├── entity
│   │   └── User.java                            ← @Entity @Table
│   └── dto
│       ├── request
│       │   └── UserRequest.java                 ← @Data @JsonIgnoreProperties
│       └── response
│           └── UserResponse.java                ← @Data @Builder @JsonIgnoreProperties
├── mapper
│   └── UserMapper.java                          ← @Mapper(componentModel = "spring")
├── validator
│   └── UserValidator.java                       ← custom @Constraint validators
├── exception
│   ├── base
│   │   ├── BaseException.java                   ← abstract + HttpStatus + errorCode
│   │   └── ExceptionHandle.java                 ← generic one-off exception, extends BusinessException
│   ├── business
│   │   ├── BusinessException.java               ← extends BaseException, default 400
│   │   └── ResourceNotFoundException.java       ← extends BusinessException, default 404
│   ├── system
│   │   └── ServiceException.java                ← extends BaseException, default 500
│   ├── handler
│   │   └── GlobalExceptionHandler.java          ← @RestControllerAdvice
│   └── response
│       └── ErrorResponse.java                   ← @Data @Builder, returned by handler
├── constant
│   ├── ErrorCode.java                           ← final class + record Detail(code, message)
│   └── enums
│       └── UserStatus.java                      ← domain enum values
└── util
    └── DateUtil.java                            ← ตัวอย่าง: static helper methods

```

## Validation Pattern

### `@Valid` vs `@Validated`

| | `@Valid` | `@Validated` |
|---|---|---|
| ใช้ที่ | Controller `@RequestBody` | Service method parameter, class-level group |
| สนับสนุน Validation Groups | ❌ | ✅ |
| package | `jakarta.validation` | `org.springframework.validation.annotation` |

**กฎ:**
- Controller → ใช้ `@Valid` บน `@RequestBody` เสมอ
- Service → ใช้ `@Validated` ที่ class level เมื่อต้องการ validate method parameter

### Validation annotations บน DTO fields

```java
@Data
@JsonIgnoreProperties(ignoreUnknown = true)   // Request DTO เท่านั้น
public class UserRequest {

    @NotBlank(message = "username is required")
    @Size(min = 3, max = 50, message = "username must be between 3 and 50 characters")
    @JsonProperty("user_name")
    private String username;

    @NotBlank(message = "password is required")
    @Size(min = 8, message = "password must be at least 8 characters")
    @JsonProperty("pass_word")
    private String password;

    @NotBlank(message = "email is required")
    @Email(message = "email format is invalid")
    @JsonProperty("email")
    private String email;

    @NotNull(message = "age is required")
    @Min(value = 0, message = "age must be >= 0")
    @Max(value = 150, message = "age must be <= 150")
    @JsonProperty("age")
    private Integer age;
}
```

### Common annotation reference

| Annotation | ใช้กับ | ความหมาย |
|---|---|---|
| `@NotNull` | Any | ห้าม null |
| `@NotBlank` | String | ห้าม null, empty, whitespace-only |
| `@NotEmpty` | String, Collection | ห้าม null หรือ empty |
| `@Size(min, max)` | String, Collection | ขนาด |
| `@Min` / `@Max` | Number | ค่าต่ำสุด / สูงสุด |
| `@Email` | String | รูปแบบ email |
| `@Pattern(regexp)` | String | regex pattern |
| `@Positive` | Number | ต้องมากกว่า 0 |
| `@PositiveOrZero` | Number | ต้อง >= 0 |
| `@Future` / `@Past` | Date | ต้องเป็นอนาคต / อดีต |

### Custom Validator

ใช้เมื่อ built-in annotations ไม่เพียงพอ เช่น ตรวจสอบ business rule

```java
// 1. สร้าง annotation
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueUsernameValidator.class)
public @interface UniqueUsername {
    String message() default "Username already exists";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

// 2. สร้าง validator — อยู่ใน validator/ package
@Component
public class UniqueUsernameValidator implements ConstraintValidator<UniqueUsername, String> {

    private final UserReadRepository readRepo;

    public UniqueUsernameValidator(UserReadRepository readRepo) {
        this.readRepo = readRepo;
    }

    @Override
    public boolean isValid(String username, ConstraintValidatorContext ctx) {
        if (username == null) return true; // let @NotBlank handle null
        return !readRepo.existsByUsername(username);
    }
}

// 3. ใช้บน DTO field
@UniqueUsername
@JsonProperty("user_name")
private String username;
```

### `@Validated` บน Service (method-level validation)

```java
@Validated
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Override
    public UserResponse create(@Valid UserRequest req) { ... }
}
```

> ต้องมี `spring-boot-starter-validation` ใน `build.gradle`

## Mapper Pattern

ใช้ **MapStruct** สำหรับแปลง Entity ↔ DTO เสมอ — ห้ามเขียน mapping logic ใน Service

```groovy
// build.gradle
implementation 'org.mapstruct:mapstruct:1.6.0'
annotationProcessor 'org.mapstruct:mapstruct-processor:1.6.0'
```

```java
@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "id",       target = "userId")
    @Mapping(source = "username", target = "username")
    UserResponse toResponse(User entity);

    @Mapping(source = "username", target = "username")
    @Mapping(target = "id",        ignore = true)
    User toEntity(UserRequest request);
}
```

ServiceImpl inject mapper ผ่าน constructor (`@RequiredArgsConstructor`):
```java
private final UserMapper mapper;

// usage
return mapper.toResponse(readRepo.findById(id).orElseThrow(...));
```

## Constant / ErrorCode Pattern

ใช้ `record` เป็น value type ใน constants class — bundle `code` + `message` ไว้ด้วยกัน ไม่ใช้ enum

```java
// constant/ErrorCode.java
public final class ErrorCode {
    private ErrorCode() {}

    public record Detail(String code, String message) {}

    // --- User ---
    public static final Detail USER_NOT_FOUND     = new Detail("ERR-001", "User not found");
    public static final Detail DUPLICATE_USERNAME = new Detail("ERR-002", "Username already exists");
    public static final Detail INVALID_CREDENTIALS = new Detail("ERR-003", "Invalid username or password");

    // --- System ---
    public static final Detail INTERNAL_ERROR     = new Detail("ERR-500", "Internal server error");
    public static final Detail DB_UNAVAILABLE     = new Detail("ERR-501", "Database unavailable");
}
```

`BaseException` รับ `ErrorCode.Detail` โดยตรง:
```java
public abstract class BaseException extends RuntimeException {
    private final HttpStatus httpStatus;
    private final String errorCode;

    public BaseException(ErrorCode.Detail error, HttpStatus httpStatus) {
        super(error.message());
        this.errorCode = error.code();
        this.httpStatus = httpStatus;
    }

    // overload — ใช้เมื่อต้องการ message แบบ dynamic เช่น "User id 99 not found"
    public BaseException(ErrorCode.Detail error, String customMessage, HttpStatus httpStatus) {
        super(customMessage);
        this.errorCode = error.code();
        this.httpStatus = httpStatus;
    }

    // overload — ใช้เมื่อมี cause เช่น ServiceException
    public BaseException(ErrorCode.Detail error, HttpStatus httpStatus, Throwable cause) {
        super(error.message(), cause);
        this.errorCode = error.code();
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() { return httpStatus; }
    public String getErrorCode()      { return errorCode; }
}
```

Subclass ส่ง `Detail` และ default status:
```java
public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(ErrorCode.Detail error) {
        super(error, HttpStatus.NOT_FOUND);
    }
    public ResourceNotFoundException(ErrorCode.Detail error, String customMessage) {
        super(error, customMessage, HttpStatus.NOT_FOUND);
    }
}
```

Usage:
```java
// message จาก constant
throw new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND);

// message แบบ dynamic — code ยังมาจาก constant
throw new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "User id " + id + " not found");

// override HTTP status
throw new BusinessException(ErrorCode.DUPLICATE_USERNAME, HttpStatus.CONFLICT);
```

`ErrorResponse` เพิ่ม `code` field:
```java
@Data
@Builder
public class ErrorResponse {
    @JsonProperty("status")   private int status;
    @JsonProperty("error")    private String error;
    @JsonProperty("code")     private String code;      // "ERR-001"
    @JsonProperty("message")  private String message;
    @JsonProperty("timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
}
```

`GlobalExceptionHandler` อ่าน `errorCode` อัตโนมัติ:
```java
@ExceptionHandler(BaseException.class)
public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex) {
    return ResponseEntity.status(ex.getHttpStatus()).body(
            ErrorResponse.builder()
                    .status(ex.getHttpStatus().value())
                    .error(ex.getHttpStatus().getReasonPhrase())
                    .code(ex.getErrorCode())
                    .message(ex.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
}
```

Response ที่ได้:
```json
{
  "status": 404,
  "error": "Not Found",
  "code": "ERR-001",
  "message": "User id 99 not found",
  "timestamp": "2026-05-31T15:00:00"
}
```

## Comments
- Write all comments in English
- Use Javadoc for public methods and classes

```java
/**
 * Calculates the total price including tax.
 */
public double calculateTotal(double price) { ... }
```


## API Media Type (consumes / produces)

| Media Type | ความหมาย |
|---|---|
| `application/json` | JSON |
| `application/xml` | XML |
| `multipart/form-data` | File upload |
| `application/x-www-form-urlencoded` | Form data |
| `text/plain` | Plain text |

### กฎการใช้งาน

- ใช้ค่าคงที่จาก `MediaType` แทน string ตรงๆ เสมอ เช่น `MediaType.APPLICATION_JSON_VALUE`
- ให้ client เลือก format เองผ่าน `Content-Type` (สำหรับ request body) และ `Accept` (สำหรับ response)
- ระบุ `produces` และ `consumes` ที่ระดับ method เสมอ ห้ามใส่ที่ class level
- `consumes` ใส่เฉพาะ method ที่รับ `@RequestBody` เท่านั้น (`POST`, `PUT`, `PATCH`)
- `GET` และ `DELETE` ไม่มี request body → ไม่ต้องระบุ `consumes`
- DTO ที่ต้องรองรับ XML ต้องเพิ่ม dependency `jackson-dataformat-xml`

### โครงสร้าง Controller

```java
@RestController
@RequestMapping(path = "/api/resource")
@RequiredArgsConstructor
public class ResourceController {

    // GET — ระบุ produces ที่ method, ไม่มี consumes
    @GetMapping(produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    public List<ResourceResponse> getAll() { ... }

    @GetMapping(path = "/{id}", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    public ResourceResponse getById(@PathVariable Long id) { ... }

    // POST — ระบุทั้ง consumes และ produces ที่ method + @Valid บน @RequestBody เสมอ
    @PostMapping(
        consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE },
        produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
    )
    public ResponseEntity<ResourceResponse> create(@Valid @RequestBody ResourceRequest req) { ... }

    // PUT — ระบุทั้ง consumes และ produces ที่ method + @Valid บน @RequestBody เสมอ
    @PutMapping(
        path = "/{id}",
        consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE },
        produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
    )
    public ResourceResponse update(@PathVariable Long id, @Valid @RequestBody ResourceRequest req) { ... }

    // DELETE — ไม่มี request body และคืน Void → ยกเว้น consumes/produces
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) { ... }
}
```

### build.gradle

```groovy
implementation 'com.fasterxml.jackson.dataformat:jackson-dataformat-xml'
```

## Service Layer Pattern

ทุก Service ต้องสร้างเป็น interface ก่อน แล้วให้ Impl implements เสมอ

```
service/
├── UserService.java             ← interface
└── impl/
    └── UserServiceImpl.java     ← @Service implements UserService
```

### Interface

```java
public interface UserService {
    UserResponse create(UserRequest req);
}
```

### Implementation

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserReadRepository readRepo;
    private final UserWriteRepository writeRepo;
    private final UserMapper mapper;

    @Override
    public UserResponse create(UserRequest req) {
        return mapper.toResponse(writeRepo.save(mapper.toEntity(req)));
    }
}
```

### Controller — inject ผ่าน interface เสมอ

```java
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService service; // inject through interface, not implementation
}
```

## Transactional Pattern

- `@Transactional` ใส่ที่ระดับ **service method** ไม่ใช่ class level
- Read-only methods ใส่ `@Transactional(readOnly = true)` เพื่อ optimize query
- Write methods ใส่ `@Transactional("postgresWriteTransactionManager")` — ระบุ transactionManager ให้ตรง datasource เสมอ
- ห้ามใส่ `@Transactional` ใน Controller

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(Long id) { ... }

    @Override
    @Transactional("postgresWriteTransactionManager")
    public UserResponse create(UserRequest req) { ... }

    @Override
    @Transactional("postgresWriteTransactionManager")
    public void deleteById(Long id) { ... }
}
```

## Exception Handling

### กฎการใช้งาน

- ทุก method ใน ServiceImpl ต้องครอบ try-catch เสมอ
- `ResourceNotFoundException` และ custom exception อื่น → rethrow ต่อตรงๆ ห้าม wrap
- `Exception` ทั่วไป → `log.error` แล้ว throw `ServiceException`
- Controller ไม่ต้องมี try-catch ให้ `GlobalExceptionHandler` จัดการแทน

### Custom Exceptions

```
exception/
├── base/
│   ├── BaseException.java             ← abstract base, carries HttpStatus + errorCode
│   └── ExceptionHandle.java           ← generic one-off, extends BusinessException
├── business/
│   ├── BusinessException.java         ← extends BaseException, default 400
│   └── ResourceNotFoundException.java ← extends BusinessException, default 404
├── system/
│   └── ServiceException.java          ← extends BaseException, default 500
├── handler/
│   └── GlobalExceptionHandler.java    ← @RestControllerAdvice
└── response/
    └── ErrorResponse.java             ← @Data @Builder, returned by handler
```

### ExceptionHandle — when to use

ใช้ `ExceptionHandle` เมื่อ error เป็น one-off ที่ไม่คุ้มสร้าง class ใหม่:

```java
// ✅ ใช้ ExceptionHandle สำหรับ error ที่ไม่ซ้ำหลายที่
throw new ExceptionHandle(ErrorCode.SCRAPER_ERROR, "Rate limit hit", HttpStatus.TOO_MANY_REQUESTS);

// ✅ ใช้ specific subclass เมื่อ error type ถูก throw หลายจุด
throw new TickerNotFoundException(symbol);          // extends ResourceNotFoundException
throw new ScraperUnavailableException(message);     // extends BusinessException → 503
throw new FactsheetParseException(symbol, cause);   // extends ServiceException → 500
```

| ใช้ | เมื่อ |
|---|---|
| `ExceptionHandle` | error เกิดครั้งเดียว, ไม่ต้องการ catch เฉพาะ type ใน test |
| specific subclass | error เกิดหลายจุด หรือต้องการ `catch (TickerNotFoundException e)` |

```java
// base/BaseException.java — carries HttpStatus + errorCode, handler needs no per-exception mapping
public abstract class BaseException extends RuntimeException {
    private final HttpStatus httpStatus;
    private final String errorCode;

    public BaseException(ErrorCode.Detail error, HttpStatus httpStatus) {
        super(error.message());
        this.errorCode = error.code();
        this.httpStatus = httpStatus;
    }

    public BaseException(ErrorCode.Detail error, String customMessage, HttpStatus httpStatus) {
        super(customMessage);
        this.errorCode = error.code();
        this.httpStatus = httpStatus;
    }

    public BaseException(ErrorCode.Detail error, HttpStatus httpStatus, Throwable cause) {
        super(error.message(), cause);
        this.errorCode = error.code();
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() { return httpStatus; }
    public String getErrorCode()      { return errorCode; }
}

// business/BusinessException.java — default 400, overridable per throw
public class BusinessException extends BaseException {
    public BusinessException(ErrorCode.Detail error) {
        super(error, HttpStatus.BAD_REQUEST);
    }
    public BusinessException(ErrorCode.Detail error, HttpStatus status) {
        super(error, status);
    }
    public BusinessException(ErrorCode.Detail error, String customMessage, HttpStatus status) {
        super(error, customMessage, status);
    }
}

// business/ResourceNotFoundException.java — always 404
public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(ErrorCode.Detail error) {
        super(error, HttpStatus.NOT_FOUND);
    }
    public ResourceNotFoundException(ErrorCode.Detail error, String customMessage) {
        super(error, customMessage, HttpStatus.NOT_FOUND);
    }
}

// system/ServiceException.java — always 500
public class ServiceException extends BaseException {
    public ServiceException(ErrorCode.Detail error, Throwable cause) {
        super(error, HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }
}
```

**ส่ง HTTP status ที่กำหนดเองได้ตอน throw:**
```java
// 400 Bad Request (default)
throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);

// 409 Conflict — override status ตอน throw
throw new BusinessException(ErrorCode.DUPLICATE_USERNAME, HttpStatus.CONFLICT);

// 404 Not Found — message จาก constant
throw new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND);

// 404 Not Found — message แบบ dynamic
throw new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "User id " + id + " not found");

// 500 Internal Server Error
throw new ServiceException(ErrorCode.INTERNAL_ERROR, e);
```

### ServiceImpl pattern

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        try {
            return readRepo.findById(id)
                    .map(mapper::toResponse)
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "User id " + id + " not found"));
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to find user id: {}", id, e);
            throw new ServiceException(ErrorCode.INTERNAL_ERROR, e);
        }
    }

    @Override
    @Transactional("postgresWriteTransactionManager")
    public UserResponse create(UserRequest req) {
        try {
            return mapper.toResponse(writeRepo.save(mapper.toEntity(req)));
        } catch (Exception e) {
            log.error("Failed to create user", e);
            throw new ServiceException(ErrorCode.INTERNAL_ERROR, e);
        }
    }
}
```

### GlobalExceptionHandler

Handler มี 3 ตัว:
- `BaseException` — ครอบ **ทุก subclass** อัตโนมัติ (`BusinessException`, `ResourceNotFoundException`, `ServiceException`, `ExceptionHandle`, และ domain exceptions ทั้งหมด) ผ่าน `ex.getHttpStatus()` + `ex.getErrorCode()`
- `MethodArgumentNotValidException` — จัดการ `@Valid` / `@Validated` failure แยก
- `Exception` — catch-all สำหรับ error ที่ไม่ได้ handle

```java
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // handles ALL BaseException subclasses (including ExceptionHandle) —
    // status and code are read dynamically from the exception itself
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex) {
        HttpStatus status = ex.getHttpStatus();
        log.warn("Application exception: status={} code={} message={}",
                status, ex.getErrorCode(), ex.getMessage());
        return ResponseEntity.status(status).body(
                ErrorResponse.builder()
                        .status(status.value())
                        .error(status.getReasonPhrase())
                        .code(ex.getErrorCode())
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    // handles @Valid / @Validated failures — always 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.builder()
                        .status(400).error("Bad Request")
                        .code("VALIDATION_ERROR")
                        .message(message)
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    // catch-all for unexpected exceptions — always 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.builder()
                        .status(500).error("Internal Server Error")
                        .message("An unexpected error occurred")
                        .timestamp(LocalDateTime.now())
                        .build());
    }
}
```

**ทุก BaseException subclass ถูก handle ด้วย handler เดียว:**

```java
// TickerNotFoundException (404) → handleBaseException → status=404 code="FACTSHEET-001"
throw new TickerNotFoundException(symbol);

// ScraperUnavailableException (503) → handleBaseException → status=503 code="FACTSHEET-003"
throw new ScraperUnavailableException("timeout");

// ExceptionHandle — generic one-off → handleBaseException → status=429
throw new ExceptionHandle(ErrorCode.SCRAPER_ERROR, "Rate limit hit", HttpStatus.TOO_MANY_REQUESTS);

// ไม่ต้องเพิ่ม @ExceptionHandler method ใหม่เมื่อสร้าง exception type ใหม่
```

`MethodArgumentNotValidException` จะถูก throw เมื่อ `@Valid` บน `@RequestBody` fail — response เป็น `ErrorResponse` format เดียวกับทุก exception:
```json
{
  "status": 400,
  "error": "Bad Request",
  "code": "VALIDATION_ERROR",
  "message": "username: must not be blank, password: size must be between 8 and 2147483647",
  "timestamp": "2026-05-31T15:00:00"
}
```

> ต้องมี `spring-boot-starter-validation` และ `@Valid` บน `@RequestBody` ใน controller ด้วย


---

## Database Configuration Pattern

ทุก datasource ต้องแยก Read / Write เสมอ และใช้โครงสร้างนี้เป็นมาตรฐาน

### กฎการตั้งชื่อ Bean

| ส่วน | รูปแบบ | ตัวอย่าง (Postgres Read) |
|---|---|---|
| DataSourceProperties | `{db}Properties{RW}` | `postgresPropertiesRead` |
| DataSource | `{db}DataSource{RW}` | `postgresDataSourceRead` |
| JPA Properties | `{db}Properties{RW}Properties` | `postgresPropertiesReadProperties` |
| EntityManagerFactory | `{db}{RW}EntityManagerFactory` | `postgresReadEntityManagerFactory` |
| TransactionManager | `{db}{RW}TransactionManager` | `postgresReadTransactionManager` |
| JdbcTemplate | `{db}{RW}JdbcTemplate` | `postgresReadJdbcTemplate` |
| NamedParameterJdbcTemplate | `{db}NamedParameter{RW}JdbcTemplate` | `postgresNamedParameterReadJdbcTemplate` |

`{db}` = `postgres` / `oracle` / `sqlserver`  
`{RW}` = `Read` / `Write`

### กฎ @Primary

- Read configuration ของ datasource หลักต้องมี `@Primary` บน DataSourceProperties, DataSource และ EntityManagerFactory
- Write configuration ไม่ต้องมี `@Primary`

### application.yaml — โครงสร้าง config key

```yaml
spring:
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
      - org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
      - org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration
  jpa:
    open-in-view: false
  datasource:
    # --- PostgreSQL ---
    postgres:
      write:
        url: jdbc:postgresql://host:5432/dbname?currentSchema=public
        username: user
        password: pass
        driver-class-name: org.postgresql.Driver
        jpa:
          hibernate.ddl-auto: none
          properties.hibernate.default_schema: public
          show-sql: true
      read:
        url: jdbc:postgresql://host:5432/dbname?currentSchema=public
        username: user
        password: pass
        driver-class-name: org.postgresql.Driver
        jpa:
          hibernate.ddl-auto: none
          properties.hibernate.default_schema: public
          show-sql: true

    # --- Oracle (เพิ่มเมื่อใช้งาน) ---
    oracle:
      write:
        url: jdbc:oracle:thin:@//host:1521/service
        username: user
        password: pass
        driver-class-name: oracle.jdbc.OracleDriver
        jpa:
          hibernate.ddl-auto: none
          properties.hibernate.default_schema: SCHEMA_NAME
          show-sql: true
      read:
        url: jdbc:oracle:thin:@//host:1521/service
        username: user
        password: pass
        driver-class-name: oracle.jdbc.OracleDriver
        jpa:
          hibernate.ddl-auto: none
          properties.hibernate.default_schema: SCHEMA_NAME
          show-sql: true

    # --- SQL Server (เพิ่มเมื่อใช้งาน) ---
    sqlserver:
      write:
        url: jdbc:sqlserver://host:1433;databaseName=dbname;encrypt=true;trustServerCertificate=true
        username: user
        password: pass
        driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver
        jpa:
          hibernate.ddl-auto: none
          show-sql: true
      read:
        url: jdbc:sqlserver://host:1433;databaseName=dbname;encrypt=true;trustServerCertificate=true
        username: user
        password: pass
        driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver
        jpa:
          hibernate.ddl-auto: none
          show-sql: true
```

### Configuration Class Template (Read)

ใช้ template นี้เป็นต้นแบบ เปลี่ยนแค่ `{Db}` และ config key prefix

> **Spring Boot version note**
> - Boot 3.x: `import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties`
> - Boot 4.x: `import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties`

```java
@Configuration
@EnableJpaRepositories(
        entityManagerFactoryRef = "{db}ReadEntityManagerFactory",
        transactionManagerRef   = "{db}ReadTransactionManager",
        basePackages            = {"com.example.app.repository.{db}.read"}
)
@EnableTransactionManagement
public class {Db}SourceReadConfiguration {

    @Primary
    @Bean("{db}PropertiesRead")
    @ConfigurationProperties("spring.datasource.{db}.read")
    public DataSourceProperties dataSourceProperties() { return new DataSourceProperties(); }

    @Primary
    @Bean("{db}DataSourceRead")
    @ConfigurationProperties("spring.datasource.{db}.read.hikari")
    public DataSource dataSource(@Qualifier("{db}PropertiesRead") DataSourceProperties p) {
        return p.initializeDataSourceBuilder().build();
    }

    @Bean("{db}PropertiesReadProperties")
    @ConfigurationProperties("spring.datasource.{db}.read.jpa")
    public Properties jpaProperties() { return new Properties(); }

    @Primary
    @Bean("{db}ReadEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            @Qualifier("{db}DataSourceRead") DataSource ds,
            @Qualifier("{db}PropertiesReadProperties") Properties props) {
        LocalContainerEntityManagerFactoryBean f = new LocalContainerEntityManagerFactoryBean();
        f.setDataSource(ds);
        f.setPackagesToScan("com.example.app.model.entity");
        f.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        f.setJpaProperties(props);
        return f;
    }

    @Bean("{db}ReadTransactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("{db}ReadEntityManagerFactory") LocalContainerEntityManagerFactoryBean emf) {
        return new JpaTransactionManager(emf.getObject());
    }

    @Bean("{db}ReadJdbcTemplate")
    public JdbcTemplate jdbcTemplate(@Qualifier("{db}DataSourceRead") DataSource ds) {
        return new JdbcTemplate(ds);
    }

    @Bean("{db}NamedParameterReadJdbcTemplate")
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(@Qualifier("{db}DataSourceRead") DataSource ds) {
        return new NamedParameterJdbcTemplate(ds);
    }
}
```

### Configuration Class Template (Write)

เหมือน Read แต่ **ลบ `@Primary` ออกทั้งหมด** และเปลี่ยน `Read` → `Write`

```java
@Configuration
@EnableJpaRepositories(
        entityManagerFactoryRef = "{db}WriteEntityManagerFactory",
        transactionManagerRef   = "{db}WriteTransactionManager",
        basePackages            = {"com.example.app.repository.{db}.write"}
)
@EnableTransactionManagement
public class {Db}SourceWriteConfiguration {

    @Bean("{db}PropertiesWrite")
    @ConfigurationProperties("spring.datasource.{db}.write")
    public DataSourceProperties dataSourceProperties() { return new DataSourceProperties(); }

    @Bean("{db}DataSourceWrite")
    @ConfigurationProperties("spring.datasource.{db}.write.hikari")
    public DataSource dataSource(@Qualifier("{db}PropertiesWrite") DataSourceProperties p) {
        return p.initializeDataSourceBuilder().build();
    }

    @Bean("{db}PropertiesWriteProperties")
    @ConfigurationProperties("spring.datasource.{db}.write.jpa")
    public Properties jpaProperties() { return new Properties(); }

    @Bean("{db}WriteEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            @Qualifier("{db}DataSourceWrite") DataSource ds,
            @Qualifier("{db}PropertiesWriteProperties") Properties props) {
        LocalContainerEntityManagerFactoryBean f = new LocalContainerEntityManagerFactoryBean();
        f.setDataSource(ds);
        f.setPackagesToScan("com.example.app.model.entity");
        f.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        f.setJpaProperties(props);
        return f;
    }

    @Bean("{db}WriteTransactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("{db}WriteEntityManagerFactory") LocalContainerEntityManagerFactoryBean emf) {
        return new JpaTransactionManager(emf.getObject());
    }

    @Bean("{db}WriteJdbcTemplate")
    public JdbcTemplate jdbcTemplate(@Qualifier("{db}DataSourceWrite") DataSource ds) {
        return new JdbcTemplate(ds);
    }

    @Bean("{db}NamedParameterWriteJdbcTemplate")
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(@Qualifier("{db}DataSourceWrite") DataSource ds) {
        return new NamedParameterJdbcTemplate(ds);
    }
}
```

### Repository — ใช้ @Qualifier เลือก EntityManager ให้ถูก datasource

- Read repository ต้องมี `@Transactional(readOnly = true)` เสมอ
- Write repository ต้องมี `@Transactional` เสมอ
- package ที่วางไฟล์คือตัวกำหนดว่า datasource ไหน scan

```java
// Read — อยู่ใน repository.{db}.read → scan โดย {Db}SourceReadConfiguration
@Repository
@Transactional(readOnly = true)
public interface UserReadRepository extends JpaRepository<User, Long> { }

// Write — อยู่ใน repository.{db}.write → scan โดย {Db}SourceWriteConfiguration
@Repository
@Transactional
public interface UserWriteRepository extends JpaRepository<User, Long> { }
```

ServiceImpl ใช้ให้ถูก:

```java
private final UserReadRepository readRepo;   // findAll, findById, existsById
private final UserWriteRepository writeRepo; // save, deleteById
```

### JdbcRepository Pattern (NamedParameterJdbcTemplate)

ใช้เมื่อต้องการ query ซับซ้อน, JOIN หลายตาราง, หรือ bulk operation ที่ JPA ทำได้ไม่ดี

**กฎสำคัญ: ห้ามใช้ `@RequiredArgsConstructor` กับ `@Qualifier` บน field**
เพราะ Lombok ไม่ส่ง `@Qualifier` ไปใน generated constructor → Spring หา Bean ไม่เจอ
ต้องใช้ explicit constructor + `@Autowired` + `@Qualifier` บน parameter เสมอ

#### ตารางชื่อ Bean NamedParameterJdbcTemplate ตาม database

| Database | Read Bean | Write Bean |
|---|---|---|
| PostgreSQL | `postgresNamedParameterReadJdbcTemplate` | `postgresNamedParameterWriteJdbcTemplate` |
| Oracle | `oracleNamedParameterReadJdbcTemplate` | `oracleNamedParameterWriteJdbcTemplate` |
| SQL Server | `sqlserverNamedParameterReadJdbcTemplate` | `sqlserverNamedParameterWriteJdbcTemplate` |

#### ตารางชื่อ transactionManager ตาม database

| Database | Read TxManager | Write TxManager |
|---|---|---|
| PostgreSQL | `postgresReadTransactionManager` | `postgresWriteTransactionManager` |
| Oracle | `oracleReadTransactionManager` | `oracleWriteTransactionManager` |
| SQL Server | `sqlserverReadTransactionManager` | `sqlserverWriteTransactionManager` |

#### JdbcReadRepository Template

```java
@Repository
@Transactional(transactionManager = "{db}ReadTransactionManager", propagation = Propagation.NOT_SUPPORTED, readOnly = true)
public class UserJdbcReadRepository {

    private final NamedParameterJdbcTemplate jdbc;

    @Autowired
    public UserJdbcReadRepository(
            @Qualifier("{db}NamedParameterReadJdbcTemplate") NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = :username";
        return jdbc.query(sql, Map.of("username", username), new BeanPropertyRowMapper<>(User.class));
    }
}
```

#### JdbcWriteRepository Template

```java
@Repository
@Transactional(transactionManager = "{db}WriteTransactionManager")
public class UserJdbcWriteRepository {

    private final NamedParameterJdbcTemplate jdbc;

    @Autowired
    public UserJdbcWriteRepository(
            @Qualifier("{db}NamedParameterWriteJdbcTemplate") NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public int updatePassword(Long id, String password) {
        String sql = "UPDATE users SET password = :password WHERE id = :id";
        return jdbc.update(sql, Map.of("id", id, "password", password));
    }
}
```

`{db}` = `postgres` / `oracle` / `sqlserver`

### build.gradle — dependencies ตาม database

```groovy
// Core
implementation 'org.springframework.boot:spring-boot-starter-web'
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
implementation 'org.springframework.boot:spring-boot-starter-validation'
compileOnly     'org.projectlombok:lombok'
annotationProcessor 'org.projectlombok:lombok'

// JSON (XML support — เพิ่มเมื่อ controller ใช้ APPLICATION_XML_VALUE)
implementation 'com.fasterxml.jackson.dataformat:jackson-dataformat-xml'

// PostgreSQL
runtimeOnly 'org.postgresql:postgresql'

// Oracle (เพิ่มเมื่อใช้งาน)
runtimeOnly 'com.oracle.database.jdbc:ojdbc11'

// SQL Server (เพิ่มเมื่อใช้งาน)
runtimeOnly 'com.microsoft.sqlserver:mssql-jdbc'
```
