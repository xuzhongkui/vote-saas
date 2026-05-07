package com.jm.vote.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload.path:upload}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取上传目录的绝对路径
        String absolutePath;
        if (Paths.get(uploadPath).isAbsolute()) {
            absolutePath = uploadPath;
        } else {
            absolutePath = Paths.get(System.getProperty("user.dir"), uploadPath).toAbsolutePath().toString();
        }
        
        // 确保路径以斜杠结尾
        if (!absolutePath.endsWith("/") && !absolutePath.endsWith("\\")) {
            absolutePath = absolutePath + "/";
        }
        
        // Windows 路径需要转换
        String resourceLocation = "file:" + absolutePath.replace("\\", "/");
        
        registry.addResourceHandler("/upload/**")
                .addResourceLocations(resourceLocation);
    }
}

