package com.thinkmore.forum.service;

import com.thinkmore.forum.configuration.Config;
import com.thinkmore.forum.entity.Img;
import com.thinkmore.forum.repository.ImgRepository;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImgService {
    private final ImgRepository imgRepository;
    private final MinioClient minioClient;

    @Value("${minio.url}")
    private String minioUrl;


    @Transactional
    public Img upload(MultipartFile img) throws Exception {
        InputStream imgIs = new ByteArrayInputStream(img.getBytes());
        String md5 = DigestUtils.md5Hex(imgIs);

        // check
        Optional<Img> image = imgRepository.findByMd5(md5);
        if (image.isPresent()) {
            return image.get();
        }

        // put
        String fileName = img.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".png") && !fileName.endsWith(".jpg"))) {
            throw new RuntimeException();
        }
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(Config.BucketName)
                        .object(fileName)
                        .stream(imgIs, -1, 5 * 1024 * 1024)
                        .contentType(fileName.endsWith(".png") ? "image/png" : "image/jpeg")
                        .build());

        // set
        Img theImg = new Img();
        theImg.setUrl(minioUrl + "/" + Config.BucketName + "/" + fileName);
        theImg.setMd5(md5);
        imgRepository.save(theImg);

        return theImg;
    }
}
