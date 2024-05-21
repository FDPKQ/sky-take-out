package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.MinIOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/admin/common")
public class CommonController {
    private final static String BUCKET_NAME = "cqwm";
    private final static String IMAGE_BASE_URL = "https://minio.lan.luoxianjun.com/" + BUCKET_NAME + "/";


    @Resource
    private MinIOUtils minIOUtils;


    @PostMapping("/upload")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile image) {
        try {
            // 获取原始文件名称
            String originalFilename = image.getOriginalFilename();

            // 生成新文件名
            String suffix = originalFilename == null || originalFilename.isEmpty() ? originalFilename : originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFileName = UUID.randomUUID() + suffix;

            // 保存文件
            minIOUtils.uploadFile(image.getInputStream(), BUCKET_NAME, newFileName);

            // 返回结果
            String filePath = IMAGE_BASE_URL + newFileName;
            log.info("文件上传成功，{}", filePath);
            return Result.success(filePath);
        } catch (Exception e) {
            log.error(MessageConstant.UPLOAD_FAILED, e);
            return Result.error(MessageConstant.UPLOAD_FAILED);
        }

    }
}
