package com.hestia.config;

import com.hestia.interceptor.JwtTokenAdminInterceptor;
import com.hestia.interceptor.JwtTokenResidentInterceptor;
import com.hestia.json.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.List;

/**
 * Web 层配置：拦截器、接口文档、静态资源、消息转换器
 */
@Configuration
@Slf4j
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    @Autowired
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;
    @Autowired
    private JwtTokenResidentInterceptor jwtTokenResidentInterceptor;

    /**
     * 密码加密器。BCrypt 自带盐值，相同明文每次加密结果不同，比 MD5 安全。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("注册 JWT 拦截器...");
        // 管理端
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/auth/login");
        // 住户端
        registry.addInterceptor(jwtTokenResidentInterceptor)
                .addPathPatterns("/resident/**")
                .excludePathPatterns("/resident/auth/login");
    }

    @Bean
    public Docket adminApi() {
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title("云筑 Hestia · 运维管理端接口")
                .version("1.0")
                .description("工单、派单、设备台账、巡检、报表")
                .build();
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("管理端接口")
                .apiInfo(apiInfo)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.hestia.controller.admin"))
                .paths(PathSelectors.any())
                .build();
    }

    @Bean
    public Docket residentApi() {
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title("云筑 Hestia · 住户端接口")
                .version("1.0")
                .description("报修提交、进度查询、验收评价")
                .build();
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("住户端接口")
                .apiInfo(apiInfo)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.hestia.controller.resident"))
                .paths(PathSelectors.any())
                .build();
    }

    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展消息转换器...");
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(new JacksonObjectMapper());
        converters.add(0, converter);
    }
}
