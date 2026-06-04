package com.sky.config;

import com.sky.interceptor.JwtTokenAdminInterceptor;
import com.sky.interceptor.JwtTokenUserInterceptor;
import com.sky.json.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

import java.util.Arrays;
import java.util.List;

/**
 * 配置类，注册web层相关组件
 */
@Configuration
@Slf4j
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    private final JwtTokenAdminInterceptor jwtTokenAdminInterceptor;
    private final JwtTokenUserInterceptor jwtTokenUserInterceptor;

    @Autowired
    public WebMvcConfiguration(JwtTokenAdminInterceptor jwtTokenAdminInterceptor,
                               JwtTokenUserInterceptor jwtTokenUserInterceptor) {
        this.jwtTokenAdminInterceptor = jwtTokenAdminInterceptor;
        this.jwtTokenUserInterceptor = jwtTokenUserInterceptor;
    }

    /**
     * 注册自定义拦截器
     *
     * @param registry
     */
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/employee/login");

        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns("/user/**")
                .excludePathPatterns(
                        "/user/user/login",
                        "/user/shop/status"
                );
    }

    /**
     * 管理端接口文档
     *
     * @return
     */
    @Bean
    public Docket adminApi() {
        return createDocket(
                "admin",
                "com.sky.controller.admin",
                "苍穹外卖管理端接口文档"
        );
    }

    /**
     * 用户端接口文档
     *
     * @return
     */
    @Bean
    public Docket userApi() {
        return createDocket(
                "user",
                "com.sky.controller.user",
                "苍穹外卖用户端接口文档"
        );
    }

    /**
     * 创建 Knife4j 接口文档分组
     *
     * @param groupName  文档分组名称
     * @param basePackage 扫描的 Controller 包
     * @param description 文档描述
     * @return Docket
     */
    private Docket createDocket(String groupName, String basePackage, String description) {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName(groupName)
                .enable(true)
                .apiInfo(createApiInfo(description))
                .select()
                .apis(RequestHandlerSelectors.basePackage(basePackage))
                .paths(PathSelectors.any())
                .build();
    }

    /**
     * 创建接口文档基本信息
     *
     * @param description 文档描述
     * @return ApiInfo
     */
    private ApiInfo createApiInfo(String description) {
        return new ApiInfoBuilder()
                .title("苍穹外卖项目接口文档")
                .version("2.0")
                .description(description)
                .build();
    }

    /**
     * 明确提供 Knife4j 页面可选择的接口文档分组
     *
     * @return Swagger 文档资源
     */
    @Bean
    @Primary
    public SwaggerResourcesProvider swaggerResourcesProvider() {
        return () -> Arrays.asList(
                createSwaggerResource("管理端接口", "admin"),
                createSwaggerResource("用户端接口", "user")
        );
    }

    private SwaggerResource createSwaggerResource(String displayName, String groupName) {
        SwaggerResource resource = new SwaggerResource();
        resource.setName(displayName);
        resource.setLocation("/v2/api-docs?group=" + groupName);
        resource.setSwaggerVersion("2.0");
        return resource;
    }

    /**
     * 设置静态资源映射
     * @param registry
     */
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }


    /**
     * 扩展 Spring MVC 框架的消息转换器
     *
     * @param converters Spring MVC 当前已有的消息转换器集合
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("开始扩展消息转换器...");

        // 创建一个 Jackson 消息转换器
        // 它的作用是：负责 Java 对象 <-> JSON 的转换
        MappingJackson2HttpMessageConverter converter =
                new MappingJackson2HttpMessageConverter();

        // 设置自定义的 ObjectMapper
        // 这样 LocalDateTime / LocalDate / LocalTime 就会按照你配置的格式转换
        converter.setObjectMapper(new JacksonObjectMapper());

        // 把自定义消息转换器放到第一个位置
        // 0 表示优先级最高，Spring MVC 会优先使用这个转换器
        converters.add(0, converter);
    }
}
