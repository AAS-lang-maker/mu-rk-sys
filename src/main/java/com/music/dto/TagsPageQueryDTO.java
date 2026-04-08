package com.music.dto;

import lombok.*;

import java.io.Serializable;

@Data

public class TagsPageQueryDTO implements Serializable {
    private String TagsName;
    private Integer page ;
    private Integer pageSize;

}
