package com.jguard.domain.permission.valueobject;

import com.jguard.domain.shared.exception.DomainException;
import java.util.regex.Pattern;

public record RoleCode(String value) {

    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_]{1,49}$");

    public RoleCode {
        if (value == null || value.isBlank()) {
            throw new DomainException("角色编码不能为空");
        }
        if (!CODE_PATTERN.matcher(value).matches()) {
            throw new DomainException("角色编码格式错误：需2-50字符，大写字母开头，仅含字母数字下划线");
        }
    }
}