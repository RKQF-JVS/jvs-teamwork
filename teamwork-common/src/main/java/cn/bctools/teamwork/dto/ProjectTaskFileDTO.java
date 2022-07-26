package cn.bctools.teamwork.dto;

import cn.bctools.oss.dto.BaseFile;
import cn.bctools.teamwork.entity.ProjectTask;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 任务文件上传参数
 *
 * @author admin
 */
@Data
@ApiModel("任务文件上传参数")
public class ProjectTaskFileDTO extends ProjectTask {
    @ApiModelProperty(value = "上传的文件信息", required = true)
    private BaseFile baseFile;
}
