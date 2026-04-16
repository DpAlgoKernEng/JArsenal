package com.example.demo.infrastructure.persistence.serializer;

import com.example.demo.domain.permission.valueobject.PermissionBitmap;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

/**
 * PermissionBitmap JSON deserializer for Redis storage
 * Converts JSON back to PermissionBitmap
 */
public class PermissionBitmapDeserializer extends JsonDeserializer<PermissionBitmap> {

    @Override
    public PermissionBitmap deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        // Parse version
        long version = node.has("version") ? node.get("version").asLong() : System.currentTimeMillis();

        // Parse actionBits Map
        Map<Long, BitSet> actionBits = new HashMap<>();
        JsonNode bitsNode = node.get("actionBits");
        if (bitsNode != null && bitsNode.isObject()) {
            bitsNode.fields().forEachRemaining(entry -> {
                Long resourceId = Long.parseLong(entry.getKey());
                BitSet bitSet = stringToBits(entry.getValue().asText());
                actionBits.put(resourceId, bitSet);
            });
        }

        return new PermissionBitmap(actionBits, version);
    }

    private BitSet stringToBits(String str) {
        BitSet bitSet = new BitSet();
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '1') {
                bitSet.set(i);
            }
        }
        return bitSet;
    }
}