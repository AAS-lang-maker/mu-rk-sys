package com.music.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;

import java.time.LocalDateTime;

public class user {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class User{
        private Long id;
        private String username;
        private String password;
        private LocalDateTime createTime;
    }
}
