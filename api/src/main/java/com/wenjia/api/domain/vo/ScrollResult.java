package com.wenjia.api.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScrollResult<T> implements Serializable {
    private Long cursor;
    private Integer offset;
    private List<T> records;
}
