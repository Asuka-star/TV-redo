package com.wenjia.common.enumeration;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum CommentType {
    Shop(0),Post(1);
    @EnumValue
    private final int value;
    CommentType(int value){
        this.value=value;
    }
}
