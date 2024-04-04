package com.nhjclxc.generator.config;

import com.nhjclxc.generator.util.StringUtils;
import lombok.Data;

import java.io.Serializable;

@Data
public class JsonResult<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 是否成功 true or false
     */
    private boolean success;

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
        SUCCESS(0),
        /** 警告 */
        WARN(301),
        /** 错误 */
        ERROR(500);
        private final int value;

        Type(int value)
        {
            this.value = value;
        }

        public int value()
        {
            return this.value;
        }
    }

    /**
     * 初始化一个新创建的 AjaxResult 对象，使其表示一个空消息。
     */
    public JsonResult()
    {
    }

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
        if (StringUtils.isNotNull(data)) {
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
     * @return
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
     * 方便链式调用
     *
     * @param key   键
     * @param value 值
     * @return 数据对象
     */
    @Deprecated
    public JsonResult<T> put(String key, Object value) {
		//super.put(key, value);
        return this;
    }

    /**
     * 是否为成功消息
     *
     * @return 结果
     */
    public boolean isSuccess() {
        return success;
    }

    public String getMsg() {
        return msg;
    }

    public Integer getCode() {
        return code;
    }
}
