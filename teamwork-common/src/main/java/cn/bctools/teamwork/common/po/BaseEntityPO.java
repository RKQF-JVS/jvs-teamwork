package cn.bctools.teamwork.common.po;

import cn.bctools.common.entity.dto.UserDto;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 这包含了一些 新增和更新的字段
 *
 * @author admin
 */
@Data
public class BaseEntityPO {

    @ApiModelProperty("创建时间")
    @DateTimeFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN, timezone = "GMT+8")
    private LocalDateTime createTime;

    @ApiModelProperty("创建者")
    private String createBy;

    @ApiModelProperty("创建者id")
    private String createById;

    @ApiModelProperty("更新人")
    private String updateBy;

    @ApiModelProperty("更新时间")
    @DateTimeFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN, timezone = "GMT+8")
    private LocalDateTime updateTime;

    /**
     * 初始化新增字段
     *
     * @param userDto
     */
    public BaseEntityPO initSave(UserDto userDto) {
        String realName = userDto.getRealName();
        String accountName = userDto.getAccountName();
        String name = StrUtil.isEmpty(realName) ? accountName : realName;
        LocalDateTime now = LocalDateTime.now();
        this.createTime = now;
        this.updateTime = now;
        this.createBy = name;
        this.createById = userDto.getId();
        this.updateBy = name;
        return this;
    }

    /**
     * 初始化更新字段
     *
     * @param userDto
     */
    public BaseEntityPO initUpdate(UserDto userDto) {
        String realName = userDto.getRealName();
        String accountName = userDto.getAccountName();
        this.updateTime = LocalDateTime.now();
        this.updateBy = StrUtil.isEmpty(realName) ? accountName : realName;
        return this;
    }
}
