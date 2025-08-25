package com.nhjclxc.generator.util;

//import com.nhjclxc.generator.config.GenConfig;
import com.nhjclxc.generator.model.GenConfig;
import com.nhjclxc.generator.model.GenTable;
import com.nhjclxc.generator.model.GenTableColumn;
import org.apache.commons.lang3.RegExUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

/**
 * 代码生成器 工具类
 *
 * @author LuoXianchao
 */
public class GenUtils
{
    /**
     * 初始化表信息
     */
    public static void initTable(GenTable genTable, String operName)
    {
        genTable.setClassName(convertClassName(genTable.getTableName()));
        genTable.setPackageName(GenConfig.getPackageName());
        genTable.setModuleName(getModuleName(GenConfig.getPackageName()));
        genTable.setBusinessName(getBusinessName(genTable.getTableName()));
        genTable.setFunctionName(replaceText(genTable.getTableComment()));
        genTable.setFunctionAuthor(GenConfig.getAuthor());
        genTable.setCreateBy(operName);
        genTable.setGenType("0");
    }

    private static final List<String> LIKE_SUFFIXES = Arrays.asList(
            "name", "label", "comment", "title", "desc", "description",
            "content", "remark", "summary", "keywords", "intro", "introduction", "value"
    );

    /**
     * 初始化列属性字段
     */
    public static void initColumnField(GenTableColumn column)
    {
        String dataType = getDbType(column.getColumnType());
        String columnName = column.getColumnName();
        // 设置java字段名
        column.setJavaField(StringUtils.toCamelCase(columnName));
        column.setGoField(StringUtils.convertToCamelCase(columnName));
        // 设置默认类型
        column.setIsQuery(GenConstants.REQUIRE);
        column.setJavaType(GenConstants.TYPE_STRING);
        column.setQueryType(GenConstants.QUERY_EQ);

        if (arraysContains(GenConstants.COLUMNTYPE_STR, dataType) || arraysContains(GenConstants.COLUMNTYPE_TEXT, dataType))
        {
            // 字符串长度超过500设置为文本域
            Integer columnLength = getColumnLength(column.getColumnType());
            String htmlType = columnLength >= 500 || arraysContains(GenConstants.COLUMNTYPE_TEXT, dataType) ? GenConstants.HTML_TEXTAREA : GenConstants.HTML_INPUT;
            column.setHtmlType(htmlType);
        }
        else if (arraysContains(GenConstants.COLUMNTYPE_TIME, dataType))
        {
            column.setJavaType(GenConstants.TYPE_DATE);
            column.setHtmlType(GenConstants.HTML_DATETIME);
        }
        else if (arraysContains(GenConstants.COLUMNTYPE_NUMBER, dataType))
        {
            column.setHtmlType(GenConstants.HTML_INPUT);

            // 如果是浮点型 统一用BigDecimal
            String[] str = StringUtils.split(StringUtils.substringBetween(column.getColumnType(), "(", ")"), ",");
            if ((str != null && str.length == 2 && Integer.parseInt(str[1]) > 0 ) || dataType.toLowerCase().contains("decimal"))
            {
                column.setJavaType(GenConstants.TYPE_BIGDECIMAL);
            }else if (dataType.toLowerCase().contains("bigint")){
                // 长整形
                column.setJavaType(GenConstants.TYPE_LONG);
            }else {
                // 整形
                column.setJavaType(GenConstants.TYPE_INTEGER);
            }
        }

        // 插入字段（默认所有字段都需要插入）
        column.setIsInsert(GenConstants.REQUIRE);

        // 编辑字段
        if (!arraysContains(GenConstants.COLUMNNAME_NOT_EDIT, columnName) && !column.isPk())
        {
            column.setIsEdit(GenConstants.REQUIRE);
        }
        // 列表字段
        if (!arraysContains(GenConstants.COLUMNNAME_NOT_LIST, columnName) && !column.isPk())
        {
            column.setIsList(GenConstants.REQUIRE);
        }
        // 查询字段
        if (!arraysContains(GenConstants.COLUMNNAME_NOT_QUERY, columnName) && !column.isPk())
        {
            column.setIsQuery(GenConstants.REQUIRE);
        }

        // 查询字段类型
//        if (StringUtils.endsWithIgnoreCase(columnName, "name")
//                || StringUtils.endsWithIgnoreCase(columnName, "label")
//                || StringUtils.endsWithIgnoreCase(columnName, "comment")
//                || StringUtils.endsWithIgnoreCase(columnName, "comment")
//                || StringUtils.endsWithIgnoreCase(columnName, "title")
//                || StringUtils.endsWithIgnoreCase(columnName, "summary")
//                || StringUtils.endsWithIgnoreCase(columnName, "keywords")
//                || StringUtils.endsWithIgnoreCase(columnName, "desc") || StringUtils.endsWithIgnoreCase(columnName, "description")
//                || StringUtils.endsWithIgnoreCase(columnName, "intro") || StringUtils.endsWithIgnoreCase(columnName, "introduction")
//                || StringUtils.endsWithIgnoreCase(columnName, "remark") )
        if (LIKE_SUFFIXES.stream().anyMatch(suffix -> StringUtils.endsWithIgnoreCase(columnName, suffix)))
        {
            column.setQueryType(GenConstants.QUERY_LIKE);
        }
        // 状态字段设置单选框
        if (StringUtils.endsWithIgnoreCase(columnName, "status"))
        {
            column.setHtmlType(GenConstants.HTML_RADIO);
        }
        // 类型&性别字段设置下拉框
        else if (StringUtils.endsWithIgnoreCase(columnName, "type")
                || StringUtils.endsWithIgnoreCase(columnName, "sex"))
        {
            column.setHtmlType(GenConstants.HTML_SELECT);
        }
        // 图片字段设置图片上传控件
        else if (StringUtils.endsWithIgnoreCase(columnName, "image"))
        {
            column.setHtmlType(GenConstants.HTML_IMAGE_UPLOAD);
        }
        // 文件字段设置文件上传控件
        else if (StringUtils.endsWithIgnoreCase(columnName, "file"))
        {
            column.setHtmlType(GenConstants.HTML_FILE_UPLOAD);
        }
        // 内容字段设置富文本控件
        else if (StringUtils.endsWithIgnoreCase(columnName, "content"))
        {
            column.setHtmlType(GenConstants.HTML_EDITOR);
        }
    }

    public static String mapToGoType(String dataType, String columnType, String isNullable) {
        String goType = mapToGoType(dataType, columnType);
        if ("YES".equals(isNullable) && !goType.equals("string")) {
            goType = "*" + goType; // 可空用指针
        }
        return goType;
    }

    public static String mapToGoType(String dataType, String columnType) {
        if (dataType == null) return "interface{}";
        dataType = dataType.toLowerCase();
        columnType = columnType != null ? columnType.toLowerCase() : "";

        boolean isUnsigned = columnType.contains("unsigned");

        switch (dataType) {
            // ---------------- 数值类型 ----------------
            case "tinyint":
                if (columnType.equals("tinyint(1)")) {
                    return "bool"; // tinyint(1) 特殊映射为 bool
                }
                return isUnsigned ? "uint8" : "int8";

            case "smallint":
                return isUnsigned ? "uint16" : "int16";

            case "mediumint":
                return isUnsigned ? "uint32" : "int32";

            case "int":
            case "integer":
                return isUnsigned ? "uint32" : "int32";

            case "bigint":
                return isUnsigned ? "uint64" : "int64";

            case "decimal":
            case "numeric":
                return "float64"; // 或者使用 github.com/shopspring/decimal

            case "float":
                return "float32";

            case "double":
            case "real":
                return "float64";

            case "bit":
                return "[]byte"; // MySQL BIT 一般用 byte array

            // ---------------- 字符串类型 ----------------
            case "char":
            case "varchar":
            case "tinytext":
            case "text":
            case "mediumtext":
            case "longtext":
            case "enum":
            case "set":
                return "string";

            // ---------------- JSON 类型 ----------------
            case "json":
                return "datatypes.JSON"; // GORM 的 datatypes.JSON

            // ---------------- 时间类型 ----------------
            case "date":
            case "datetime":
            case "timestamp":
            case "time":
            case "year":
                return "time.Time";

            // ---------------- 二进制类型 ----------------
            case "binary":
            case "varbinary":
            case "tinyblob":
            case "blob":
            case "mediumblob":
            case "longblob":
                return "[]byte";

            // ---------------- 兜底 ----------------
            default:
                return "interface{}"; // 未知类型，默认 interface{}
        }
    }

    public static String execGoGormtarget(ResultSet rs) throws SQLException {
        String columnName = rs.getString("column_name");
        String dataType = rs.getString("data_type");
        Long charLength = rs.getLong("character_maximum_length");
        String columnKey = rs.getString("column_key");
        String columnType =  rs.getString("column_type");
        String extra = rs.getString("extra");
        String comment = rs.getString("column_comment");
        String indexNames = rs.getString("index_names");
        String isNullable = rs.getString("is_nullable");
        String columnDefault = rs.getString("column_default");


        boolean priFlag = false, uniFlag = false;
        // 构建 GORM 标签
        List<String> tags = new ArrayList<>();
        tags.add("column:" + columnName);
        tags.add("type:" + columnType);
        if (columnKey.equals("PRI")) {
            tags.add("primaryKey");
            priFlag = true;
        }
        if (columnKey.equals("UNI")) {
            tags.add("unique");
            uniFlag = true;
        }
        if (extra != null && extra.contains("auto_increment"))
            tags.add("autoIncrement");

        if ("1".equals(rs.getString("is_nullable")))
            tags.add("not null");

        if (null != columnDefault) {

            tags.add("default:" + columnDefault);
            if ("CURRENT_TIMESTAMP".equals(columnDefault))
                tags.add("autoUpdateTime");
        }

        if (indexNames != null && !indexNames.isEmpty()) {
            String[] idxs = indexNames.split(",");
            for (String s : idxs) {
                String[] idx = s.split(":");
                if ( (priFlag && "PRIMARY".equals(idx[0])) || (uniFlag && columnName.equals(idx[0]))) {
                    continue;
                }
                tags.add("index:" + idx[0] + ",priority:" + idx[1]);
            }
        }
        if (comment != null && !comment.isEmpty()) {
            tags.add("comment:" + comment);
        }

        String goGormTarget = String.join(";", tags);

        // 输出 Go 字段
//        System.out.printf("%s `gorm:\"%s\"` \n\n", columnName, goGormTarget);
        return goGormTarget;
    }

    /**
     * 校验数组是否包含指定值
     *
     * @param arr 数组
     * @param targetValue 值
     * @return 是否包含
     */
    public static boolean arraysContains(String[] arr, String targetValue)
    {
        return Arrays.asList(arr).contains(targetValue);
    }

    /**
     * 获取模块名
     *
     * @param packageName 包名
     * @return 模块名
     */
    public static String getModuleName(String packageName)
    {
        int lastIndex = packageName.lastIndexOf(".");
        int nameLength = packageName.length();
        return StringUtils.substring(packageName, lastIndex + 1, nameLength);
    }

    /**
     * 获取业务名
     *
     * @param tableName 表名
     * @return 业务名
     */
    public static String getBusinessName(String tableName)
    {
//        int lastIndex = tableName.lastIndexOf("_");
//        int nameLength = tableName.length();
//        return StringUtils.substring(tableName, lastIndex + 1, nameLength);

        return tableName.replaceAll("_", "/");
    }

    /**
     * 表名转换成Java类名
     *
     * @param tableName 表名称
     * @return 类名
     */
    public static String convertClassName(String tableName)
    {
        boolean autoRemovePre = GenConfig.getAutoRemovePre();
        String tablePrefix = GenConfig.getTablePrefix();
        if (autoRemovePre && StringUtils.isNotEmpty(tablePrefix))
        {
            String[] searchList = StringUtils.split(tablePrefix, ",");
            tableName = replaceFirst(tableName, searchList);
        }
        return StringUtils.convertToCamelCase(tableName);
    }

    /**
     * 批量替换前缀
     *
     * @param replacementm 替换值
     * @param searchList 替换列表
     * @return
     */
    public static String replaceFirst(String replacementm, String[] searchList)
    {
        String text = replacementm;
        for (String searchString : searchList)
        {
            if (replacementm.startsWith(searchString))
            {
                text = replacementm.replaceFirst(searchString, "");
                break;
            }
        }
        return text;
    }

    /**
     * 关键字替换
     *
     * @param text 需要被替换的名字
     * @return 替换后的名字
     */
    public static String replaceText(String text)
    {
        return RegExUtils.replaceAll(text, "(?:表|若依)", "");
    }

    /**
     * 获取数据库类型字段
     *
     * @param columnType 列类型
     * @return 截取后的列类型
     */
    public static String getDbType(String columnType)
    {
        if (StringUtils.indexOf(columnType, "(") > 0)
        {
            return StringUtils.substringBefore(columnType, "(");
        }
        else
        {
            return columnType;
        }
    }

    /**
     * 获取字段长度
     *
     * @param columnType 列类型
     * @return 截取后的列类型
     */
    public static Integer getColumnLength(String columnType)
    {
        if (StringUtils.indexOf(columnType, "(") > 0)
        {
            String length = StringUtils.substringBetween(columnType, "(", ")");
            return Integer.valueOf(length);
        }
        else
        {
            return 0;
        }
    }
}
