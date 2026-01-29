package com.music.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor // 放在类上面，正确
@NoArgsConstructor
public class UserInfo { // 注意类名首字母大写，符合Java规范
    private Integer id;
    private String username;
    private String password;
    private LocalDateTime createTime;
}