package com.example.demo.infrastructure.persistence.mapper;

import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 用户维度 Mapper
 * 存储用户与维度值的关联关系
 */
@Mapper
public interface UserDimensionMapper {

    /**
     * 获取用户指定维度的值
     */
    @Select("SELECT dimension_value_id FROM user_dimension WHERE user_id = #{userId} AND dimension_code = #{dimensionCode}")
    Long getValueByDimension(@Param("userId") Long userId, @Param("dimensionCode") String dimensionCode);

    /**
     * 获取用户指定维度的所有值（多值场景）
     */
    @Select("SELECT dimension_value_id FROM user_dimension WHERE user_id = #{userId} AND dimension_code = #{dimensionCode}")
    List<Long> findValuesByDimension(@Param("userId") Long userId, @Param("dimensionCode") String dimensionCode);

    /**
     * 分配用户维度值
     */
    @Insert("INSERT INTO user_dimension(user_id, dimension_code, dimension_value_id) " +
            "VALUES(#{userId}, #{dimensionCode}, #{valueId})")
    int insert(@Param("userId") Long userId, @Param("dimensionCode") String dimensionCode, @Param("valueId") Long valueId);

    /**
     * 删除用户指定维度值
     */
    @Delete("DELETE FROM user_dimension WHERE user_id = #{userId} AND dimension_code = #{dimensionCode}")
    int deleteByUserAndDimension(@Param("userId") Long userId, @Param("dimensionCode") String dimensionCode);

    /**
     * 删除用户所有维度值
     */
    @Delete("DELETE FROM user_dimension WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);
}