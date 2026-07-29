package com.vn.baseapis.config;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce;
import io.lettuce.core.RedisClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Hạ tầng rate limit phân tán: bucket được lưu trên Redis thay vì trong bộ nhớ từng instance,
 * nhờ đó N instance dùng chung một quota (trước đây limit thực tế bị nhân lên N lần).
 *
 * <p>Phần kết nối Redis do {@link RedisConfig} lo, class này chỉ dựng bucket trên nền kết nối đó.
 */
@Configuration
public class RateLimitConfig {

    /** Tiền tố key trên Redis để tách biệt với dữ liệu khác dùng chung instance Redis. */
    static final String KEY_PREFIX = "rate-limit:";

    /**
     * Khoảng dư thêm vào TTL của key so với thời điểm bucket hồi đầy token.
     * Bucket4j tự đặt TTL = (thời gian hồi đầy token) + margin, nên key của IP không còn
     * hoạt động sẽ tự bị Redis xoá, không cần job dọn dẹp.
     */
    private static final Duration TTL_MARGIN = Duration.ofMinutes(1);

    /**
     * Chặn trên thời gian chờ Redis cho mỗi lần kiểm tra rate limit. Nếu Redis treo,
     * request thất bại nhanh thay vì giữ thread cho tới khi hết socket timeout.
     */
    private static final Duration REDIS_REQUEST_TIMEOUT = Duration.ofSeconds(1);

    /**
     * ProxyManager nhận key dạng String (đã có tiền tố) và tự chuyển sang byte[] cho Redis.
     * Dùng cơ chế CAS (compare-and-swap) nên nhiều instance cùng cập nhật một bucket vẫn an toàn.
     *
     * <p>{@code @Lazy} là bắt buộc: {@code build()} mở kết nối Redis ngay khi được gọi, nếu tạo bean
     * lúc khởi động thì Redis chưa sẵn sàng sẽ làm app không boot được — mất luôn tác dụng của
     * {@code app.rate-limit.fail-open}. Dời sang request đầu tiên để lỗi kết nối được xử lý tại aspect.
     */
    @Bean
    @Lazy
    public ProxyManager<String> rateLimitProxyManager(RedisClient redisClient) {
        return Bucket4jLettuce.casBasedBuilder(redisClient)
                .expirationAfterWrite(ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(TTL_MARGIN))
                .requestTimeout(REDIS_REQUEST_TIMEOUT)
                .build()
                .withMapper(key -> key.getBytes(StandardCharsets.UTF_8));
    }
}
