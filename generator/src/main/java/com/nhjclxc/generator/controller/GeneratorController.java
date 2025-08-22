package com.nhjclxc.generator.controller;

import com.alibaba.fastjson2.JSON;
import com.github.pagehelper.PageInfo;
import com.nhjclxc.generator.model.ContextHolder;
import com.nhjclxc.generator.model.GenTable;
import com.nhjclxc.generator.model.GeneratorCodeDTO;
import com.nhjclxc.generator.model.JDBCObject;
import com.nhjclxc.generator.service.GeneratorService;
import com.nhjclxc.generator.config.JsonResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

/**
 * 代码生成控制器
 *
 * @author LuoXianchao
 */
@Slf4j
@RestController
@RequestMapping("/gen")
@Api(tags = "tags⼀个登录的接⼝", description = "⽤户登录API")
public class GeneratorController {
    @Autowired
    private GeneratorService generatorService;

    /*
    连接到数据库： http://127.0.0.1:8099/gen/connect?username=root&password=root123&jdbcUrl=jdbc:mysql://127.0.0.1:3306/test1

    代码生成：
        连接到数据库接口的响应头里面会返回一个Authorization，要在这个接口加上
        http://127.0.0.1:8099/gen/genCode?enableLombok=true&enableSwagger=true&packageName=com.example&tables=xxx

    代码预览：
        连接到数据库接口的响应头里面会返回一个Authorization，要在这个接口加上
        http://127.0.0.1:8099/gen/preview?enableLombok=true&enableSwagger=true&packageName=com.example&tables=xxx

     */

    /**
     * 连接到数据库
     */
    @ApiOperation(value = "连接到数据库")
    @ApiResponse(code = 200, message = "success")
    @GetMapping("/connect")
    public JsonResult<PageInfo<GenTable>> connect(JDBCObject jdbcObject, HttpServletRequest request, HttpServletResponse response) throws SQLException {
        // 关闭上一个连接，防止一个浏览器客户端不断发起连接请求的情况，导致连接数不断增加
        String preSessionUuid = request.getHeader(ContextHolder.Authorization);
        System.out.println("preSessionUuid" + preSessionUuid);
        System.out.println("connect.jdbcObject" + JSON.toJSONString(jdbcObject));
        boolean b = generatorService.removeSessionUuid(preSessionUuid);

        String sessionUuid = generatorService.connect(jdbcObject);
        // 返回会话uuid
        // 先暴露这个响应头，之后在设置对应的响应头，不暴露的话前端拿不到对应的响应头
        response.addHeader("Access-Control-Expose-Headers", ContextHolder.Authorization);
        response.setHeader(ContextHolder.Authorization, sessionUuid);

        //设置会话，下面获取数据要用
        ContextHolder.setAuthorization(sessionUuid);

        // 获取默认数据
        PageInfo<GenTable> tablePageInfo = generatorService.parse(new GeneratorCodeDTO(), 1, 10);
        return JsonResult.success("数据库连接成功 ！！！", tablePageInfo);
    }

    /**
     * 关闭数据库连接
     */
    @ApiOperation(value = "关闭数据库连接")
    @ApiResponse(code = 200, message = "success")
    @GetMapping("/closeConnect")
    public JsonResult<String> closeConnect() {
        return JsonResult.success(generatorService.closeConnect());
    }


    /**
     * 解析数据库表结构
     */
    @ApiOperation(value = "解析数据库表结构")
    @ApiResponse(code = 200, message = "success")
    @GetMapping("/parse")
    public JsonResult<PageInfo<GenTable>> parse(GeneratorCodeDTO dto, Integer pageNum, Integer pageSize) throws SQLException {
        return JsonResult.success(generatorService.parse(dto, pageNum, pageSize));
    }

    /**
     * 生成代码
     */
    @GetMapping("/genCode")
    public void genCode(GeneratorCodeDTO dto, HttpServletResponse response) throws IOException, SQLException {
        byte[] data = generatorService.genCode(dto);
        write(response, data, dto.getTables());
    }

    /**
     * 预览代码
     */
    @GetMapping("/preview")
    public JsonResult<Map<String, String>> preview(GeneratorCodeDTO dto) throws IOException, SQLException {
        Map<String, String> dataMap = generatorService.previewCode(dto);
        return JsonResult.success(dataMap);
    }

    /**
     * 生成zip文件
     */
    private void write(HttpServletResponse response, byte[] data, String tables) throws IOException
    {
        response.reset();
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + tables + "-" + System.currentTimeMillis() + ".zip\"");
        response.addHeader("Content-Length", "" + data.length);
        response.setContentType("application/octet-stream; charset=UTF-8");
        IOUtils.write(data, response.getOutputStream());
    }
}
