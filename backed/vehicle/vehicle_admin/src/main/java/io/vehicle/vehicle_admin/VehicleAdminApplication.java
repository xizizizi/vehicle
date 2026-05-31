package io.vehicle.vehicle_admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@MapperScan("io.vehicle.vehicle_admin.mapper")
@SpringBootApplication
public class VehicleAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(VehicleAdminApplication.class, args);
    }

    @Configuration
    public static class GlobalCorsConfig {
        @Bean
        public WebMvcConfigurer corsConfigurer() {
            return new WebMvcConfigurer() {
                @Override
                public void addCorsMappings(CorsRegistry registry) {
                    registry.addMapping("/**")
                            .allowedOriginPatterns("*")
                            .allowedMethods("*")
                            .allowedHeaders("*")
                            .allowCredentials(true);
                }
            };
        }
    }
}
