package com.nhjclxc.generator.controller;

import com.github.pagehelper.PageInfo;
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

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

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

    /**
     * 连接到数据库
     */
    @ApiOperation(value = "连接到数据库")
    @ApiResponse(code = 200, message = "success")
    @GetMapping("/parse")
    public JsonResult<PageInfo<GenTable>> parse(JDBCObject jdbcObject, GenTable genTable, Integer pageNum, Integer pageSize) throws SQLException {
        return JsonResult.success(generatorService.parse(jdbcObject, genTable, pageNum, pageSize));
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
     * 生成zip文件
     */
    private void write(HttpServletResponse response, byte[] data, String tables) throws IOException
    {
        response.reset();
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + tables + "-Code-" + System.currentTimeMillis() + ".zip\"");
        response.addHeader("Content-Length", "" + data.length);
        response.setContentType("application/octet-stream; charset=UTF-8");
        IOUtils.write(data, response.getOutputStream());
    }
}