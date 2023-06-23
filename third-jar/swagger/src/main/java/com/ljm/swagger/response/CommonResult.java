package com.ljm.swagger.response;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("entity for response")
public class CommonResult<T> {

    @ApiModelProperty(value = "code for response", example = "200")
    private long code;

    @ApiModelProperty(value = "message for response", example = "操作成功")
    private String message;

    @ApiModelProperty(value = "data for response")
    private T data;


    public long getCode() {
        return code;
    }

    public void setCode(long code) {
        this.code = code;
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

    public CommonResult() {
    }

    public CommonResult(long code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功返回结果
     *
     * @param data 获取的数据
     */
    public static <T> CommonResult<T> success(T data) {
        return new CommonResult<T>( ResultCode.SUCCESS.getCode(),  ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功返回结果
     *
     * @param data 获取的数据
     * @param  message 提示信息
     */
    public static <T> CommonResult<T> success(T data, String message) {
        return new CommonResult<T>( ResultCode.SUCCESS.getCode(), message, data);
    }

    /**
     * 失败返回结果
     * @param errorCode 错误码
     */
    public static <T> CommonResult<T> failed( IErrorCode errorCode) {
        return new CommonResult<T>(errorCode.getCode(), errorCode.getMessage(), null);
    }


    /**
     * 根据执行结果返回结果
     */
    public static <T> CommonResult<T> row(Boolean row) {
        if(!row)
            return failed( ResultCode.FAILED);
        else
            return new CommonResult<T>( ResultCode.SUCCESS.getCode(), "操作成功！", null);
    }

    /**
     * 根据执行结果返回结果
     */
    public static <T> CommonResult<T> row(int row) {
        if(row <= 0)
            return failed(ResultCode.FAILED);
        else
            return new CommonResult<T>( ResultCode.SUCCESS.getCode(), "操作成功！", null);
    }

    /**
     * 失败返回结果
     * @param message 提示信息
     */
    public static <T> CommonResult<T> failed(String message) {
        return new CommonResult<T>( ResultCode.FAILED.getCode(), message, null);
    }

    /**
     * 失败返回结果
     */
    public static <T> CommonResult<T> failed() {
        return failed( ResultCode.FAILED);
    }

    /**
     * 参数验证失败返回结果
     */
    public static <T> CommonResult<T> validateFailed() {
        return failed( ResultCode.VALIDATE_FAILED);
    }

    /**
     * 参数验证失败返回结果
     * @param message 提示信息
     */
    public static <T> CommonResult<T> validateFailed(String message) {
        return new CommonResult<T>( ResultCode.VALIDATE_FAILED.getCode(), message, null);
    }

    /**
     * 未登录返回结果
     */
    public static <T> CommonResult<T> unauthorized(T data) {
        return new CommonResult<T>( ResultCode.UNAUTHORIZED.getCode(),  ResultCode.UNAUTHORIZED.getMessage(), data);
    }

    /**
     * 未授权返回结果
     */
    public static <T> CommonResult<T> forbidden(T data) {
        return new CommonResult<T>( ResultCode.FORBIDDEN.getCode(),  ResultCode.FORBIDDEN.getMessage(), data);
    }
}
