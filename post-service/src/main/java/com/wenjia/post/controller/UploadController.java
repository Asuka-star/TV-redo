package com.wenjia.post.controller;


import com.wenjia.common.exception.UploadException;
import com.wenjia.common.result.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
@Tag(name = "上传接口")
public class UploadController {

    @PutMapping
    public Result<String> upload(@RequestParam("file")MultipartFile file){
        if(file.isEmpty()) throw new UploadException("未选择文件");
        try {
            //生成唯一文件名
            String originalName = file.getName();
            String fileExt = originalName.substring(originalName.lastIndexOf("."));
            String fileName = UUID.randomUUID() + fileExt;
            //保存文件
            String UPLOAD_DIR = "E:/uploads";
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) uploadDir.mkdirs();
            String filePath = UPLOAD_DIR +"/"+ fileName;
            file.transferTo(new File(filePath));
            //返回访问URL
            String accessUrl = "http://localhost:8080/uploads/" + fileName;
            return Result.success(accessUrl);
        } catch (Exception e) {
            throw new RuntimeException("上传失败");
        }
    }
}