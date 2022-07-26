package cn.bctools.teamwork.common.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 公共的配置
 * @author admin
 */
@Configuration
@ConfigurationProperties(prefix = "common")
@Data
public class CommonConfig {
    /**
     * 桶名称
     */
    private String bucketName;

    /**
     * 任务导入模板 文件名称
     */
    private String taskImportTemplate;

}
