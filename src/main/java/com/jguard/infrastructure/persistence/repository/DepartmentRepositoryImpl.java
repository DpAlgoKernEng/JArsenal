package com.jguard.infrastructure.persistence.repository;

import com.jguard.domain.permission.repository.DepartmentRepository;
import com.jguard.infrastructure.persistence.mapper.DepartmentMapper;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 部门仓储实现
 */
@Repository
public class DepartmentRepositoryImpl implements DepartmentRepository {

    private final DepartmentMapper mapper;

    public DepartmentRepositoryImpl(DepartmentMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Set<Long> findAllSubDeptIds(Long deptId) {
        if (deptId == null) {
            return Set.of();
        }
        return mapper.findAllSubDeptIds(deptId);
    }

    @Override
    public String getDeptPath(Long deptId) {
        if (deptId == null) {
            return "";
        }

        List<Map<String, Object>> path = mapper.getDeptPath(deptId);

        // 构建路径字符串：顶级部门/二级部门/三级部门
        return path.stream()
            .map(m -> (String) m.get("name"))
            .reduce((a, b) -> a + "/" + b)
            .orElse("");
    }

    @Override
    public boolean existsById(Long deptId) {
        return mapper.existsById(deptId) > 0;
    }
}