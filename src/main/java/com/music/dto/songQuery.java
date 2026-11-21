package com.music.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Schema(description = "Song Query Object")
@EqualsAndHashCode(callSuper = true)
public class songQuery extends pageQuery {
    @Schema(description = "Song ID")
    private Integer songId;
    @Schema(description = "Song Name")
    private String songName;
    @Schema(description = "Singer ID")
    private Integer singerId;

}
