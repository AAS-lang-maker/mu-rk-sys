package com.music.pojo;

import lombok.Data;

@Data
public class Category {
    private Integer categoryId;
    private String categoryName;
    private Integer categoryStatus;
    private Integer categorySort;
}
