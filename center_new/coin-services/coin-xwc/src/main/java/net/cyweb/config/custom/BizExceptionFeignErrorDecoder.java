package net.cyweb.config.custom;

import com.netflix.hystrix.exception.HystrixBadRequestException;
import feign.Response;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BizExceptionFeignErrorDecoder implements feign.codec.ErrorDecoder{


    @Override
    public Exception decode(String methodKey, Response response) {
        if(response.status() >= 400 && response.status() <= 499){
            return new HystrixBadRequestException("xxxxxx");
        }
        return feign.FeignException.errorStatus(methodKey, response);
    }


}