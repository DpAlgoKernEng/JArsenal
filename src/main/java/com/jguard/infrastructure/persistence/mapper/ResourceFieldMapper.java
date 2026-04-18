package com.jguard.infrastructure.persistence.mapper;

import com.jguard.domain.permission.entity.ResourceField;
import com.jguard.domain.permission.valueobject.SensitiveLevel;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 资源字段 Mapper
 */
@Mapper
public interface ResourceFieldMapper {

    @Insert("INSERT INTO resource_field(resource_id, field_code, field_name, sensitive_level, mask_pattern) " +
            "VALUES(#{resourceId}, #{fieldCode}, #{fieldName}, #{sensitiveLevel}, #{maskPattern})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ResourceField field);

    @Select("SELECT id, resource_id, field_code, field_name, sensitive_level, mask_pattern, create_time " +
            "FROM resource_field WHERE id = #{id}")
    @Results(id = "resourceFieldResult", value = {
        @Result(property = "id", column = "id"),
        @Result(property = "resourceId", column = "resource_id"),
        @Result(property = "fieldCode", column = "field_code"),
        @Result(property = "fieldName", column = "field_name"),
        @Result(property = "sensitiveLevel", column = "sensitive_level",
                typeHandler = com.jguard.infrastructure.persistence.converter.SensitiveLevelTypeHandler.class),
        @Result(property = "maskPattern", column = "mask_pattern")
    })
    ResourceField findById(@Param("id") Long id);

    @Select("SELECT id, resource_id, field_code, field_name, sensitive_level, mask_pattern, create_time " +
            "FROM resource_field WHERE resource_id = #{resourceId}")
    @ResultMap("resourceFieldResult")
    List<ResourceField> findByResourceId(@Param("resourceId") Long resourceId);

    @Select("SELECT id, resource_id, field_code, field_name, sensitive_level, mask_pattern, create_time " +
            "FROM resource_field WHERE resource_id = #{resourceId} AND field_code = #{fieldCode}")
    @ResultMap("resourceFieldResult")
    ResourceField findByResourceAndCode(@Param("resourceId") Long resourceId, @Param("fieldCode") String fieldCode);

    @Update("UPDATE resource_field SET sensitive_level=#{sensitiveLevel}, mask_pattern=#{maskPattern}, " +
            "field_name=#{fieldName} WHERE id=#{id}")
    int update(ResourceField field);

    @Delete("DELETE FROM resource_field WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Delete("DELETE FROM resource_field WHERE resource_id = #{resourceId}")
    int deleteByResourceId(@Param("resourceId") Long resourceId);
}