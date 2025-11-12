package org.apples.travelinebackend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Slf4j
@Service
public class ImageMetadataService {

    /**
     * 이미지에서 메타데이터 추출
     */
    public ImageMetadata extractMetadata(MultipartFile file) {
        ImageMetadata metadata = new ImageMetadata();

        try {
            // 이미지 크기 추출
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image != null) {
                metadata.setWidth(image.getWidth());
                metadata.setHeight(image.getHeight());
                log.info("이미지 크기 추출 성공: {}x{}", metadata.getWidth(), metadata.getHeight());
            }

            // EXIF 데이터 추출 시도 (라이브러리가 있을 경우만)
            try {
                extractExifData(file, metadata);
            } catch (NoClassDefFoundError e) {
                log.warn("EXIF 라이브러리가 없습니다. metadata-extractor 의존성을 확인하세요.");
            } catch (Exception e) {
                log.warn("EXIF 데이터 추출 실패: {}", e.getMessage());
            }

        } catch (IOException e) {
            log.error("이미지 읽기 실패: {}", e.getMessage());
        }

        // 촬영 시간이 없으면 현재 시간 사용
        if (metadata.getTimestamp() == null) {
            metadata.setTimestamp(LocalDateTime.now());
        }

        return metadata;
    }

    /**
     * EXIF 데이터 추출 (metadata-extractor 라이브러리 필요)
     */
    private void extractExifData(MultipartFile file, ImageMetadata metadata) throws Exception {
        try {
            Class<?> readerClass = Class.forName("com.drew.imaging.ImageMetadataReader");
            Class<?> metadataClass = Class.forName("com.drew.metadata.Metadata");
            Class<?> gpsClass = Class.forName("com.drew.metadata.exif.GpsDirectory");
            Class<?> exifClass = Class.forName("com.drew.metadata.exif.ExifSubIFDDirectory");
            Class<?> geoLocationClass = Class.forName("com.drew.lang.GeoLocation");

            // ImageMetadataReader.readMetadata()
            file.getInputStream().reset();
            Object exifMetadata = readerClass.getMethod("readMetadata", java.io.InputStream.class)
                    .invoke(null, file.getInputStream());

            // GPS 정보 추출
            Object gpsDirectory = metadataClass.getMethod("getFirstDirectoryOfType", Class.class)
                    .invoke(exifMetadata, gpsClass);

            if (gpsDirectory != null) {
                Object geoLocation = gpsClass.getMethod("getGeoLocation").invoke(gpsDirectory);
                if (geoLocation != null) {
                    Boolean isZero = (Boolean) geoLocationClass.getMethod("isZero").invoke(geoLocation);
                    if (!isZero) {
                        Double lat = (Double) geoLocationClass.getMethod("getLatitude").invoke(geoLocation);
                        Double lng = (Double) geoLocationClass.getMethod("getLongitude").invoke(geoLocation);
                        metadata.setLatitude(lat);
                        metadata.setLongitude(lng);
                        log.info("GPS 정보 추출 성공: lat={}, lng={}", lat, lng);
                    }
                }
            }

            // 촬영 시간 추출
            Object exifDirectory = metadataClass.getMethod("getFirstDirectoryOfType", Class.class)
                    .invoke(exifMetadata, exifClass);

            if (exifDirectory != null) {
                Date originalDate = (Date) exifClass.getMethod("getDate", int.class)
                        .invoke(exifDirectory, 36867); // TAG_DATETIME_ORIGINAL
                if (originalDate != null) {
                    metadata.setTimestamp(LocalDateTime.ofInstant(
                            originalDate.toInstant(), ZoneId.systemDefault()));
                    log.info("촬영 시간 추출 성공: {}", metadata.getTimestamp());
                }
            }
        } catch (ClassNotFoundException e) {
            log.warn("EXIF 라이브러리를 찾을 수 없습니다: {}", e.getMessage());
        }
    }

    /**
     * 이미지 메타데이터 DTO
     */
    public static class ImageMetadata {
        private Integer width;
        private Integer height;
        private Double latitude;
        private Double longitude;
        private LocalDateTime timestamp;

        public Integer getWidth() {
            return width;
        }

        public void setWidth(Integer width) {
            this.width = width;
        }

        public Integer getHeight() {
            return height;
        }

        public void setHeight(Integer height) {
            this.height = height;
        }

        public Double getLatitude() {
            return latitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
    }
}

