package com.music.utils;
public class UserContext {
    // 字节面试必考：ThreadLocal 就像一个线程内部的保险柜
    private static final ThreadLocal<Integer> userHolder = new ThreadLocal<>();

    public static void setUserId(Integer userId) {
        userHolder.set(userId);
    }

    public static Integer getUserId() {
        return (Integer) ThreadLocalUtil.get();
    }

    public static void remove() {
// ⚠️ 极其重要：用完必须清理，否则会发生“内存泄漏”，字节面试必问！
        userHolder.remove();
    }
}