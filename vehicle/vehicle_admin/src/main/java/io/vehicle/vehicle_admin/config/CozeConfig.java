package io.vehicle.vehicle_admin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class CozeConfig {

    @Value("${coze.connect-timeout:30000}")
    private int connectTimeout;

    @Value("${coze.read-timeout:30000}")
    private int readTimeout;

    /**
     * Coze 专用 RestTemplate，独立配置，不影响其他服务
     */
    @Bean("cozeRestTemplate")
    public RestTemplate cozeRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);

        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.setErrorHandler(new CozeResponseErrorHandler());

        return restTemplate;
    }

    /**
     * 默认 RestTemplate，供其他服务使用
     */
    @Bean
    @Primary
    public RestTemplate defaultRestTemplate() {
        return new RestTemplate();
    }
}