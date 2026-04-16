package com.example.demo.domain.permission.aggregate;

import com.example.demo.domain.shared.common.BaseEntity;

/**
 * 数据维度聚合根
 * 定义数据权限的维度来源（如部门、项目、客户等）
 */
public class DataDimension extends BaseEntity<Long> {

    private String code;
    private String name;
    private String description;
    private String sourceTable;
    private String sourceColumn;
    private boolean status;

    /**
     * 工厂方法：创建数据维度
     */
    public static DataDimension create(String code, String name, String sourceTable, String sourceColumn) {
        DataDimension dimension = new DataDimension();
        dimension.code = code;
        dimension.name = name;
        dimension.sourceTable = sourceTable;
        dimension.sourceColumn = sourceColumn;
        dimension.status = true;
        return dimension;
    }

    /**
     * 禁用维度
     */
    public void disable() {
        this.status = false;
    }

    /**
     * 启用维度
     */
    public void enable() {
        this.status = true;
    }

    // Getter/Setter

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

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSourceTable() {
        return sourceTable;
    }

    public void setSourceTable(String sourceTable) {
        this.sourceTable = sourceTable;
    }

    public String getSourceColumn() {
        return sourceColumn;
    }

    public void setSourceColumn(String sourceColumn) {
        this.sourceColumn = sourceColumn;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}