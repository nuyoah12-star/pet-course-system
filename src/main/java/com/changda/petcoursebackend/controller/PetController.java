package com.changda.petcoursebackend.controller;

import com.changda.petcoursebackend.entity.Pet;
import com.changda.petcoursebackend.mapper.PetMapper;
import com.changda.petcoursebackend.service.PetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/pet")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:8081"}, allowCredentials = "true")
public class PetController {

    private static final Logger logger = LoggerFactory.getLogger(PetController.class);

    @Autowired
    private PetService petService;

    @Autowired
    private PetMapper petMapper;

    // ========== 核心修改：使用正斜杠，避免 Unicode 转义错误 ==========
    private static final String AVATAR_BASE_URL = "http://localhost:8080/uploads/";
    private static final String LOCAL_UPLOAD_PATH = "C:/Users/Administrator/Desktop/pet-course-backend/uploads/";

    // ========== Java原生MD5计算工具方法 ==========
    private String calculateFileMd5(MultipartFile file) throws IOException {
        try (var inputStream = file.getInputStream()) {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                md.update(buffer, 0, len);
            }
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("MD5算法不可用", e);
        }
    }

    private String calculateLocalFileMd5(File file) throws IOException {
        try (var inputStream = Files.newInputStream(file.toPath())) {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                md.update(buffer, 0, len);
            }
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new IOException("计算文件MD5失败", e);
        }
    }

    // ========== 项目根目录查询接口 ==========
    @GetMapping("/getPath")
    public String getProjectRootPath() {
        String rootPath = System.getProperty("user.dir");
        System.out.println("===== 项目根目录路径 =====");
        System.out.println(rootPath);
        System.out.println("本地上传路径：" + LOCAL_UPLOAD_PATH);
        System.out.println("========================");
        return "项目根目录：" + rootPath + " | 本地上传路径：" + LOCAL_UPLOAD_PATH;
    }

    // ========== 拼接头像完整URL ==========
    private List<Pet> buildPetAvatarUrl(List<Pet> petList) {
        if (petList == null || petList.isEmpty()) {
            return petList;
        }
        for (Pet pet : petList) {
            String avatar = pet.getAvatar();
            if (avatar != null && !avatar.isEmpty() && !avatar.startsWith("http")) {
                pet.setAvatar(AVATAR_BASE_URL + avatar);
                logger.debug("宠物{}的头像文件名{}已拼接为完整URL：{}", pet.getName(), avatar, pet.getAvatar());
            } else if (avatar != null && avatar.startsWith("http")) {
                logger.debug("宠物{}的头像已是完整URL，直接使用：{}", pet.getName(), avatar);
            }
        }
        return petList;
    }

    // ========== 原有接口（保留+优化） ==========
    @GetMapping
    public List<Pet> getAllPets() {
        List<Pet> petList = petService.getAllPets();
        return buildPetAvatarUrl(petList);
    }

    @GetMapping("/list")
    public List<Pet> listAllPets() {
        return this.getAllPets();
    }

    @GetMapping("/{petId}")
    public Pet getPetById(@PathVariable Integer petId) {
        Pet pet = petService.getPetById(petId);
        if (pet != null) {
            String avatar = pet.getAvatar();
            if (avatar != null && !avatar.isEmpty() && !avatar.startsWith("http")) {
                pet.setAvatar(AVATAR_BASE_URL + avatar);
                logger.debug("宠物{}（ID:{}）的头像文件名{}已拼接为完整URL：{}",
                        pet.getName(), petId, avatar, pet.getAvatar());
            } else if (avatar != null && avatar.startsWith("http")) {
                logger.debug("宠物{}（ID:{}）的头像已是完整URL，直接使用：{}",
                        pet.getName(), petId, avatar);
            }
        }
        return pet;
    }

    @GetMapping("/rank")
    public List<Pet> getPetRank() {
        List<Pet> petList = petService.getPetRankList();
        return buildPetAvatarUrl(petList);
    }

    @PostMapping
    public String addPet(@RequestBody Pet pet) {
        try {
            petService.addPet(pet);
            return "添加宠物成功";
        } catch (Exception e) {
            logger.error("新增宠物失败", e);
            return "添加失败: " + e.getMessage();
        }
    }

    // ========== 头像上传接口 ==========
    @PostMapping("/upload/avatar")
    public String uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return "上传失败：文件为空";
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "上传失败：文件格式不合法（无后缀）";
        }
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        List<String> allowSuffix = Arrays.asList(".jpg", ".jpeg", ".png", ".gif");
        if (!allowSuffix.contains(suffix.toLowerCase())) {
            return "上传失败：仅支持jpg/jpeg/png/gif格式";
        }

        // 3. 使用本地绝对路径创建uploads文件夹
        File dir = new File(LOCAL_UPLOAD_PATH);
        if (!dir.exists()) {
            boolean isCreated = dir.mkdirs();
            logger.info("上传目录创建结果：{}，路径：{}", isCreated ? "成功" : "失败", LOCAL_UPLOAD_PATH);
        }

        try {
            String currentFileMd5 = calculateFileMd5(file);
            logger.info("当前上传文件MD5：{}", currentFileMd5);

            File[] existingFiles = dir.listFiles();
            if (existingFiles != null) {
                for (File existFile : existingFiles) {
                    if (!existFile.isFile()) continue;
                    String existFileMd5 = calculateLocalFileMd5(existFile);
                    if (currentFileMd5.equals(existFileMd5)) {
                        logger.info("文件内容已存在，直接返回已有文件名：{}", existFile.getName());
                        return existFile.getName();
                    }
                }
            }

            String fileName = UUID.randomUUID().toString() + suffix.toLowerCase();
            File destFile = new File(dir, fileName);
            if (destFile.exists()) {
                boolean isDeleted = destFile.delete();
                logger.info("同名文件删除结果：{}，文件路径：{}", isDeleted ? "成功" : "失败", destFile.getAbsolutePath());
            }

            file.transferTo(destFile);
            logger.info("新头像上传成功，文件保存路径：{}", destFile.getAbsolutePath());
            return fileName;
        } catch (IOException e) {
            logger.error("头像上传失败", e);
            return "上传失败：" + e.getMessage();
        }
    }

    // ========== 更新宠物头像接口 ==========
    @PostMapping("/avatar")
    public String updateAvatar(@RequestParam Integer petId, @RequestParam String avatar) {
        try {
            if (petId == null || petId <= 0) {
                return "更新失败：宠物ID非法";
            }
            if (avatar == null || avatar.trim().isEmpty()) {
                return "更新失败：头像地址不能为空";
            }

            Pet pet = petService.getPetById(petId);
            if (pet == null) {
                return "更新失败：未找到ID为" + petId + "的宠物";
            }

            petMapper.updateAvatar(petId, avatar);
            logger.info("宠物{}（ID:{}）头像更新成功：{}", pet.getName(), petId, avatar);
            return "头像更新成功";
        } catch (Exception e) {
            logger.error("更新宠物头像失败", e);
            return "更新失败：" + e.getMessage();
        }
    }
}