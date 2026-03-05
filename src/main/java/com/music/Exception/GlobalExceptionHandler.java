package com.music.Exception;

import com.music.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice // 这个注解就是“大管家”的身份标识
public class GlobalExceptionHandler {

    // 这个方法专门负责抓取所有的 Exception 异常
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
// 1. 先在控制台打印具体的错误，方便我们调试
        log.error("程序出事啦！原因：{}", e.getMessage());
        e.printStackTrace();

// 2. 统一给前端返回一个漂亮的 Result 对象
// 这样前端就不会看到白屏或者 500 报错，而是看到具体的错误提示
        return Result.error("服务器开小差了：" + e.getMessage());
    }
    @ExceptionHandler(ServiceException.class)
    public Result handleServiceException(ServiceException e) {
// 拦截器抛出的“请先登录”，会被这里精准抓到
        return Result.error(e.getMessage());
    }
}
