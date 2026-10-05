package com.vn.baseapis.dto.projection;

public interface UserProjection {
    Long getId();
    String getEmail();
    String getFullName();
    Integer getStatus();
    Integer getRole();
}
