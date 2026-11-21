package com.music.dto;


import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Data
@Schema(description = "Page Query Object")
@Getter
public class pageQuery {
    @Schema(description = "Page Number")
    private int pageNum;
    @Schema(description = "Page Size")
    private int pageSize;
    @Schema(description = "排序字段")
    private String orderBy;
    @Schema(description = "是否升序")
    private boolean isAsc;
}
