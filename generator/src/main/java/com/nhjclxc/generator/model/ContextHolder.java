package com.nhjclxc.generator.model;

/**
 * @author LuoXianchao  
 * @since 2024/04/05 10:58
 */
public class ContextHolder {

    public static String Authorization = "Authorization";

    /**
     *  这个泛型是你想存放到ThreadLoacl里面的数据的类型
     */
    public static ThreadLocal<String> authorization = new ThreadLocal<>();


    /**
     * 获取存进来的数据
     */
    public static String getAuthorization() {
        return authorization.get();
    }

    /**
     * 设置对应的值
     */
    public static void setAuthorization(String value) {
        authorization.set(value);
    }

    /**
     * 在退出拦截器之后要使用remove方法来移除数据，即在拦截器的after方法里面调用AuthorizationThreadLocal.remove();
     *     这个方法是为了防止内存溢出
     */

    public static void remove() {
        authorization.remove();
    }
}
