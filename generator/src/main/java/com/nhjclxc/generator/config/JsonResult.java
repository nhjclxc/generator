package com.nhjclxc.generator.config;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 通用的返回结果集
 *
 * @author LuoXianchao
 * @param <T> 泛型数据类型
 */
public class JsonResult<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 是否成功 true or false
     */
    private Boolean success;

    /**
     * 状态码
     */
    private int code;

    /**
     * 返回内容
     */
    private String msg;

    /**
     * 数据对象
     */
    private T data;

    /**
     * 状态类型
     */
    public enum Type
    {
        /** 成功 */
        SUCCESS(0, "成功"),
        /** 警告 */
        WARN(301, "警告 */"),
        /** 错误 */
        ERROR(500, "错误");
        private final int value;
        private final String typeName;

        Type(int value, String typeName) {
            this.value = value;
            this.typeName = typeName;
        }

        public int value() {  return this.value; }
        public String typeName() {  return this.typeName; }
    }

    /**
     * 初始化一个新创建的 AjaxResult 对象，使其表示一个空消息。
     */
    public JsonResult() { }

    /**
     * 初始化一个新创建的 AjaxResult 对象
     *
     * @param type 状态类型
     * @param msg 返回内容
     * @param data 数据对象
     */
    public JsonResult(Type type, String msg, T data) {
        this.code = type.value();
        this.msg = msg;
        if (null != data) {
            this.data = data;
        }

        if (type.value == Type.SUCCESS.value) {
            this.success = Boolean.TRUE;
        } else {
            this.success = Boolean.FALSE;
        }
    }

    /**
     * 返回成功消息
     *
     * @return 成功消息
     */
    public static <T> JsonResult<T> success()
    {
        return JsonResult.success("操作成功");
    }

    /**
     * 返回成功数据
     *
     * @return 成功消息
     */
    public static <T> JsonResult<T> success(T data)
    {
        return JsonResult.success("操作成功", data);
    }

    /**
     * 返回成功消息
     *
     * @param msg 返回内容
     * @return 成功消息
     */
    public static <T> JsonResult<T> success(String msg)
    {
        return JsonResult.success(msg, null);
    }

    /**
     * 返回成功消息
     *
     * @param msg 返回内容
     * @param data 数据对象
     * @return 成功消息
     */
    public static <T> JsonResult<T> success(String msg, T data)
    {
        return new JsonResult<>(Type.SUCCESS, msg, data);
    }

    /**
     * 返回警告消息
     *
     * @param msg 返回内容
     * @return 警告消息
     */
    public static <T> JsonResult<T> warn(String msg)
    {
        return JsonResult.warn(msg, null);
    }

    /**
     * 返回警告消息
     *
     * @param msg 返回内容
     * @param data 数据对象
     * @return 警告消息
     */
    public static <T> JsonResult<T> warn(String msg, T data)
    {
        return new JsonResult<>(Type.WARN, msg, data);
    }

    /**
     * 返回错误消息
     *
     * @return 警告消息
     */
    public static <T> JsonResult<T> error()
    {
        return JsonResult.error("操作失败");
    }

    /**
     * 返回错误消息
     *
     * @param msg 返回内容
     * @return 警告消息
     */
    public static <T> JsonResult<T> error(String msg)
    {
        return JsonResult.error(msg, null);
    }

    /**
     * 返回错误消息
     *
     * @param msg 返回内容
     * @param data 数据对象
     * @return 警告消息
     */
    public static <T> JsonResult<T> error(String msg, T data)
    {
        return new JsonResult<>(Type.ERROR, msg, data);
    }


    /**
     * 往data为map类型的数据里面添加keyvalue的数据
     *
     * @param key map对应的key值
     * @param value 改key对应的value值
     * @return 返回本对象
     */
    public JsonResult<T> put(String key, Object value) {
        if (this.data instanceof Map){
            Map<String, Object> data = (Map<String, Object>) this.data;
            data.put(key, value);
            return this; // 支持链式调用
        }
        throw new RuntimeException("data对应的泛型不支持put方法");
    }

    /**
     * 返回一个泛型为Map的JsonResult对象
     */
    public static JsonResult<Map<String, Object>> getMapInstance(){
//        JsonResult<Map<String, Object>> put = JsonResult.getMapInstance().put("key0", "data100").put("key1", "data11").put("key2", "data22").put("key3", "data33");
//        {"code":0,"data":{"key1":"data11","key2":"data22","key0":"data100","key3":"data33"},"msg":"操作成功","success":true}
        return JsonResult.success(new HashMap<>());
    }

    public T getData() {
        return data;
    }

    public void getData(T data) {
        this.data = data;
    }

    public boolean getSuccess() {
        return success;
    }

    public String getMsg() {
        return msg;
    }

    public Integer getCode() {
        return code;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
