package com.nhjclxc.generator.config;

import com.nhjclxc.generator.model.ContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author LuoXianchao 
 */
@Component
public class MyInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
 
        if (handler instanceof HandlerMethod) {
            HandlerMethod hm = (HandlerMethod) handler;
            String method = hm.getMethod().getName();
            System.out.println("拦截器生效  ========>> " + hm.getBeanType().getName() + "." + method + "() 发起了请求。。。");
            String sessionUuid = request.getHeader(ContextHolder.Authorization);
            if (sessionUuid == null || "".equals(sessionUuid)){
                throw new RuntimeException("无效的会话，请先连接数据库");
            }
            ContextHolder.setAuthorization(sessionUuid);
        }
        //请求往后传递
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求离开，移除ContextHolder保存的数据
        ContextHolder.remove();
    }
}
