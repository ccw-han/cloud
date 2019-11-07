package com.example.serviceapigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

@SpringBootApplication
@EnableZuulProxy
@EnableEurekaClient
public class ServiceApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceApiGatewayApplication.class, args);
    }
    /*
    * 核心是过滤器
    * 1身份认证与安全
    * 2审查与监控
    * 3动态路由
    * 4压力测试
    * 5负载分配
    * 6静态响应处理
    * 7多区域弹性
    * 为了解决上面这些问题，我们需要将权限控制、
    * 日志收集这样的东西从我们的服务单元中抽离出去，
    * 而最适合这些逻辑的地方就是处于对外访问最前端的地方，
    * 我们需要一个更强大一些的均衡负载器 服务网关。
    * 服务网关是微服务架构中一个不可或缺的部分。通过服务网关统一向外系统提供
    * REST API的过程中，除了具备服务路由、负载均衡功能之外，它还具备了权限控制等功能
    * 。Spring Cloud Netflix中的Zuul就担任了这样的一个角色，
    * 为微服务架构提供了前门保护的作用，同时将权限控制这些较重的非业务逻辑内容迁移到
    * 服务路由层面，使得服务集群主体能够具备更高的可复用性和可测试性。
    *
    * 当然在Zuul上层也可以搭建Nginx、F5等负载均衡设施。

    *filterType：返回字符串代表过滤器的类型
        a)pre：请求在被路由之前执行
        b)routing：在路由请求时调用
        c)post：在routing和errror过滤器之后调用
        d)error：处理请求时发生错误调用
    *
    *zuul默认开启ribbon
    *
    * .网关集群
        Zuul可以配合nginx搭建网关集群。只要在nginx的配置文件nginx.conf里配置多个zuul地址：

    * http {
        upstream myhttpServer{
        #配置多个zuul地址
        server localhost:8087;
        server localhost:8086;
        }
        server {
            listen       80;
           server_name  www.zpc.com;
            location / {
                proxy_pass   http://myhttpServer;
                index  index.html index.htm;
            }
        }
    }

    * * */
    /*
     * 动态网关，把网关设置放到Git上，采用配置中心一样的模式 必须在bootstrap.yml中配置不然获取不到配置文件
     *
     * */
}
