package com.music.dto;


import lombok.Data;

@Data
public class BattleReport {
    // BattleReport.java
        // 消息类型：SUCCESS, FAIL, INFO, WARNING
        private String type;
        // 标题
        private String title;
        // 详细内容
        private String detail;
        // 时间戳（可选）
        private String timestamp;

        // 构造函数
        public BattleReport(String type, String title, String detail) {
            this.type = type;
            this.title = title;
            this.detail = detail;
            this.timestamp = java.time.LocalTime.now().toString();
        }

    }
