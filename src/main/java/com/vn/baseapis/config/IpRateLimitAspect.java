package com.vn.baseapis.config;

import com.vn.baseapis.exception.InternalException;
import com.vn.baseapis.exception.TooManyRequestsException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.util.Objects;

/**
 * Rate limit theo IP cho các method có {@link IpRateLimited}.
 *
 * <p>Trạng thái bucket nằm trên Redis (xem {@link RateLimitConfig}) nên quota được chia sẻ giữa
 * mọi instance. TTL của key do Bucket4j tự quản lý, không cần cache cục bộ để dọn dẹp.
 *
 * <p><b>IP của client</b> lấy từ {@code request.getRemoteAddr()} và <b>không</b> tự đọc
 * {@code X-Forwarded-For}. Header này do client gửi nên tự parse là lỗ hổng: chỉ cần đổi header là
 * mỗi request thành một "IP" khác và lách được rate limit. Việc bóc tách được giao cho
 * Tomcat {@code RemoteIpValve} (bật bằng {@code server.forward-headers-strategy=native}) — valve chỉ
 * tin header khi peer kết nối trực tiếp nằm trong {@code server.tomcat.remoteip.internal-proxies},
 * nên header do người dùng cuối gửi sẽ bị bỏ qua.
 */
@Aspect
@Component
@Slf4j
public class IpRateLimitAspect {

    private final ProxyManager<String> rateLimitProxyManager;

    /**
     * Tiêm dạng {@code @Lazy} để app vẫn khởi động được khi Redis chưa sẵn sàng; kết nối chỉ được
     * mở ở lần kiểm tra rate limit đầu tiên và lỗi lúc đó rơi vào nhánh xử lý bên dưới.
     */
    public IpRateLimitAspect(@Lazy ProxyManager<String> rateLimitProxyManager) {
        this.rateLimitProxyManager = rateLimitProxyManager;
    }

    /**
     * Xử lý khi không truy vấn được Redis (mất kết nối, timeout).
     * <ul>
     *   <li>{@code true} (mặc định) — cho request đi qua và ghi log cảnh báo: ưu tiên tính khả dụng,
     *       đánh đổi là trong lúc Redis sự cố thì rate limit tạm thời không có hiệu lực.</li>
     *   <li>{@code false} — từ chối request (HTTP 500): ưu tiên siết chặt, đánh đổi là Redis sập
     *       sẽ làm mọi API có rate limit ngừng phục vụ.</li>
     * </ul>
     */
    @Value("${app.rate-limit.fail-open:true}")
    private boolean failOpen;

    @Around("@annotation(rateLimited)")
    public Object limitByIp(ProceedingJoinPoint joinPoint, IpRateLimited rateLimited) throws Throwable {

        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();

        String ip = request.getRemoteAddr();
        String api = request.getRequestURI();

        // Key duy nhất: prefix + IP + API endpoint
        String key = RateLimitConfig.KEY_PREFIX + ip + ":" + api;

        boolean allowed;
        try {
            allowed = rateLimitProxyManager
                    .getProxy(key, () -> newConfiguration(rateLimited))
                    .tryConsume(1);
        } catch (Exception ex) {
            // Chỉ bọc riêng lệnh gọi Redis: lỗi từ business logic bên dưới không bị hiểu nhầm thành lỗi Redis.
            if (failOpen) {
                log.warn("Không kiểm tra được rate limit trên Redis, tạm cho request đi qua (ip={}, api={}): {}",
                        ip, api, ex.toString());
                return joinPoint.proceed();
            }
            log.error("Không kiểm tra được rate limit trên Redis, từ chối request (ip={}, api={})", ip, api, ex);
            throw new InternalException();
        }

        if (allowed) {
            return joinPoint.proceed();
        } else {
            throw new TooManyRequestsException("Too many requests from IP " + ip + " to " + api);
        }
    }

    private BucketConfiguration newConfiguration(IpRateLimited limitConfig) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(limitConfig.limit())
                .refillGreedy(limitConfig.limit(), Duration.ofSeconds(limitConfig.durationSeconds()))
                .build();
        return BucketConfiguration.builder().addLimit(limit).build();
    }
}
