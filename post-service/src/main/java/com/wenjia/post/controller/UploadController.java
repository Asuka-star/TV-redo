package com.wenjia.post.controller;


import com.wenjia.common.exception.UploadException;
import com.wenjia.common.result.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
@Tag(name = "上传接口")
public class UploadController {

    //todo 这里需要去了解一下别人是怎么完成的
    // 不然我感觉别人上传了图片之后又不进行提交post
    // 但是这样也会让图片保存在本地

    @PostMapping
    public Result<String> upload(@RequestParam("file")MultipartFile file){
        if(file.isEmpty()) throw new UploadException("未选择文件");
        try {
            //生成唯一文件名
            String originalName = file.getOriginalFilename();
            String fileExt = Objects.requireNonNull(originalName).substring(originalName.lastIndexOf("."));
            String fileName = UUID.randomUUID() + fileExt;
            //保存文件
            String UPLOAD_DIR = "E:/uploads";
            Path path= Paths.get(UPLOAD_DIR,fileName);
            file.transferTo(path);
            //返回访问URL
            String accessUrl = "http://localhost:8080/uploads/" + fileName;
            return Result.success(accessUrl);
        } catch (Exception e) {
            throw new RuntimeException("上传失败");
        }
    }
}