package com.vn.baseapis.security;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class RequesterInfo implements Serializable {
    private String requesterCode;
    private String requesterName;
    private long expirationInMillis = 0;
}
