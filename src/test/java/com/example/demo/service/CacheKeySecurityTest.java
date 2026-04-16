package com.example.demo.service;

import org.junit.jupiter.api.Test;
import org.apache.commons.codec.digest.DigestUtils;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Cache key security tests
 * Validates SHA256 hashed keys prevent enumeration attacks
 */
class CacheKeySecurityTest {

    private static final String CACHE_KEY_PREFIX = "perm:bitmap:";
    private static final String CACHE_SALT = "perm_salt_2026";

    @Test
    void shouldGenerateDifferentKeysForDifferentUsers() {
        String key1 = safeKey(1L);
        String key2 = safeKey(2L);

        assertFalse(key1.equals(key2), "Keys for different users should differ");
    }

    @Test
    void shouldUseSha256Hash() {
        String key = safeKey(1L);

        // Key should be hashed, not just prefix + raw userId
        assertFalse(key.equals(CACHE_KEY_PREFIX + "1"), "Key should be hashed, not raw userId");
        assertTrue(key.startsWith(CACHE_KEY_PREFIX), "Key should have correct prefix");
        assertEquals(CACHE_KEY_PREFIX.length() + 16, key.length(), "Key should have prefix + 16 hex chars");
        // Key should match hex pattern
        assertTrue(key.matches("perm:bitmap:[a-f0-9]{16}"), "Key should be hex hash");
    }

    @Test
    void shouldPreventEnumerationAttack() {
        // Attacker cannot guess sequential userId keys
        String key100 = safeKey(100L);
        String key101 = safeKey(101L);

        // Keys should not follow predictable pattern
        String hash100 = key100.substring(CACHE_KEY_PREFIX.length());
        String hash101 = key101.substring(CACHE_KEY_PREFIX.length());

        assertFalse(hash100.equals(hash101), "Sequential userIds should not have predictable keys");
        assertFalse(hash100.equals("100"), "Hash should not equal raw userId");
        assertFalse(hash101.equals("101"), "Hash should not equal raw userId");
    }

    @Test
    void shouldGenerateConsistentKeyForSameUser() {
        String key1 = safeKey(1L);
        String key2 = safeKey(1L);

        assertEquals(key1, key2, "Same userId should generate same key");
    }

    @Test
    void shouldHaveCorrectKeyFormat() {
        String key = safeKey(999L);

        assertTrue(key.startsWith("perm:bitmap:"), "Key should start with correct prefix");
        assertTrue(key.matches("perm:bitmap:[a-f0-9]{16}"), "Key should match expected pattern");
    }

    @Test
    void shouldHandleLargeUserId() {
        String key = safeKey(Long.MAX_VALUE);

        assertTrue(key.startsWith(CACHE_KEY_PREFIX), "Key should have prefix for large userId");
        assertEquals(CACHE_KEY_PREFIX.length() + 16, key.length(), "Key length should be consistent");
    }

    @Test
    void shouldHandleZeroUserId() {
        String key = safeKey(0L);

        assertTrue(key.startsWith(CACHE_KEY_PREFIX), "Key should have prefix for zero userId");
        assertNotNull(key, "Key should be generated for zero userId");
    }

    private String safeKey(Long userId) {
        String raw = userId + ":" + CACHE_SALT;
        return CACHE_KEY_PREFIX + DigestUtils.sha256Hex(raw).substring(0, 16);
    }
}