package org.apples.travelinebackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * 업로드된 파일을 정적 리소스로 제공하기 위한 설정
 */
@Configuration
public class FileStorageConfig implements WebMvcConfigurer {

    @Value("${file.upload.base-path:uploads}")
    private String basePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /photos/** 경로로 접근 시 uploads/photos/ 디렉토리의 파일 제공
        registry.addResourceHandler("/photos/**")
                .addResourceLocations("file:" + Paths.get(basePath, "photos").toAbsolutePath() + "/");

        // /thumbnails/** 경로로 접근 시 uploads/thumbnails/ 디렉토리의 파일 제공
        registry.addResourceHandler("/thumbnails/**")
                .addResourceLocations("file:" + Paths.get(basePath, "thumbnails").toAbsolutePath() + "/");
    }
}

