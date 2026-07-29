# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Bối cảnh dự án

Bộ khung (base/template) cho REST API bằng Spring Boot 3.5.4 + Java 21, dùng làm nền cho các service khác:
JWT stateless, error contract thống nhất, rate limit theo IP, cache Redis, và một framework export/import Excel.
Nhiều phần là **hạ tầng dùng lại** chứ không phải business logic — `User`, `AuthController`, `UserExportController`
chủ yếu là ví dụ minh hoạ cách dùng khung.

Javadoc và comment trong repo viết bằng **tiếng Việt** — giữ nguyên quy ước này khi thêm code.

## Lệnh thường dùng

```bash
./mvnw compile                              # build
./mvnw spring-boot:run                      # chạy app -> http://localhost:8080/common-api
./mvnw test                                 # chạy toàn bộ test
./mvnw test -Dtest=BaseApisApplicationTests           # chạy 1 class test
./mvnw test -Dtest=SomeTest#someMethod                # chạy 1 method
```

**Điều kiện để chạy:**

- **MySQL** ở `localhost:3306`, database `base_apis` **phải tồn tại sẵn** — URL trong `application.yaml` không có
  `createDatabaseIfNotExist`. Nếu chưa có, ghi đè URL khi chạy thay vì sửa yaml:
  `-Dspring-boot.run.arguments="--spring.datasource.url=jdbc:mysql://localhost:3306/base_apis?createDatabaseIfNotExist=true"`
- **Redis** ở `localhost:6379` — cần cho cache và rate limit hoạt động, nhưng **không** cần để context khởi động
  (kết nối được mở lười). `BaseApisApplicationTests` (`@SpringBootTest` load full context) pass được khi Redis tắt.
- `ddl-auto: update` nên schema tự tạo/cập nhật từ entity.
- Đừng thêm cờ `-o` (offline) cho `./mvnw test` nếu plugin surefire chưa có trong `~/.m2` — sẽ fail vì không tải được.

Repo **chưa có test nào ngoài `contextLoads`**. Không có Testcontainers, nên test tích hợp thật sẽ phụ thuộc
MySQL/Redis cài sẵn trên máy.

## Kiến trúc

### Error contract (đọc 5 file mới hiểu hết)

`ApiResponseCode` (enum) → `IApiResponse` → `BaseException` → `GlobalExceptionHandler` → `ApiErrorResponse`.

- `ApiResponseCode.code` là **chuỗi HTTP status** (`"404"`, `"429"`). `handleBusinessException` gọi
  `Integer.parseInt(ex.getCode())` để đặt HTTP status → **mọi code mới thêm vào enum phải là số HTTP hợp lệ**,
  nếu không request sẽ nổ ngay trong exception handler.
- Chọn exception theo tầng: `BusinessException` (status lấy từ code), `InternalException` (500),
  `CallApiException` (502), `UnauthenticatedException` (401), `TooManyRequestsException` (429).
- **Không có file `messages*.properties`.** `resolveDescription` thử tra `MessageSource`, `CommonUtils.getMessage`
  nuốt exception và trả `null`, `ApiErrorResponse` có `@JsonInclude(NON_NULL)` → `errorDescription` bị bỏ khỏi JSON.
  Nghĩa là muốn client thấy mô tả lỗi thì phải **truyền `messageDescription` tường minh** khi ném exception.
- Muốn thêm HTTP status mới (ví dụ 503) thì phải thêm cả entry vào `ApiResponseCode` **và** handler tương ứng.

### Authentication

Stateless JWT, không session. `SecurityConfig` mở `PUBLIC_ENDPOINTS = {"/auth/**"}`, mọi request khác cần xác thực.

- `TokenProvider` nhét claim `type` (`ACCESS_TOKEN` / `REFRESH_TOKEN`) và **bắt buộc đúng loại** khi parse —
  refresh token không dùng được để gọi API, access token không dùng được ở `/auth/refresh`.
- `JwtAuthenticationFilter` **không bao giờ ném lỗi**: token sai thì clear context và cho request đi tiếp,
  để `RestAuthenticationEntryPoint` trả 401. Đừng đổi thành ném exception — sẽ mất định dạng lỗi thống nhất.
- Principal là `IBEUser` (extends Spring `User`, có thêm `id`), bọc trong `AuthenticationToken`
  (extends `UsernamePasswordAuthenticationToken`, có thêm `userId`/`roles`/`token`).
- Lấy user hiện tại qua `SecurityUtils.getCurrentUserIdLogin()` / `getCurrentUserLogin()` — `getCurrentUserIdLogin`
  chỉ hoạt động khi authentication là `AuthenticationToken`.
- Role lưu trong DB là `Integer`, map qua `AuthoritiesConstants` (`ROLE_ADMIN(1)`, `ROLE_USER(2)`).

### JPA auditing

Entity `extends BaseEntity` là tự có `created_by/created_date/last_modified_by/last_modified_date`.
`SpringSecurityAuditorAware` cung cấp user id, fallback `0L` khi không có ai đăng nhập (job, luồng nội bộ).
`@EnableJpaAuditing(auditorAwareRef = "springSecurityAuditorAware")` nằm ở `DatabaseConfiguration`.

### Redis — ba đường dùng song song

`RedisConfig` cấu hình cả ba, dùng chung `spring.data.redis.*`:

| Đường | Bean | Dùng cho |
|---|---|---|
| Annotation cache | `@EnableCaching` + `RedisCacheConfiguration` | `@Cacheable`/`@CacheEvict`/`@CachePut` |
| Thủ công | `RedisTemplate<String, Object>` | TTL riêng từng key, `INCR`, `SETNX`, kiểm tra tồn tại |
| Bucket4j | `RedisClient` (Lettuce thuần) | rate limit, xem `RateLimitConfig` |

Cả cache và template đều serialize value bằng JSON có trường `@class` (không dùng JDK serializer mặc định của Boot,
nên object **không cần** `implements Serializable` và đọc được bằng `redis-cli`).
`PolymorphicTypeValidator` chỉ whitelist `com.vn.baseapis.` / `java.util.` / `java.time.` / `java.lang.` —
thêm package khác vào whitelist là mở rủi ro deserialization attack, cân nhắc kỹ.

Bean `redisTemplate` **thay thế** bean cùng tên của Spring Boot (Boot khai kèm `@ConditionalOnMissingBean(name=...)`).
`StringRedisTemplate` của Boot vẫn còn, dùng cho chuỗi thuần.

### Rate limit theo IP

`@IpRateLimited(limit, durationSeconds)` trên method controller + `IpRateLimitAspect` (Spring AOP) + bucket trên Redis.
Key là `rate-limit:<ip>:<uri>` nên **mỗi cặp (IP, endpoint) có quota riêng**. TTL do Bucket4j quản lý, không cần job dọn.

- IP lấy từ `request.getRemoteAddr()`, **tuyệt đối không tự parse `X-Forwarded-For`** — header do client gửi nên
  parse tay là lỗ hổng lách rate limit. Việc bóc tách giao cho Tomcat `RemoteIpValve`
  (`server.forward-headers-strategy: native` + `server.tomcat.remoteip.internal-proxies`), nó chỉ tin header khi
  peer kết nối trực tiếp nằm trong danh sách proxy tin cậy.
- `app.rate-limit.fail-open` (mặc định `true`): Redis lỗi thì cho request đi qua + log `WARN`; đặt `false` để từ chối (500).
- Khi triển khai production nên siết `internal-proxies` về đúng IP của reverse proxy — dải private mặc định là
  điểm yếu trong môi trường container/K8s.

### Framework Excel (`service/io`)

Export: `IExcelExporter` ← `AbstractExcelExporter` ← `BaseExcelExporter` (XSSF, giữ hết trong RAM, auto-size cột,
~1000 bản ghi) hoặc `StreamExcelExporter` (SXSSF, streaming, độ rộng cột cố định, ~100.000 bản ghi).
Cột khai bằng `ExcelColumn.of(header, extractor).width(n)`; exporter tự map `String`/`Number`/`Boolean`/
`LocalDate`/`LocalDateTime`/`Date` sang ô, kiểu khác dùng `toString()`.

- Muốn header tuỳ biến thì override `writeCustomHeader(Sheet, Workbook)` và **trả về số dòng đã ghi** — exporter dựa
  vào con số đó để dịch tiêu đề cột, dữ liệu và freeze-pane xuống. Trả sai số là file lệch dòng.
- `ExcelDownload.to(response, fileName, exporter, data)` lo `Content-Type` + `Content-Disposition`; tách riêng để
  việc tạo file không phụ thuộc servlet.
- Với dữ liệu lớn, truyền `Iterable` lười (`() -> stream.iterator()`) chứ đừng `toList()` — mất hết ý nghĩa streaming.

Import: `IExcelImporter` / `StreamExcelImporter` + `ExcelRowMapper` map từng `ExcelRow` → object.
Giá trị ô luôn là `String` đã format sẵn bởi POI; mapper trả `null` để bỏ qua dòng đó.

### Validation tuỳ chỉnh

`@MyDate` / `@MyDateTime` kiểm tra chuỗi ngày theo pattern (mặc định `dd/MM/yyyy`, `dd/MM/yyyy HH:mm:ss` trong
`DateTimeUtils`). Cả hai coi `null`/rỗng là **hợp lệ** — phải ghép thêm `@NotBlank` nếu trường là bắt buộc.
Toàn bộ hệ thống dùng timezone `UTC+07:00` qua `DateTimeUtils.zoneId7`.

## Bẫy đã gặp thực tế

**Spring AOP là proxy** — ảnh hưởng trực tiếp tới `@IpRateLimited`, `@Cacheable`, `@Transactional`:
- Gọi nội bộ `this.method()` **không** qua proxy → annotation bị bỏ qua, im lặng không báo lỗi.
- Gắn annotation lên method `private`/`final`/`static` → không có tác dụng, cũng không có warning.
- Proxy CGLIB được tạo **không qua constructor** → đọc *field* trực tiếp trên bean bị proxy sẽ ra `null`.
  Luôn truy cập qua method (đây là nguyên nhân của những `NullPointerException` rất khó hiểu).

**`ProxyManager` trong `RateLimitConfig` phải giữ `@Lazy`** (cả bean definition lẫn injection point ở
`IpRateLimitAspect`). `Bucket4jLettuce...build()` mở kết nối Redis ngay lúc tạo bean; bỏ `@Lazy` là app không boot
được khi Redis chưa sẵn sàng, đồng thời vô hiệu hoá luôn `fail-open`. Lombok `@RequiredArgsConstructor` **không**
copy `@Lazy` sang tham số constructor nên phải viết constructor tường minh.

**Generic matching khi inject `RedisTemplate`** bất đối xứng: khai `RedisTemplate<String, Object>` (đúng) và
`RedisTemplate<Object, Object>` đều resolve về bean trong `RedisConfig`; nhưng nếu bean là `<Object, Object>` thì
khai `<String, Object>` sẽ **không** tìm thấy bean và app chết lúc khởi động.

**Giá trị lưu vào Redis** (cả cache lẫn template): đừng để `Map.of(...)` / `List.of(...)` làm value ở tầng ngoài
cùng — class final của JDK không được ghi `@class`, lần đọc lại ném `InvalidTypeIdException`. `LocalDateTime` ngoài
cùng thì đọc ra `String` (mất kiểu, không báo lỗi). Dùng `new ArrayList<>(...)`/`new HashMap<>(...)` hoặc bọc trong
DTO; nằm *bên trong* DTO thì `List.of` bình thường.

**`CommonUtils.retrieveClientIpAddress` / `retrieveClientIpAddressV2` đọc header thô** (`X-Forwarded-For`,
`Proxy-Client-IP`, ...). Hiện không chỗ nào dùng. **Đừng dùng chúng cho quyết định bảo mật hay rate limit** —
sẽ tái tạo đúng lỗ hổng giả mạo mà `RemoteIpValve` được đưa vào để chặn.

**`context-path` là `/common-api`** — mọi URL khi test tay đều phải có tiền tố này
(`POST http://localhost:8080/common-api/auth/login`), rất dễ nhận 404 "không tìm thấy handler" và nhầm là lỗi logic.
