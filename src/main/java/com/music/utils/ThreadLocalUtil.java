package com.music.utils;

/**
 * ThreadLocal 工具类：用来在同一个线程（请求）中存取用户信息
 */
public class ThreadLocalUtil {
    // 创建一个 ThreadLocal 容器
    private static final ThreadLocal<Object> THREAD_LOCAL = new ThreadLocal<>();

    // 存入数据（把 ID 往托盘放）
    public static void set(Object value) {
        THREAD_LOCAL.set(value);
    }

    // 获取数据（从托盘拿 ID）
    public static <T> T get() {
        return (T) THREAD_LOCAL.get();
    }

    // 清除数据（刷干净托盘，非常重要！）
    public static void remove() {
        THREAD_LOCAL.remove();
    }
}