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
    public static boolean autoRemovePre = false;

    /** 表前缀(类名不会包含表前缀) */
    public static String tablePrefix;

    /** 是否开启swagger注解 */
    public static Boolean enableSwagger = true;

    public static Boolean getEnableSwagger() {
        return enableSwagger;
    }

    public static void setEnableSwagger(Boolean enableSwagger) {
        GenConfig.enableSwagger = enableSwagger;
    }
    /** 是否开启Lombok注解 */
    public static Boolean enableLombok = true;

    public static Boolean getEnableLombok() {
        return enableLombok;
    }

    public static void setEnableLombok(Boolean enableLombok) {
        GenConfig.enableLombok = enableLombok;
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

    public static Boolean getAutoRemovePre()
    {
        return autoRemovePre;
    }

    public static void setAutoRemovePre(Boolean autoRemovePre)
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
