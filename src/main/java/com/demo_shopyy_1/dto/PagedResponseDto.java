package com.demo_shopyy_1.dto;

import lombok.Data;

import java.util.List;

@Data
public class PagedResponseDto<T> {
    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
}
