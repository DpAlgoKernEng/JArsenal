package com.example.demo.infrastructure.persistence.serializer;

import com.example.demo.domain.permission.valueobject.PermissionBitmap;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.BitSet;
import java.util.Map;

/**
 * PermissionBitmap JSON serializer for Redis storage
 * Converts PermissionBitmap to compact JSON format
 */
public class PermissionBitmapSerializer extends JsonSerializer<PermissionBitmap> {

    @Override
    public void serialize(PermissionBitmap value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();

        // Serialize version
        gen.writeNumberField("version", value.getVersion());

        // Serialize actionBits Map (convert BitSet to binary string)
        gen.writeObjectFieldStart("actionBits");
        Map<Long, BitSet> bits = value.getActionBits();
        for (Map.Entry<Long, BitSet> entry : bits.entrySet()) {
            gen.writeStringField(String.valueOf(entry.getKey()), bitsToString(entry.getValue()));
        }
        gen.writeEndObject();

        gen.writeEndObject();
    }

    private String bitsToString(BitSet bitSet) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bitSet.length(); i++) {
            sb.append(bitSet.get(i) ? '1' : '0');
        }
        return sb.toString();
    }
}