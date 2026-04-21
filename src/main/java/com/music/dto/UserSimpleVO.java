package com.music.dto;

import com.music.pojo.UserFollow;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSimpleVO {
    private String username;
    private Integer userId;
    private List<UserFollow> followList;
    private List<String> fanList;
    private List<String> masterList;
}
