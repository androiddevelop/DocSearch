package me.codeboy.doc.base;

/**
 * 通用请求
 * Created by yuedong.li on 2019/5/16
 */
public class CommonResult<T> {
    /**
     * 是否请求成功
     */
    private boolean success = true;
    /**
     * 请求失败的时候的信息
     */
    private String message;
    /**
     * 请求成功时的数据
     */
    private T data;

    public CommonResult() {
    }

    private CommonResult(T data) {
        this.data = data;
    }

    public CommonResult(boolean success, T data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
    }

    private CommonResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public CommonResult(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }


    public static <T> CommonResult success(T data) {
        return new CommonResult<>(data);
    }

    public static CommonResult failed(String message) {
        return new CommonResult<>(false, message);
    }

    public static <T> CommonResult failed(T data, String message) {
        return new CommonResult<>(false, data, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
