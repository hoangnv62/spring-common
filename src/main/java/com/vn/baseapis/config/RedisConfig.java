package com.vn.baseapis.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

/**
 * Cấu hình Redis cho hai mục đích:
 *
 * <ul>
 *   <li>Cache bằng annotation ({@code @Cacheable}, {@code @CacheEvict}, {@code @CachePut}) —
 *       {@code @EnableCaching} ở đây là thứ kích hoạt chúng, thiếu nó thì annotation không có tác dụng gì.</li>
 *   <li>{@link RedisTemplate} cho những việc annotation không diễn đạt được: TTL riêng cho từng key,
 *       {@code INCR}, kiểm tra tồn tại, distributed lock...</li>
 *   <li>{@link RedisClient} cho Bucket4j ở {@link RateLimitConfig} — bắt buộc phải có, nếu bỏ thì
 *       rate limit theo IP mất chỗ lưu bucket.</li>
 * </ul>
 *
 * <p>Cả ba đọc cấu hình chuẩn {@code spring.data.redis.*} nên chỉ khai báo host/port/password một lần.
 */
@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * Cấu hình cho {@code RedisCacheManager}. Bean này thay thế cấu hình mặc định của Spring Boot,
     * vì bản mặc định serialize value bằng {@code JdkSerializationRedisSerializer}: value trên Redis là
     * binary không đọc được bằng {@code redis-cli}, và mọi class đem cache buộc phải
     * {@code implements Serializable}. Đổi sang JSON để bỏ cả hai ràng buộc đó.
     *
     * <p>Value được ghi kèm trường {@code @class} nên lúc đọc trả về đúng class gốc thay vì
     * {@code LinkedHashMap}. Hai giới hạn đã kiểm chứng, cần biết khi chọn kiểu trả về của method cache:
     *
     * <ul>
     *   <li>Đừng để method cache trả về trực tiếp {@code Map.of(...)} / {@code List.of(...)}. Đây là các
     *       class final của JDK nên không được ghi {@code @class}, lần đọc lại sẽ ném
     *       {@code InvalidTypeIdException}. Dùng {@code new ArrayList<>(...)} / {@code new HashMap<>(...)},
     *       hoặc bọc trong DTO. Nằm <em>bên trong</em> một DTO thì {@code List.of} vẫn bình thường.</li>
     *   <li>Trả về trực tiếp {@code LocalDateTime} sẽ đọc ra {@code String} (mất kiểu, không báo lỗi).</li>
     * </ul>
     *
     * <p>Vẫn tôn trọng các property chuẩn {@code spring.cache.redis.*}: khi tự khai bean này thì Spring Boot
     * không áp property nữa (xem {@code determineConfiguration} trong autoconfig của Boot), nên phải tự đọc
     * {@link CacheProperties} và áp lại, nếu không TTL cấu hình trong yaml sẽ bị bỏ qua âm thầm.
     */
    @Bean
    public RedisCacheConfiguration cacheConfiguration(CacheProperties cacheProperties, ObjectMapper objectMapper) {
        CacheProperties.Redis redisProperties = cacheProperties.getRedis();

        RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig()
                // key giữ nguyên StringRedisSerializer của defaultCacheConfig -> key dạng "tenCache::khoa"
                .serializeValuesWith(SerializationPair.fromSerializer(GenericJackson2JsonRedisSerializer.builder()
                        .objectMapper(buildRedisObjectMapper(objectMapper))
                        .defaultTyping(true)
                        .build()));

        if (redisProperties.getTimeToLive() != null) {
            configuration = configuration.entryTtl(redisProperties.getTimeToLive());
        }
        if (redisProperties.getKeyPrefix() != null) {
            configuration = configuration.prefixCacheNameWith(redisProperties.getKeyPrefix());
        }
        if (!redisProperties.isCacheNullValues()) {
            configuration = configuration.disableCachingNullValues();
        }
        if (!redisProperties.isUseKeyPrefix()) {
            configuration = configuration.disableKeyPrefix();
        }
        return configuration;
    }

    /**
     * RedisTemplate với key dạng String và value dạng JSON, dùng cho các thao tác thủ công mà
     * {@code @Cacheable} không diễn đạt được (TTL riêng từng key, {@code INCR}, {@code SETNX}...).
     *
     * <p>Method này đặt tên đúng bằng {@code redisTemplate} nên **thay thế** bean mà Spring Boot tự cấu
     * hình (Boot khai kèm {@code @ConditionalOnMissingBean(name = "redisTemplate")}). Đây là chủ đích:
     * bản của Boot serialize cả key lẫn value bằng JDK nên trên Redis là binary, {@code redis-cli} không
     * đọc được và object buộc phải {@code implements Serializable}.
     *
     * <p>Nên inject đúng {@code RedisTemplate<String, Object>}. Khai {@code RedisTemplate<Object, Object>}
     * cũng resolve về chính bean này (Spring khớp generic theo assignability, {@code String} gán được vào
     * {@code Object}), nhưng khai sai kiểu key thì tự đánh mất tính rõ ràng. Chỉ lưu chuỗi thì dùng
     * {@code StringRedisTemplate} — bean đó Boot vẫn tự cấu hình, không bị ảnh hưởng.
     *
     * <p>Giới hạn của value JSON giống hệt phần cache ở trên: đừng lưu trực tiếp {@code Map.of(...)} /
     * {@code List.of(...)} hay {@code LocalDateTime} làm value ngoài cùng — bọc trong DTO, hoặc dùng
     * {@code new ArrayList<>(...)} / {@code new HashMap<>(...)}.
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory,
                                                      ObjectMapper objectMapper) {
        RedisSerializer<String> keySerializer = new StringRedisSerializer();
        RedisSerializer<Object> valueSerializer = GenericJackson2JsonRedisSerializer.builder()
                .objectMapper(buildRedisObjectMapper(objectMapper))
                .defaultTyping(true)
                .build();

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(keySerializer);
        template.setValueSerializer(valueSerializer);
        // Hash cũng phải set riêng, bỏ trống sẽ rơi về JDK serializer mặc định
        template.setHashKeySerializer(keySerializer);
        template.setHashValueSerializer(valueSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Client Lettuce thuần dành cho Bucket4j (yêu cầu codec byte[]), tách khỏi
     * {@code LettuceConnectionFactory} mà Spring Data Redis dựng cho cache.
     */
    @Bean(destroyMethod = "shutdown")
    public RedisClient redisClient(RedisProperties properties) {
        return RedisClient.create(buildRedisUri(properties));
    }

    /**
     * ObjectMapper riêng cho Redis, dựa trên ObjectMapper của app nên thừa hưởng sẵn các module đã
     * đăng ký (ví dụ JavaTimeModule để đọc/ghi được {@code LocalDateTime}).
     *
     * <p>Việc bật default typing được giao cho {@code defaultTyping(true)} của Spring thay vì tự gọi
     * {@code activateDefaultTyping(...)}, vì cả hai giá trị tự đặt đều có vấn đề đã kiểm chứng:
     * {@code NON_FINAL} bỏ qua class final (record DTO là final) nên ghi thiếu {@code @class} rồi ném
     * {@code InvalidTypeIdException} lúc đọc; còn {@code EVERYTHING} làm map bất biến đọc ra bị lẫn
     * thêm một entry {@code @class} vào chính map — hỏng dữ liệu mà không báo lỗi.
     *
     * <p>{@code PolymorphicTypeValidator} được siết theo package một cách có chủ đích. Default typing
     * nghĩa là Jackson sẽ khởi tạo class có tên ghi trong JSON, nên nếu cho phép mọi class thì kẻ ghi
     * được vào Redis có thể chèn tên một class "gadget" để thực thi mã (deserialization attack).
     *
     * <p>Không cần whitelist {@code NullValue}: Spring Data Redis ghi/đọc giá trị {@code null} bằng một
     * chuỗi nhị phân cố định, không đi qua Jackson (đã kiểm chứng — cache {@code null} vẫn hoạt động
     * khi bỏ {@code NullValue} khỏi danh sách).
     */
    private ObjectMapper buildRedisObjectMapper(ObjectMapper objectMapper) {
        // copy() để không làm ảnh hưởng ObjectMapper mà Spring MVC dùng cho request/response
        ObjectMapper redisMapper = objectMapper.copy();
        redisMapper.setPolymorphicTypeValidator(BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("com.vn.baseapis.")
                .allowIfSubType("java.util.")
                .allowIfSubType("java.time.")
                .allowIfSubType("java.lang.")
                .build());
        return redisMapper;
    }

    private RedisURI buildRedisUri(RedisProperties properties) {
        if (StringUtils.hasText(properties.getUrl())) {
            return RedisURI.create(properties.getUrl());
        }

        RedisURI.Builder builder = RedisURI.builder()
                .withHost(properties.getHost())
                .withPort(properties.getPort())
                .withDatabase(properties.getDatabase())
                .withSsl(properties.getSsl().isEnabled());

        if (properties.getTimeout() != null) {
            builder.withTimeout(properties.getTimeout());
        }
        if (StringUtils.hasText(properties.getPassword())) {
            if (StringUtils.hasText(properties.getUsername())) {
                builder.withAuthentication(properties.getUsername(), properties.getPassword().toCharArray());
            } else {
                builder.withPassword(properties.getPassword().toCharArray());
            }
        }
        return builder.build();
    }
}
