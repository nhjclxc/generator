package com.nhjclxc.generator.model;


/**
 * 读取代码生成相关配置
 * 
 */

public class GenConfig
{
    /** 作者 */
    public static String author;

    /** 生成包路径 */
    public static String packageName;

    /** 自动去除表前缀，默认是false */
    public static boolean autoRemovePre;

    /** 表前缀(类名不会包含表前缀) */
    public static String tablePrefix;

    /** 是否开启swagger注解 */
    public static Boolean enableSwagger;

    public static Boolean getEnableSwagger() {
        return enableSwagger;
    }

    public static void setEnableSwagger(Boolean enableSwagger) {
        GenConfig.enableSwagger = enableSwagger;
    }

    public static String getAuthor()
    {
        return author;
    }

    public static void setAuthor(String author)
    {
        GenConfig.author = author;
    }

    public static String getPackageName()
    {
        return packageName;
    }

    public static void setPackageName(String packageName)
    {
        GenConfig.packageName = packageName;
    }

    public static boolean getAutoRemovePre()
    {
        return autoRemovePre;
    }

    public static void setAutoRemovePre(boolean autoRemovePre)
    {
        GenConfig.autoRemovePre = autoRemovePre;
    }

    public static String getTablePrefix()
    {
        return tablePrefix;
    }

    public static void setTablePrefix(String tablePrefix)
    {
        GenConfig.tablePrefix = tablePrefix;
    }
}
