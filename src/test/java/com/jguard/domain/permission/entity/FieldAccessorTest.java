package com.jguard.domain.permission.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FieldAccessorTest {

    // Test DTO class
    static class TestDto {
        private String name;
        private Integer age;
        private Boolean active;

        public TestDto() {}

        public TestDto(String name, Integer age, Boolean active) {
            this.name = name;
            this.age = age;
            this.active = active;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }

        // No getter/setter for active - uses direct field access
    }

    @BeforeEach
    void clearCache() {
        FieldAccessor.clearCache();
    }

    @Test
    void shouldGetValueUsingGetter() {
        TestDto dto = new TestDto("test", 25, true);
        FieldAccessor accessor = FieldAccessor.get(TestDto.class, "name");

        assertEquals("test", accessor.getValue(dto));
    }

    @Test
    void shouldSetValueUsingSetter() {
        TestDto dto = new TestDto();
        FieldAccessor accessor = FieldAccessor.get(TestDto.class, "name");

        accessor.setValue(dto, "newValue");

        assertEquals("newValue", dto.getName());
    }

    @Test
    void shouldUseDirectAccessWhenNoGetterSetter() {
        TestDto dto = new TestDto("test", 25, true);
        FieldAccessor accessor = FieldAccessor.get(TestDto.class, "active");

        assertEquals(true, accessor.getValue(dto));
    }

    @Test
    void shouldSetUsingDirectAccessWhenNoSetter() {
        TestDto dto = new TestDto();
        FieldAccessor accessor = FieldAccessor.get(TestDto.class, "active");

        accessor.setValue(dto, false);

        assertFalse(dto.active);
    }

    @Test
    void shouldCacheAccessor() {
        FieldAccessor accessor1 = FieldAccessor.get(TestDto.class, "name");
        FieldAccessor accessor2 = FieldAccessor.get(TestDto.class, "name");

        // Same instance from cache
        assertSame(accessor1, accessor2);
    }

    @Test
    void shouldThrowForNonExistentField() {
        assertThrows(RuntimeException.class, () ->
            FieldAccessor.get(TestDto.class, "nonexistent")
        );
    }

    @Test
    void shouldReturnFieldType() {
        FieldAccessor nameAccessor = FieldAccessor.get(TestDto.class, "name");
        FieldAccessor ageAccessor = FieldAccessor.get(TestDto.class, "age");

        assertEquals(String.class, nameAccessor.getFieldType());
        assertEquals(Integer.class, ageAccessor.getFieldType());
    }

    @Test
    void shouldReturnFieldName() {
        FieldAccessor accessor = FieldAccessor.get(TestDto.class, "name");

        assertEquals("name", accessor.getFieldName());
    }
}