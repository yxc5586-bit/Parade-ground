package com.cyx.paradegroundbackend.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<T> records;

    private long total;

    private long current;

    private long pageSize;

    private long pages;

    public static <T> PageResponse<T> of(IPage<?> page, List<T> records) {
        return new PageResponse<>(
                records,
                page.getTotal(),
                page.getCurrent(),
                page.getSize(),
                page.getPages()
        );
    }

    public static <T> PageResponse<T> fromPage(IPage<T> page) {
        return new PageResponse<>(
                page.getRecords(),
                page.getTotal(),
                page.getCurrent(),
                page.getSize(),
                page.getPages()
        );
    }
}
