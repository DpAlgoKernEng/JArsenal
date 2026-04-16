package com.example.demo.domain.permission.entity;

import com.example.demo.domain.permission.valueobject.SensitiveLevel;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ResourceFieldMaskingTest {

    private ResourceField createField(SensitiveLevel level, String maskPattern) {
        ResourceField field = new ResourceField();
        field.setSensitiveLevel(level);
        field.setMaskPattern(maskPattern);
        return field;
    }

    @Test
    void shouldReturnNullForHiddenField() {
        ResourceField field = createField(SensitiveLevel.HIDDEN, null);

        assertNull(field.maskValue("sensitive data"));
        assertNull(field.maskValue(12345));
    }

    @Test
    void shouldMaskIdCard() {
        ResourceField field = createField(SensitiveLevel.ENCRYPTED, "ID_CARD");

        String idCard = "320102199001011234";
        String masked = (String) field.maskValue(idCard);

        assertEquals("320102********1234", masked);
    }

    @Test
    void shouldMaskIdCardWithShortLength() {
        ResourceField field = createField(SensitiveLevel.ENCRYPTED, "ID_CARD");

        String shortId = "12345";
        String masked = (String) field.maskValue(shortId);

        // Less than 18 chars, returns original
        assertEquals("12345", masked);
    }

    @Test
    void shouldMaskPhone() {
        ResourceField field = createField(SensitiveLevel.ENCRYPTED, "PHONE");

        String phone = "13812345678";
        String masked = (String) field.maskValue(phone);

        assertEquals("138****5678", masked);
    }

    @Test
    void shouldMaskPhoneWithShortLength() {
        ResourceField field = createField(SensitiveLevel.ENCRYPTED, "PHONE");

        String shortPhone = "123";
        String masked = (String) field.maskValue(shortPhone);

        // Less than 11 chars, returns original
        assertEquals("123", masked);
    }

    @Test
    void shouldMaskSalary() {
        ResourceField field = createField(SensitiveLevel.ENCRYPTED, "SALARY");

        String salary = "50000";
        String masked = (String) field.maskValue(salary);

        assertEquals("***", masked);
    }

    @Test
    void shouldReturnOriginalForNormalLevel() {
        ResourceField field = createField(SensitiveLevel.NORMAL, null);

        String value = "normal data";
        String masked = (String) field.maskValue(value);

        assertEquals("normal data", masked);
    }

    @Test
    void shouldReturnNullForNullInput() {
        ResourceField field = createField(SensitiveLevel.ENCRYPTED, "PHONE");

        assertNull(field.maskValue(null));
    }

    @Test
    void shouldReturnOriginalForUnknownMaskPattern() {
        ResourceField field = createField(SensitiveLevel.ENCRYPTED, "UNKNOWN");

        String value = "test data";
        String masked = (String) field.maskValue(value);

        assertEquals("test data", masked);
    }

    @Test
    void shouldHandleNonStringValue() {
        ResourceField field = createField(SensitiveLevel.ENCRYPTED, "SALARY");

        Integer value = 12345;
        String masked = (String) field.maskValue(value);

        // Salary is always masked to ***
        assertEquals("***", masked);
    }
}