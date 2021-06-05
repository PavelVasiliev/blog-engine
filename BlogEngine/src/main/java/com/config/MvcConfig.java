package com.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {

    private static final String[] CLASSPATH_RESOURCE_LOCATIONS =
            {
                    "classpath:/META-INF/resources/",
                    "classpath:/resources/",
                    "classpath:/static/",
                    "classpath:/public/",
            };


    //ToDo static res path
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:///Desc/Java/java_basics/Diploma/Blog/upload/");
        registry.addResourceHandler("/avatars/**")
                .addResourceLocations("file:///Desc/Java/java_basics/Diploma/Blog/avatars/");
        registry.addResourceHandler("/**")
                .addResourceLocations(CLASSPATH_RESOURCE_LOCATIONS);

    }
}

