package com.ljm.swagger.controller;

import com.ljm.swagger.response.CommonResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("file")
@Api(tags = "API for file")
public class FileController {

    Logger logger = LoggerFactory.getLogger(FileController.class);


    @PostMapping(value = "/simple", headers = "content-type=multipart/form-data")
    public CommonResult uploadSimpleFile(@ApiParam(value = "upload file") @RequestPart() MultipartFile file){

        logger.info(file.getOriginalFilename());

        return CommonResult.success(null);

    }


    @PostMapping("/multiple")
    public CommonResult uploadMultipleFile(@ApiParam(value = "upload files") @RequestPart() List<MultipartFile> files){

        for(MultipartFile file: files){
            logger.info(file.getOriginalFilename());
        }

        return CommonResult.success(null);

    }




}
