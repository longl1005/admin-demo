package com.example.admin.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PageResult<T> {
    private long total;
    private List<T> records;

    public static <T> PageResult<T> from(IPage<T> page) {
        return new PageResult<>(page.getTotal(), page.getRecords());
    }
}
