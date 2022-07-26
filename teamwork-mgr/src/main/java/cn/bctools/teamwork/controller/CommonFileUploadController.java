package cn.bctools.teamwork.controller;

import cn.bctools.common.utils.R;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.log.annotation.Log;
import cn.bctools.oss.dto.BaseFile;
import cn.bctools.oss.template.OssTemplate;
import cn.bctools.teamwork.common.common.config.CommonConfig;
import cn.hutool.core.lang.Dict;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 公共的文件上传
 *
 * @author admin
 */
@Api(tags = "公共文件上传")
@RestController
@AllArgsConstructor
@RequestMapping("/fileUpload")
public class CommonFileUploadController {
    OssTemplate ossTemplate;
    CommonConfig commonConfig;

    @Log
    @ApiOperation("文件上传")
    @PostMapping("/uploadProjectFile")
    public R<BaseFile> taskRelationFile(@RequestPart("file") MultipartFile file) {
        String applicationContextName = SpringContextUtil.getApplicationContextName();
        BaseFile baseFile = ossTemplate.putFile(commonConfig.getBucketName(), applicationContextName, file.getOriginalFilename(), file);
        return R.ok(baseFile);
    }

    @Log
    @ApiOperation("获取文件链接")
    @GetMapping("/get/url")
    public R<Dict> taskRelationFile(@RequestParam String fileName) {
        String fileLink = ossTemplate.fileLink(fileName, commonConfig.getBucketName());
        Dict dict = new Dict().set("url", fileLink);
        return R.ok(dict);
    }
}
