package com.wenjia.post.controller;


import com.wenjia.common.exception.UploadException;
import com.wenjia.common.result.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/upload")
@Tag(name = "上传接口")
public class UploadController {

    @PutMapping
    public Result<String> upload(@RequestParam(required = false) HttpServletRequest request){
        try {
            //获取文件
            Part filePart = request.getPart("file");
            if (filePart == null || filePart.getSize() == 0) {
                throw new UploadException("未选择文件");
            }
            //生成唯一文件名
            String originalName = filePart.getSubmittedFileName();
            String fileExt = originalName.substring(originalName.lastIndexOf("."));
            String fileName = UUID.randomUUID() + fileExt;
            //保存文件
            String UPLOAD_DIR = "E:/uploads";
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) uploadDir.mkdirs();
            String filePath = UPLOAD_DIR +"/"+ fileName;
            filePart.write(filePath);
            //返回访问URL
            String accessUrl = "http://localhost:8080/TopView_war/uploads/" + fileName;
            return Result.success(accessUrl);
        } catch (Exception e) {
            throw new RuntimeException("上传失败");
        }
    }
}