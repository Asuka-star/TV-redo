package com.wenjia.post.controller;


import com.wenjia.common.exception.UploadException;
import com.wenjia.common.result.Result;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
@Tag(name = "上传接口")
@RequiredArgsConstructor
public class UploadController {
    //todo 这里需要去了解一下别人是怎么完成的
    // 不然我感觉别人上传了图片之后又不进行提交post
    // 但是这样也会让图片保存在本地
    private final MinioClient minioClient;

    @PostMapping
    public Result<String> upload(@RequestParam("file")MultipartFile file){
        if(file.isEmpty()) {
            throw new UploadException("未选择文件");
        }
        try {
            //生成唯一文件名
            String originalName = file.getOriginalFilename();
            String fileExt = Objects.requireNonNull(originalName).substring(originalName.lastIndexOf("."));
            String fileName = UUID.randomUUID() + fileExt;
            //保存文件在minio上面
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket("tvredo")
                            .object(fileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());
            //返回访问URL
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket("tvredo")
                            .object(fileName)
                            .build()
            );
            return Result.success(url);
        } catch (Exception e) {
            throw new RuntimeException("上传失败");
        }
    }
}