package net.cyweb.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix="myProps") //接收application.yml中的myProps下面的属性
@Data
public class UserProPs {

    private Map<String, String> notice = new HashMap<>();

}
