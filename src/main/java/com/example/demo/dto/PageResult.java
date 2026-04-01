package com.example.demo.dto;

import lombok.Data;
import java.util.List;

/**
 * 分页响应结果
 */
@Data
public class PageResult<T> {
    private List<T> list;          // 数据列表
    private Long total;            // 总记录数
    private Integer pages;         // 总页数
    private Integer pageNum;       // 当前页
    private Integer pageSize;      // 每页大小
    private Boolean hasNextPage;   // 是否有下一页
    private Boolean hasPreviousPage; // 是否有上一页

    public static <T> PageResult<T> of(List<T> list, Long total, Integer pages,
                                        Integer pageNum, Integer pageSize) {
        PageResult<T> result = new PageResult<>();
        result.setList(list);
        result.setTotal(total);
        result.setPages(pages);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setHasNextPage(pageNum < pages);
        result.setHasPreviousPage(pageNum > 1);
        return result;
    }
}