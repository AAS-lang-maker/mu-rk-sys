package com.music.utils;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code;
    private String msg=" ";
    private T data;


    //三种数据类型，组合起来是后端对前端请求的结果响应
    public static <T> Result<T> success() {
        Result<T>result=new Result<>();//无参数的成功
        result.setCode(200);
        result.setMsg("success");
        return result;
    }
    public static <T> Result<T> success(T date) {
        Result<T>result=new Result<>();//有数据的业务成功
        result.setCode(200);
        result.setMsg("success");
        result.setData(date);
        return  result;
    }
    public static <T>Result<T> error(String msg) {//失败
        Result<T>result=new Result<>();
        result.setCode(500);
        result.setMsg(msg==null?"success":msg);
        return result;
    }
    public static <T> Result<T> badRequest(String msg) {
        Result<T>result=new Result<>();//参数错误（登录注册）
        result.setCode(400);
        result.setMsg(msg==null?"success":msg);//加了点前端修饰就把我后端代码改成了这样的屎。
        return result;
    }
}
