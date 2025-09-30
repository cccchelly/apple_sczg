package com.sczg.apple.base;

public class BaseResponse<DataType> {

    public static final int RESULT_CODE_SUCCESS = 0;
    public static final int RESULT_CODE_DEVICE_UNLOGIN = 10; ////未登录
    public static final int RESULT_CODE_ERROR = 11;
    public static final int RESULT_CODE_TOKEN_EXPIRED = 401;

    private int code;

    private String msg;

    private DataType data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public DataType getData() {
        return data;
    }

    public void setData(DataType data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "BaseResponse{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
