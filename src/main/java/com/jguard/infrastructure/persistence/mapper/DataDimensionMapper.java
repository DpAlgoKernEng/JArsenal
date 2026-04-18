package com.jguard.infrastructure.persistence.mapper;

import com.jguard.domain.permission.aggregate.DataDimension;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Optional;

/**
 * 数据维度 Mapper
 */
@Mapper
public interface DataDimensionMapper {

    @Select("SELECT * FROM data_dimension WHERE code = #{code} AND status = 1")
    Optional<DataDimension> findByCode(@Param("code") String code);

    @Select("SELECT * FROM data_dimension WHERE status = 1")
    List<DataDimension> findAllEnabled();

    @Select("SELECT * FROM data_dimension WHERE id = #{id}")
    Optional<DataDimension> findById(@Param("id") Long id);

    @Insert("INSERT INTO data_dimension(code, name, description, source_table, source_column, status) " +
            "VALUES(#{code}, #{name}, #{description}, #{sourceTable}, #{sourceColumn}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DataDimension dimension);

    @Update("UPDATE data_dimension SET name=#{name}, description=#{description}, " +
            "source_table=#{sourceTable}, source_column=#{sourceColumn}, status=#{status} WHERE id=#{id}")
    int update(DataDimension dimension);
}