package com.wenjia.gateway.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MyRequestPath {
    private String url;
    private MyRequestMethod methodType;

    @Override
    public int hashCode() {
        return Objects.hash(url, methodType);
    }

    /**
     * 重写equals方法，方便map中比对当前RequestPath是否已经存在
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MyRequestPath that = (MyRequestPath) o;
        return Objects.equals(url, that.url) && methodType == that.methodType;
    }
}
