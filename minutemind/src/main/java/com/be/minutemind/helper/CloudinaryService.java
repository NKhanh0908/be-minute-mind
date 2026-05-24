package com.be.minutemind.helper;


import com.cloudinary.Cloudinary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class CloudinaryService {
    private final Cloudinary cloudinary;

    @Value("${cloudinary.folder:mintues-mind}")
    private String defaultFolder;

    public CloudinaryService(
            @Value("${cloudinary.cloud_name}") String cloudName,
            @Value("${cloudinary.api_key}") String apiKey,
            @Value("${cloudinary.api_secret}") String apiSecret) {

        this.cloudinary = new Cloudinary(Map.of(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }

    /**
     * Upload file lên Cloudinary
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> uploadFile(MultipartFile file, String folder) {
        try {
            String originalFileName = file.getOriginalFilename(); // Lấy tên gốc của file
            String customFileName = generateCustomFileName(originalFileName); // Tạo tên file tùy chỉnh

            String contentType = file.getContentType(); // Lấy kiểu nội dung của file (VD: image/png, image/jpeg)
            long fileSize = file.getSize(); // Lấy kích thước file (tính bằng byte)

            String targetFolder = (folder != null && !folder.isEmpty()) ? folder : defaultFolder;

            log.info("Uploading file: {}, Size: {} bytes, ContentType: {}, to folder: {}, with custom name: {}",
                    originalFileName, fileSize, contentType, targetFolder, customFileName);

            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), Map.of(
                    "folder", targetFolder,
                    "public_id", customFileName,
                    "resource_type", "auto" // Tự động xác định loại tài nguyên (image, video, ...)
            ));


            uploadResult.put("contentType", contentType);
            uploadResult.put("fileSize", fileSize);
            uploadResult.put("customFileName", customFileName);
            uploadResult.put("originalFileName", originalFileName);
            uploadResult.put("folder", targetFolder);

            log.info("Upload successful: {}", uploadResult);
            return uploadResult;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Xóa file khỏi Cloudinary
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> deleteFile(String publicId) {
        try {
            log.info("Deleting file with public_id: {}", publicId);
            return cloudinary.uploader().destroy(publicId, Map.of(
                    "resource_type", "image"  // Tự động xác định loại tài nguyên (image, video, ...)
            ));

        } catch (IOException e) {
            log.error("Delete failed: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Lấy public_id từ URL
     */
    public String extractPublicId(String url) {
        String[] parts = url.split("/");
        String publicIdWithExtension = parts[parts.length - 1];  // tên file gốc kèm đuôi
        String publicId = publicIdWithExtension.split("\\.")[0]; // bỏ đuôi file

        // Tìm vị trí của "upload"
        int uploadIndex = -1;
        for (int i = 0; i < parts.length; i++) {
            if ("upload".equals(parts[i])) {
                uploadIndex = i;
                break;
            }
        }

        // Nếu sau "upload" có folder thì thêm vào publicId
        if (uploadIndex != -1 && uploadIndex + 2 < parts.length - 1) {
            // Sau "upload" là v<version>, sau nữa là folder
            StringBuilder fullPath = new StringBuilder();
            for (int i = uploadIndex + 2; i < parts.length - 1; i++) {
                fullPath.append(parts[i]).append("/");
            }
            fullPath.append(publicId);
            return fullPath.toString();
        }

        // Không có folder → chỉ return publicId
        return publicId;
    }



    /**
     * Tạo tên file tùy chỉnh để tránh trùng lặp
     */
    private String generateCustomFileName(String originalFileName) {
        return "upload_" + System.currentTimeMillis() + "_" + UUID.randomUUID();
    }

}