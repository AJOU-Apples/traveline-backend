package org.apples.travelinebackend.service;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
public class LocalFileStorageService implements FileStorageService {

    @Value("${file.upload.base-path:uploads}")
    private String basePath;

    @Override
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("업로드할 파일이 비어있습니다.");
        }

        // 저장 디렉토리 생성
        Path uploadPath = Paths.get(basePath, folder);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("디렉토리 생성: {}", uploadPath.toAbsolutePath());
        }

        // 고유한 파일명 생성
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String uniqueFilename = UUID.randomUUID().toString() + extension;

        // 파일 저장
        Path targetPath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        String fileUri = "/" + folder + "/" + uniqueFilename;
        log.info("파일 저장 완료: {}", fileUri);

        return fileUri;
    }

    @Override
    public String uploadThumbnail(MultipartFile file, String folder, int maxWidth, int maxHeight) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("업로드할 파일이 비어있습니다.");
        }

        // 저장 디렉토리 생성
        Path uploadPath = Paths.get(basePath, folder);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("디렉토리 생성: {}", uploadPath.toAbsolutePath());
        }

        // 썸네일 생성
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Thumbnails.of(file.getInputStream())
                .size(maxWidth, maxHeight)
                .outputFormat("jpg")
                .outputQuality(0.8)
                .toOutputStream(outputStream);

        // 고유한 파일명 생성
        String uniqueFilename = "thumb_" + UUID.randomUUID().toString() + ".jpg";

        // 파일 저장
        Path targetPath = uploadPath.resolve(uniqueFilename);
        Files.copy(new ByteArrayInputStream(outputStream.toByteArray()), 
                   targetPath, 
                   StandardCopyOption.REPLACE_EXISTING);

        String thumbnailUri = "/" + folder + "/" + uniqueFilename;
        log.info("썸네일 생성 완료: {}", thumbnailUri);

        return thumbnailUri;
    }

    @Override
    public void deleteFile(String fileUri) throws IOException {
        if (fileUri == null || fileUri.isEmpty()) {
            return;
        }

        // URI에서 실제 파일 경로 추출
        String filePath = fileUri.startsWith("/") ? fileUri.substring(1) : fileUri;
        Path targetPath = Paths.get(basePath, filePath);

        if (Files.exists(targetPath)) {
            Files.delete(targetPath);
            log.info("파일 삭제 완료: {}", fileUri);
        } else {
            log.warn("삭제할 파일이 존재하지 않음: {}", fileUri);
        }
    }

    /**
     * 파일의 실제 경로 반환 (테스트 또는 직접 접근용)
     */
    public String getAbsolutePath(String fileUri) {
        String filePath = fileUri.startsWith("/") ? fileUri.substring(1) : fileUri;
        return Paths.get(basePath, filePath).toAbsolutePath().toString();
    }
}

