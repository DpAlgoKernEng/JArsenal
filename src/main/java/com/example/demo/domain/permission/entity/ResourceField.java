package com.example.demo.domain.permission.entity;

import com.example.demo.domain.permission.valueobject.SensitiveLevel;
import com.example.demo.domain.shared.common.BaseEntity;

/**
 * 资源字段实体
 * 用于定义资源的敏感字段及脱敏规则
 */
public class ResourceField extends BaseEntity<Long> {

    private Long resourceId;
    private String fieldCode;
    private String fieldName;
    private SensitiveLevel sensitiveLevel;
    private String maskPattern;

    /**
     * 脱敏处理
     */
    public Object maskValue(Object originalValue) {
        if (originalValue == null) {
            return null;
        }

        if (sensitiveLevel == SensitiveLevel.HIDDEN) {
            return null;
        }

        if (sensitiveLevel == SensitiveLevel.ENCRYPTED && maskPattern != null) {
            String strValue = originalValue.toString();
            return switch (maskPattern) {
                case "ID_CARD" -> maskIdCard(strValue);
                case "PHONE" -> maskPhone(strValue);
                case "SALARY" -> maskSalary(strValue);
                default -> strValue;
            };
        }

        return originalValue;
    }

    private String maskIdCard(String value) {
        if (value.length() < 18) {
            return value;
        }
        return value.substring(0, 6) + "********" + value.substring(14);
    }

    private String maskPhone(String value) {
        if (value.length() < 11) {
            return value;
        }
        return value.substring(0, 3) + "****" + value.substring(7);
    }

    private String maskSalary(String value) {
        return "***";
    }

    // Getter/Setter

    @Override
    public Long getId() {
        return super.getId();
    }

    public void setId(Long id) {
        super.setId(id);
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public String getFieldCode() {
        return fieldCode;
    }

    public void setFieldCode(String fieldCode) {
        this.fieldCode = fieldCode;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public SensitiveLevel getSensitiveLevel() {
        return sensitiveLevel;
    }

    public void setSensitiveLevel(SensitiveLevel sensitiveLevel) {
        this.sensitiveLevel = sensitiveLevel;
    }

    public String getMaskPattern() {
        return maskPattern;
    }

    public void setMaskPattern(String maskPattern) {
        this.maskPattern = maskPattern;
    }
}