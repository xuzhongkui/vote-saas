package com.jm.vote.service;

import com.jm.vote.dto.FileUploadResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@Slf4j
public class FileUploadService {

    @Value("${file.upload.path:upload}")
    private String uploadPath;

    public FileUploadResponse uploadFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 获取上传目录的绝对路径
        Path uploadDir;
        if (Paths.get(uploadPath).isAbsolute()) {
            uploadDir = Paths.get(uploadPath);
        } else {
            // 相对路径，基于当前工作目录
            uploadDir = Paths.get(System.getProperty("user.dir"), uploadPath);
        }
        
        log.info("上传目录: {}", uploadDir.toAbsolutePath());

        // 创建上传目录
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
            log.info("创建上传目录: {}", uploadDir.toAbsolutePath());
        }

        // 生成文件名：时间戳_随机UUID_扩展名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }
        
        // 验证文件类型
        if (!isAllowedExtension(extension)) {
            throw new IllegalArgumentException("不支持的文件类型: " + extension);
        }
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String randomId = UUID.randomUUID().toString().substring(0, 8);
        String newFilename = timestamp + "_" + randomId + extension;

        // 保存文件
        Path filePath = uploadDir.resolve(newFilename);
        file.transferTo(filePath.toFile());

        // 返回访问URL
        FileUploadResponse response = new FileUploadResponse();
        response.setUrl("/upload/" + newFilename);
        response.setOriginalName(originalFilename);
        response.setSize(file.getSize());

        log.info("文件上传成功: {} -> {}", originalFilename, filePath.toAbsolutePath());
        return response;
    }
    
    private boolean isAllowedExtension(String extension) {
        if (extension == null || extension.isEmpty()) {
            return false;
        }
        String[] allowedExtensions = {
            ".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp",
            ".mp4", ".avi", ".mov", ".wmv", ".flv", ".webm", ".mkv"
        };
        for (String allowed : allowedExtensions) {
            if (allowed.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }
}

