package com.nhjclxc.generator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 *
 * @author LuoXianchao
 * @since 2024/04/03 23:18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratorCodeDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 要生成的表，多个表使用','分割 */
    private String tables;
    /** 是否开启swagger注解 */
    private Boolean enableSwagger = true;
    /** 代码作者 */
    private String author = "";
    /** 默认生成包路径  需改成自己的模块名称  */
    private String packageName = "com.example";
    /** 自动去除表前缀，默认是false */
    private Boolean autoRemovePre = false;
    /** 表前缀（生成类名不会包含表前缀，多个用逗号分隔） */
    private String tablePrefix = "";

    /** 表名称 */
    private String tableName;

    /** 表描述 */
    private String tableComment;

}
