package com.changda.petcoursebackend.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
// 兼容前端5173/8081端口，避免跨域报错
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:8081"}, allowCredentials = "true")
public class UploadController {

    // 头像上传接口（保留原有功能）
    @PostMapping("/avatar")
    public String uploadAvatar(@RequestParam("file") MultipartFile file) {
        return uploadFile(file, "avatar");
    }

    // 课程封面上传接口（新增，专门用于课程封面）
    @PostMapping("/cover")
    public String uploadCover(@RequestParam("file") MultipartFile file) {
        return uploadFile(file, "cover");
    }

    // 通用文件上传方法（抽离公共逻辑，便于维护）
    private String uploadFile(MultipartFile file, String type) {
        // 1. 核心修复：获取项目根目录的绝对路径（解决相对路径找不到的问题）
        String rootPath = System.getProperty("user.dir");
        // 按文件类型分目录存储（头像/封面分开，便于管理）
        String uploadPath = rootPath + File.separator + "uploads" + File.separator + type;

        // 2. 创建上传目录（递归创建，确保多级目录都能创建）
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            boolean isCreated = uploadDir.mkdirs();
            System.out.println(String.format("[%s上传] 目录创建结果：%s，路径：%s",
                    type, isCreated ? "成功" : "失败", uploadPath));
        }

        // 3. 校验文件是否为空
        if (file.isEmpty()) {
            return String.format("上传失败：%s文件为空", type);
        }

        // 4. 校验文件大小（限制5MB以内，避免超大文件上传）
        long fileSize = file.getSize();
        long maxSize = 5 * 1024 * 1024; // 5MB
        if (fileSize > maxSize) {
            return String.format("上传失败：%s文件大小超过5MB限制", type);
        }

        // 5. 校验文件类型（仅允许图片格式）
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || !originalFileName.matches("^.+\\.(jpg|jpeg|png|gif|webp)$")) {
            return String.format("上传失败：%s文件仅支持jpg/jpeg/png/gif/webp格式", type);
        }

        try {
            // 6. 生成唯一文件名（UUID替代时间戳，避免重名+中文乱码）
            String suffix = originalFileName.substring(originalFileName.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + suffix;

            // 7. 目标文件绝对路径
            File destFile = new File(uploadDir, fileName);
            System.out.println(String.format("[%s上传] 文件保存路径：%s", type, destFile.getAbsolutePath()));

            // 8. 写入文件
            file.transferTo(destFile);

            // 9. 返回可直接访问的URL路径（前端无需拼接，直接使用）
            return String.format("http://localhost:8080/uploads/%s/%s", type, fileName);
        } catch (IOException e) {
            System.err.println(String.format("[%s上传] 失败：%s", type, e.getMessage()));
            return String.format("上传失败：%s", e.getMessage());
        } catch (Exception e) {
            System.err.println(String.format("[%s上传] 异常：%s", type, e.getMessage()));
            return String.format("上传异常：%s", e.getMessage());
        }
    }
}