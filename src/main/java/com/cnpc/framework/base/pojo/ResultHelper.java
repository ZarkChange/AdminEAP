package com.cnpc.framework.base.pojo;

/**
 * @author bin
 *
 */
public class ResultHelper {

    /**
     * 成功
     */
    public static String SUCCESS = "01";

    /**
     * 失败
     */
    public static String ERROR = "05";

    /**
     * 系统异常（CSRF、UnCatchException）
     */
    public static String EXCEPTION = "100";

    /**
     * 状态码
     */
    private String code;

    /**
     * 结果集
     */
    private Object data;

    // 增加以下几个构造函数，以便更好地调用
    public ResultHelper() {

        this.code = this.SUCCESS;
    }

    public ResultHelper(String code) {

        this.code = code;
    }

    public ResultHelper(String code, Object data) {

        this.code = code;
        this.data = data;
    }

    public String getCode() {

        return code;
    }

    public void setCode(String code) {

        this.code = code;
    }

    public Object getData() {

        return data;
    }

    public void setData(Object data) {

        this.data = data;
    }

}
