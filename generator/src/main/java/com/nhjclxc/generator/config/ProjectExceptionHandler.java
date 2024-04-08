package com.nhjclxc.generator.config;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 统一异常处理
 */
@RestControllerAdvice(basePackages = "com.nhjclxc.generator")
public class ProjectExceptionHandler {

    //统一处理所有的Exception异常
    @ResponseBody
    @ExceptionHandler({Exception.class})
    public Object doException(Exception ex) {
        ex.printStackTrace();
        JsonResult<Object> jsonResult = new JsonResult<>();
        jsonResult.setCode(JsonResult.Type.ERROR.value());
        jsonResult.setMsg(ex.getMessage());
        jsonResult.setData(null);
        return jsonResult;
    }
}