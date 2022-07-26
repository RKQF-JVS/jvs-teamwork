package cn.bctools.teamwork.dto;

import cn.bctools.oss.dto.BaseFile;
import cn.bctools.teamwork.entity.ProjectInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 项目文件上传参数
 * @author admin
 */
@Data
@ApiModel("项目文件上传参数")
public class ProjectFileUploadDTO extends ProjectInfo {

    @ApiModelProperty(value = "上传的文件信息",required = true)
    private BaseFile baseFile;

}
