package cn.bctools.teamwork.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import cn.bctools.teamwork.common.po.BaseEntityPO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author Auto Generator
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("项目文件信息表")
@EqualsAndHashCode(callSuper = false)
@TableName("project_file_info")
public class ProjectFileInfo extends BaseEntityPO implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty("主键id")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    @ApiModelProperty("原文件名称")
    @TableField("origin_name")
    private String originName;
    @ApiModelProperty("上传后的文件名")
    @TableField("file_name")
    private String fileName;
    @ApiModelProperty("扩展名")
    @TableField("extension")
    private String extension;
    @ApiModelProperty("文件大小")
    @TableField("size")
    private Long size;
    @ApiModelProperty("对象类型")
    @TableField("object_type")
    private String objectType;
    @ApiModelProperty("组织id")
    @TableField("organization_id")
    private String organizationId;
    @ApiModelProperty("任务id")
    @TableField("task_id")
    private String taskId;
    @ApiModelProperty("项目id")
    @TableField("project_id")
    private String projectId;
    @ApiModelProperty("下载次数")
    @TableField("downloads")
    private String downloads;
    @ApiModelProperty("额外信息")
    @TableField("extra")
    private String extra;
    @ApiModelProperty("完整地址")
    @TableField("file_url")
    private String fileUrl;
    @ApiModelProperty("文件类型")
    @TableField("file_type")
    private String fileType;
    @ApiModelProperty("删除标记")
    @TableField("del_flag")
    private Integer delFlag;
    @ApiModelProperty("回收站标记 0-不在回收站 1-在回收站")
    @TableField("recycle_flag")
    private Integer recycleFlag;
    @ApiModelProperty("删除时间")
    @TableField("deleted_time")
    private LocalDateTime deletedTime;
    @ApiModelProperty("所属租户id")
    @TableField("tenant_id")
    private String tenantId;

}
