package io.vehicle.vehicle_admin.dto;

/**
 *
 * @author xixixizi
 * @version 2025/7/18 19:05
 * @since JDK22
 */
//封装响应结果
public class Result {

    private int code=0;
    private String msg="ok";
    private Object data;


    public String toString() {
        return "Result{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
    public Result() {}
    public Result(int code,String msg,Object data) {this.code=code;this.msg=msg;this.data=data;}
    public Result(Object data) {this.data=data;}
    public Result(int code,String msg) {this.code=code;this.msg=msg;}
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    // 成功响应（带数据）
    public static Result success(Object data) {
        return new Result(0, "ok", data);
    }

    // 成功响应（带消息和数据）
    public static Result success(String msg, Object data) {
        return new Result(0, msg, data);
    }

    // 失败响应
    public static Result fail(String msg) {
        return new Result(-1, msg, null);
    }
}

