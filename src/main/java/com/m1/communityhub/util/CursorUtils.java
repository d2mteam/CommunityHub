package com.m1.communityhub.util;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

public final class CursorUtils {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private CursorUtils() {
    }

    public static String encode(OffsetDateTime createdAt, Long id) {
        if (createdAt == null || id == null) {
            return null;
        }
        String value = FORMATTER.format(createdAt) + "|" + id;
        return Base64.getUrlEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    public static Cursor decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        String decoded = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
        String[] parts = decoded.split("\\|");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid cursor");
        }
        OffsetDateTime createdAt = OffsetDateTime.parse(parts[0], FORMATTER);
        Long id = Long.parseLong(parts[1]);
        return new Cursor(createdAt, id);
    }

    public record Cursor(OffsetDateTime createdAt, Long id) {
    }
}
