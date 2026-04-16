package com.example.demo.service;

import com.example.demo.domain.permission.valueobject.PermissionBitmap;
import com.example.demo.domain.permission.valueobject.ActionType;
import com.example.demo.infrastructure.persistence.serializer.PermissionBitmapSerializer;
import com.example.demo.infrastructure.persistence.serializer.PermissionBitmapDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

/**
 * PermissionBitmap serialization/deserialization tests
 * Validates JSON serialization for Redis storage
 */
class PermissionBitmapSerializationTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setup() {
        mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule()
            .addSerializer(PermissionBitmap.class, new PermissionBitmapSerializer())
            .addDeserializer(PermissionBitmap.class, new PermissionBitmapDeserializer()));
    }

    @Test
    void shouldSerializeAndDeserializeCorrectly() {
        PermissionBitmap original = PermissionBitmap.empty(1234567890L)
            .addPermission(1L, Set.of(ActionType.VIEW, ActionType.CREATE))
            .addPermission(2L, Set.of(ActionType.UPDATE));

        // Serialize
        String json = serializeToJson(original);

        // Deserialize
        PermissionBitmap deserialized = deserializeFromJson(json);

        // Verify version preserved
        assertEquals(original.getVersion(), deserialized.getVersion(), "Version should be preserved");

        // Verify permissions preserved
        assertTrue(deserialized.hasAction(1L, ActionType.VIEW), "VIEW action should be preserved");
        assertTrue(deserialized.hasAction(1L, ActionType.CREATE), "CREATE action should be preserved");
        assertFalse(deserialized.hasAction(1L, ActionType.DELETE), "DELETE action should not exist");
        assertTrue(deserialized.hasAction(2L, ActionType.UPDATE), "UPDATE action should be preserved");
        assertFalse(deserialized.hasAction(2L, ActionType.VIEW), "VIEW action should not exist on resource 2");
    }

    @Test
    void shouldSerializeEmptyBitmap() {
        PermissionBitmap empty = PermissionBitmap.empty(System.currentTimeMillis());

        String json = serializeToJson(empty);
        PermissionBitmap deserialized = deserializeFromJson(json);

        assertTrue(deserialized.getActionBits().isEmpty(), "Empty bitmap should remain empty");
        assertFalse(deserialized.hasAction(1L, ActionType.VIEW), "Empty bitmap should have no actions");
    }

    @Test
    void shouldPreserveBitSetRepresentation() {
        PermissionBitmap bitmap = PermissionBitmap.empty()
            .addPermission(1L, Set.of(ActionType.VIEW, ActionType.CREATE, ActionType.UPDATE, ActionType.DELETE));

        String json = serializeToJson(bitmap);

        // BitSet should be binary string "1111" (VIEW=0, CREATE=1, UPDATE=2, DELETE=3)
        assertTrue(json.contains("\"1111\"") || json.contains("\"11110\""), "BitSet should be binary string");

        PermissionBitmap deserialized = deserializeFromJson(json);

        // Verify all action permissions exist
        assertTrue(deserialized.hasAction(1L, ActionType.VIEW), "VIEW should exist");
        assertTrue(deserialized.hasAction(1L, ActionType.CREATE), "CREATE should exist");
        assertTrue(deserialized.hasAction(1L, ActionType.UPDATE), "UPDATE should exist");
        assertTrue(deserialized.hasAction(1L, ActionType.DELETE), "DELETE should exist");
        assertFalse(deserialized.hasAction(1L, ActionType.EXECUTE), "EXECUTE should not exist");
    }

    @Test
    void shouldHandleMultipleResources() {
        PermissionBitmap bitmap = PermissionBitmap.empty()
            .addPermission(1L, Set.of(ActionType.VIEW))
            .addPermission(2L, Set.of(ActionType.CREATE, ActionType.UPDATE))
            .addPermission(3L, Set.of(ActionType.DELETE));

        String json = serializeToJson(bitmap);
        PermissionBitmap deserialized = deserializeFromJson(json);

        // Verify each resource has independent permissions
        assertTrue(deserialized.hasAction(1L, ActionType.VIEW), "Resource 1 should have VIEW");
        assertFalse(deserialized.hasAction(1L, ActionType.CREATE), "Resource 1 should not have CREATE");

        assertTrue(deserialized.hasAction(2L, ActionType.CREATE), "Resource 2 should have CREATE");
        assertTrue(deserialized.hasAction(2L, ActionType.UPDATE), "Resource 2 should have UPDATE");
        assertFalse(deserialized.hasAction(2L, ActionType.DELETE), "Resource 2 should not have DELETE");

        assertTrue(deserialized.hasAction(3L, ActionType.DELETE), "Resource 3 should have DELETE");
        assertFalse(deserialized.hasAction(3L, ActionType.VIEW), "Resource 3 should not have VIEW");
    }

    @Test
    void shouldSerializeToJsonFormat() {
        // Create empty bitmap with specific version (no addPermission to preserve version)
        PermissionBitmap bitmap = PermissionBitmap.empty(100L);

        String json = serializeToJson(bitmap);

        // Verify JSON structure
        assertTrue(json.contains("\"version\""), "Version field should be in JSON");
        assertTrue(json.contains("\"actionBits\""), "actionBits should be in JSON");
        // For empty bitmap, actionBits should be empty object
        assertTrue(json.contains("\"actionBits\":{}") || json.contains("\"actionBits\" : {}"), "Empty actionBits should be empty object");
    }

    @Test
    void shouldRoundTripPreserveAllData() {
        // Create bitmap with various permissions
        PermissionBitmap original = PermissionBitmap.empty(999L)
            .addPermission(10L, Set.of(ActionType.VIEW, ActionType.EXECUTE))
            .addPermission(20L, Set.of(ActionType.CREATE))
            .addPermission(30L, Set.of(ActionType.UPDATE, ActionType.DELETE, ActionType.EXECUTE));

        // Round trip through serialization
        String json = serializeToJson(original);
        PermissionBitmap restored = deserializeFromJson(json);

        // Verify all data preserved
        assertEquals(original.getVersion(), restored.getVersion(), "Version preserved");
        assertEquals(original.getActionBits().keySet(), restored.getActionBits().keySet(), "Resource keys preserved");

        // Verify each permission
        assertTrue(restored.hasAction(10L, ActionType.VIEW));
        assertTrue(restored.hasAction(10L, ActionType.EXECUTE));
        assertFalse(restored.hasAction(10L, ActionType.CREATE));

        assertTrue(restored.hasAction(20L, ActionType.CREATE));
        assertFalse(restored.hasAction(20L, ActionType.VIEW));

        assertTrue(restored.hasAction(30L, ActionType.UPDATE));
        assertTrue(restored.hasAction(30L, ActionType.DELETE));
        assertTrue(restored.hasAction(30L, ActionType.EXECUTE));
    }

    private String serializeToJson(PermissionBitmap bitmap) {
        try {
            return mapper.writeValueAsString(bitmap);
        } catch (Exception e) {
            throw new RuntimeException("Serialization failed", e);
        }
    }

    private PermissionBitmap deserializeFromJson(String json) {
        try {
            return mapper.readValue(json, PermissionBitmap.class);
        } catch (Exception e) {
            throw new RuntimeException("Deserialization failed", e);
        }
    }
}