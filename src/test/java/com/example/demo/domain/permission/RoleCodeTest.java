package com.example.demo.domain.permission;

import com.example.demo.domain.permission.valueobject.RoleCode;
import com.example.demo.domain.permission.exception.DomainException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RoleCodeTest {

    @Test
    void shouldAcceptValidCode() {
        RoleCode code = new RoleCode("ADMIN");
        assertEquals("ADMIN", code.value());
    }

    @Test
    void shouldRejectNullCode() {
        assertThrows(DomainException.class, () -> new RoleCode(null));
    }

    @Test
    void shouldRejectBlankCode() {
        assertThrows(DomainException.class, () -> new RoleCode(""));
        assertThrows(DomainException.class, () -> new RoleCode("   "));
    }

    @Test
    void shouldRejectInvalidFormat() {
        assertThrows(DomainException.class, () -> new RoleCode("admin"));  // 小写
        assertThrows(DomainException.class, () -> new RoleCode("1ADMIN")); // 数字开头
        assertThrows(DomainException.class, () -> new RoleCode("A"));      // 太短
    }
}