package com.vn.baseapis.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

public class CommonUtils {
    public static Locale getLocaleResolver(HttpServletRequest request) {
        try {
            Locale locale;
            String lang = request.getHeader("lang");
            if (StringUtils.isNotEmpty(lang)) {
                locale = new Locale(lang);
            } else {
                LocaleResolver localeResolver = (LocaleResolver) request.getAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE);
                locale = localeResolver.resolveLocale(request);
            }
            return locale;
        } catch (Exception e) {
            return Locale.forLanguageTag("vi");
        }

    }

    public static String getLocaleLanguage(HttpServletRequest request) {
        try {
            Locale locale;
            String lang = request.getHeader("lang");
            if (StringUtils.isNotEmpty(lang)) {
                locale = new Locale(lang);
            } else {
                LocaleResolver localeResolver = (LocaleResolver) request.getAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE);
                locale = localeResolver.resolveLocale(request);
            }
            return locale.getLanguage();
        } catch (Exception e) {
            return "vi";
        }
    }

    public static String getMessage(MessageSource messageSource, HttpServletRequest request, String key, Object[] params) {
        try {
            Locale locale = CommonUtils.getLocaleResolver(request);
            return messageSource.getMessage(key, params, locale);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getMessage(MessageSource messageSource, HttpServletRequest request, String key) {
        try {
            final Locale locale = CommonUtils.getLocaleResolver(request);
            return messageSource.getMessage(key, null, locale);
        } catch (Exception e) {
            return null;
        }
    }

    public static final String[] IP_HEADER_CANDIDATES = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"};

    public static String retrieveClientIpAddress(HttpServletRequest request) {
        for (String header : IP_HEADER_CANDIDATES) {
            String ip = request.getHeader(header);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }
        }
        return request.getRemoteAddr();
    }

    public static String retrieveClientIpAddressV2(HttpServletRequest request) {
        String ip = request.getHeader("X-Original-Forwarded-For");
        if (StringUtils.isNotBlank(ip)) {
            String[] ips = ip.replaceAll("\\s+","") .split(",");
            if (ips.length > 0) {
                return ips[ips.length - 1];
            }
        }
        return request.getRemoteAddr();
    }

    public static String hideString(String value) {
        if (StringUtils.isBlank(value)) return value;

        int length = value.length();
        int hiddenLength = length / 3;

        if (hiddenLength == 3) {
            return "******" + value.substring(length-4);
        }

        if (hiddenLength < 3) {
            return value.substring(0, hiddenLength) + "***" + value.substring(length - hiddenLength);
        }

        return value.substring(0, hiddenLength) + "******" + value.substring(length - hiddenLength);
    }

}
