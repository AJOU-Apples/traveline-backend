package org.apples.travelinebackend.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 파일 저장 인터페이스
 * 로컬 파일 시스템 또는 AWS S3로 쉽게 교체 가능
 */
public interface FileStorageService {
    
    /**
     * 파일 업로드
     * @param file 업로드할 파일
     * @param folder 저장할 폴더 (photos, thumbnails 등)
     * @return 저장된 파일의 URI (로컬 경로 또는 S3 URL)
     */
    String uploadFile(MultipartFile file, String folder) throws IOException;
    
    /**
     * 썸네일 생성 및 업로드
     * @param file 원본 이미지 파일
     * @param folder 저장할 폴더
     * @param maxWidth 최대 가로 크기
     * @param maxHeight 최대 세로 크기
     * @return 썸네일 파일의 URI
     */
    String uploadThumbnail(MultipartFile file, String folder, int maxWidth, int maxHeight) throws IOException;
    
    /**
     * 파일 삭제
     * @param fileUri 삭제할 파일의 URI
     */
    void deleteFile(String fileUri) throws IOException;
}

