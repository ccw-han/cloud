package com.example.serviceapigateway.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * 网关过滤器
 * 加入到Spring容器
 *
 * @author Evan
 */
@Component
public class UserLoginZuulFilter extends ZuulFilter {

    @Override
    public boolean shouldFilter() {
        return true; // 该过滤器需要执行
    }

    @Override
    public Object run() { //编写业务逻辑
        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletRequest request = requestContext.getRequest();
        String token = request.getParameter("token");
        if (StringUtils.isEmpty(token)) {
            requestContext.setSendZuulResponse(false); // 过滤该请求，不对其进行路由
            requestContext.setResponseStatusCode(401); // 设置响应状态码
            requestContext.setResponseBody(" token is empty!!"); // 设置响应状态码
            return null;
        }
        return null;
    }

    @Override
    public String filterType() {
        return "pre"; // 设置过滤器类型为：pre
    }

    @Override
    public int filterOrder() {
        return 0;// 设置执行顺序为0
    }
}

