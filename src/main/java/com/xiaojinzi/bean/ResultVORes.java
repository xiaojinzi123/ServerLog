package com.xiaojinzi.bean;

import com.xiaojinzi.anno.NotEmpty;
import com.xiaojinzi.anno.Nullable;

public class ResultVORes<T> {

    public static final int CODE_SUCCESS = 0;

    public static final int CODE_ERROR_NORMAL = 10000;

    /**
     * 非 0 都是错误的
     */
    private int errorCode;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 数据
     */
    private T data;

    public ResultVORes(int errorCode, String errorMessage, T data) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.data = data;
    }

    public static <T> ResultVORes<T> success() {
        return success(null);
    }

    public static <T> ResultVORes<T> success(@Nullable T t) {
        return new ResultVORes(CODE_SUCCESS, null, t);
    }

    public static <T> ResultVORes<T> error(int errorCode, @NotEmpty String errMessage) {
        return new ResultVORes(errorCode, errMessage, null);
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

}
