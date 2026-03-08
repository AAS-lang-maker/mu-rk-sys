package com.music.Exception;

import com.music.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice // 全局异常处理标识
public class GlobalExceptionHandler {

    // 1. 处理静态资源未找到异常（重点解决 .map 文件问题）
    @ExceptionHandler(NoResourceFoundException.class)
    public Result handleNoResourceException(NoResourceFoundException e) {
        String resourcePath = e.getResourcePath();
        // 如果是 .map 文件请求，直接返回成功（不报错、不打印冗余日志）
        if (resourcePath != null && resourcePath.endsWith(".map")) {
            log.debug("忽略 .map 文件请求：{}", resourcePath); // 用debug级别，不刷屏
            return Result.success(null); // 返回成功，前端无感知
        }
        // 其他静态资源未找到，正常打印错误并返回提示
        log.error("静态资源不存在：{}", e.getMessage());
        return Result.error("静态资源不存在：" + resourcePath);
    }

    // 2. 处理缺少请求参数异常（比如category参数缺失）
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result handleMissingParamException(MissingServletRequestParameterException e) {
        String paramName = e.getParameterName();
        String msg = "请求缺少必填参数：" + paramName;
        log.error(msg);
        return Result.error(msg);
    }

    // 3. 处理@Valid参数校验异常（比如DTO的@NotBlank校验失败）
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleValidException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldError().getDefaultMessage();
        log.error("参数校验失败：{}", msg);
        return Result.error(msg);
    }

    // 4. 处理自定义的ServiceException（原有逻辑保留）
    @ExceptionHandler(ServiceException.class)
    public Result handleServiceException(ServiceException e) {
        log.error("业务异常：{}", e.getMessage());
        return Result.error(e.getMessage());
    }

    // 5. 处理所有其他未捕获的异常（原有逻辑保留）
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        log.error("程序出错：{}", e.getMessage(), e); // 打印完整堆栈，方便调试
        e.printStackTrace();
        return Result.error("服务器开小差了：" + e.getMessage());
    }
}