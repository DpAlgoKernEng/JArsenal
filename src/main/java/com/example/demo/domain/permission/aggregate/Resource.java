package com.example.demo.domain.permission.aggregate;

import com.example.demo.domain.permission.entity.ResourceField;
import com.example.demo.domain.permission.valueobject.ResourceType;
import com.example.demo.domain.permission.valueobject.SensitiveLevel;
import com.example.demo.domain.shared.common.BaseEntity;
import java.util.ArrayList;
import java.util.List;

/**
 * 资源聚合根
 * 支持菜单、操作、API三种资源类型
 */
public class Resource extends BaseEntity<Long> {

    private String code;
    private String name;
    private Long parentId;
    private ResourceType type;
    private String path;
    private String pathPattern;  // Ant风格路径模式
    private String method;       // HTTP方法（仅API类型）
    private String icon;
    private String component;    // 前端组件路径
    private int sort;
    private boolean status;      // true-启用
    private boolean isDeleted;
    private List<ResourceField> sensitiveFields;

    /**
     * 创建菜单资源
     */
    public static Resource createMenu(String code, String name, String path, String icon, String component) {
        Resource resource = new Resource();
        resource.code = code;
        resource.name = name;
        resource.type = ResourceType.MENU;
        resource.path = path;
        resource.icon = icon;
        resource.component = component;
        resource.status = true;
        resource.isDeleted = false;
        resource.sensitiveFields = new ArrayList<>();
        return resource;
    }

    /**
     * 创建操作资源
     */
    public static Resource createOperation(String code, String name, Long parentId) {
        Resource resource = new Resource();
        resource.code = code;
        resource.name = name;
        resource.parentId = parentId;
        resource.type = ResourceType.OPERATION;
        resource.status = true;
        resource.isDeleted = false;
        resource.sensitiveFields = new ArrayList<>();
        return resource;
    }

    /**
     * 创建API资源
     */
    public static Resource createApi(String code, String name, String pathPattern, String method) {
        Resource resource = new Resource();
        resource.code = code;
        resource.name = name;
        resource.type = ResourceType.API;
        resource.pathPattern = pathPattern;
        resource.method = method;
        resource.status = true;
        resource.isDeleted = false;
        resource.sensitiveFields = new ArrayList<>();
        return resource;
    }

    /**
     * 添加敏感字段
     */
    public void addSensitiveField(String fieldCode, String fieldName, SensitiveLevel level, String maskPattern) {
        ResourceField field = new ResourceField();
        field.setFieldCode(fieldCode);
        field.setFieldName(fieldName);
        field.setSensitiveLevel(level);
        field.setMaskPattern(maskPattern);
        field.setResourceId(this.getId());
        sensitiveFields.add(field);
    }

    /**
     * 软删除
     */
    public void softDelete() {
        this.isDeleted = true;
    }

    // Getter/Setter methods

    @Override
    public Long getId() {
        return super.getId();
    }

    public void setId(Long id) {
        super.setId(id);
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public ResourceType getType() {
        return type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPathPattern() {
        return pathPattern;
    }

    public String getMethod() {
        return method;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getComponent() {
        return component;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public List<ResourceField> getSensitiveFields() {
        return sensitiveFields;
    }

    public void setSensitiveFields(List<ResourceField> sensitiveFields) {
        this.sensitiveFields = sensitiveFields;
    }
}