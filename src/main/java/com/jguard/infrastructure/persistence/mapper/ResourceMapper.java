package com.jguard.infrastructure.persistence.mapper;

import com.jguard.domain.permission.aggregate.Resource;
import com.jguard.domain.permission.valueobject.ResourceType;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * Resource Mapper Interface
 * MyBatis mapper for Resource aggregate persistence
 */
@Mapper
public interface ResourceMapper {

    @Insert("INSERT INTO resource(code, name, parent_id, type, path, path_pattern, method, icon, component, sort, status, is_deleted, data_dimension_code) " +
            "VALUES(#{code}, #{name}, #{parentId}, #{type}, #{path}, #{pathPattern}, #{method}, #{icon}, #{component}, #{sort}, #{status}, #{isDeleted}, #{dataDimensionCode})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Resource resource);

    @Update("UPDATE resource SET name=#{name}, parent_id=#{parentId}, path=#{path}, " +
            "path_pattern=#{pathPattern}, method=#{method}, icon=#{icon}, component=#{component}, " +
            "sort=#{sort}, status=#{status}, data_dimension_code=#{dataDimensionCode} WHERE id=#{id}")
    int update(Resource resource);

    @Select("SELECT * FROM resource WHERE id = #{id} AND is_deleted = 0")
    @Results(id = "resourceResult", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "code", column = "code"),
            @Result(property = "name", column = "name"),
            @Result(property = "parentId", column = "parent_id"),
            @Result(property = "type", column = "type", typeHandler = com.jguard.infrastructure.persistence.converter.ResourceTypeTypeHandler.class),
            @Result(property = "path", column = "path"),
            @Result(property = "pathPattern", column = "path_pattern"),
            @Result(property = "method", column = "method"),
            @Result(property = "icon", column = "icon"),
            @Result(property = "component", column = "component"),
            @Result(property = "sort", column = "sort"),
            @Result(property = "status", column = "status"),
            @Result(property = "isDeleted", column = "is_deleted"),
            @Result(property = "dataDimensionCode", column = "data_dimension_code")
    })
    Resource findById(@Param("id") Long id);

    @Select("SELECT * FROM resource WHERE code = #{code} AND is_deleted = 0")
    @ResultMap("resourceResult")
    Resource findByCode(@Param("code") String code);

    @Select("SELECT * FROM resource WHERE is_deleted = 0 ORDER BY sort")
    @ResultMap("resourceResult")
    List<Resource> findAll();

    @Select("SELECT * FROM resource WHERE type = #{type} AND is_deleted = 0 ORDER BY sort")
    @ResultMap("resourceResult")
    List<Resource> findByType(@Param("type") ResourceType type);

    @Select("SELECT * FROM resource WHERE parent_id = #{parentId} AND is_deleted = 0 ORDER BY sort")
    @ResultMap("resourceResult")
    List<Resource> findByParentId(@Param("parentId") Long parentId);

    @Select("SELECT * FROM resource WHERE type = 'API' AND is_deleted = 0 ORDER BY sort")
    @ResultMap("resourceResult")
    List<Resource> findAllApis();

    @Update("UPDATE resource SET is_deleted = 1 WHERE id = #{id}")
    int softDelete(@Param("id") Long id);

    @Delete("DELETE FROM resource WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}