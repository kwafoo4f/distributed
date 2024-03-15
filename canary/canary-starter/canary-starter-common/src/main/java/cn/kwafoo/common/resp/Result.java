package cn.kwafoo.common.resp;

import java.util.HashMap;

/**
 * @description:
 * @author: zk
 * @date: 2024-03-13
 */
public class Result<R> {
    private static final int OK_CODE = 200;
    private static final String OK_MSG = "ok";

    private int code;
    private String message;
    private R data;

    private Result() {
    }
    ;

    public static Result ok() {
        Result result = new Result<>();
        result.setCode(OK_CODE);
        result.setMessage(OK_MSG);
        result.setData(new HashMap<>());
        return result;
    }

    public static <R> Result<R> ok(R data) {
        Result<R> result = new Result<R>();
        result.setCode(OK_CODE);
        result.setMessage(OK_MSG);
        result.setData(data);
        return result;
    }

    public static Result fail(int code, String message) {
        Result result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(new HashMap<>());
        return result;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public R getData() {
        return data;
    }

    public void setData(R data) {
        this.data = data;
    }
}
