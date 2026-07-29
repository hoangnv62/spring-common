package com.vn.baseapis.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.Instant;

/**
 * Lớp nền cho entity: chứa sẵn 4 field auditing được Spring Data JPA tự điền.
 * Giá trị "người thao tác" lấy từ {@link com.vn.baseapis.security.SpringSecurityAuditorAware}.
 *
 * <p>Chỉ cần {@code extends BaseEntity}; auditing hoạt động nhờ {@link AuditingEntityListener}
 * đã khai báo ở đây (JPA Auditing đã được bật trong DatabaseConfiguration).
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Serializable {

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @CreatedDate
    @Column(name = "created_date", updatable = false)
    private Instant createdDate;

    @LastModifiedBy
    @Column(name = "last_modified_by")
    private Long lastModifiedBy;

    @LastModifiedDate
    @Column(name = "last_modified_date")
    private Instant lastModifiedDate;
}
