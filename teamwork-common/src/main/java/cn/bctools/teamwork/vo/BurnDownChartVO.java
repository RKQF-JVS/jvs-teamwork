package cn.bctools.teamwork.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 项目任务燃尽图展示
 *
 * @author admin
 */
@Data
@ApiModel("项目任务燃尽图展示")
public class BurnDownChartVO {
    @ApiModelProperty("燃尽图开始时间，项目开始时间 格式 yyyy-MM-dd")
    private String startTime;
    @ApiModelProperty("燃尽图结束时间，项目结束时间 格式 yyyy-MM-dd")
    private String endTime;

    @ApiModelProperty("燃尽图展示开始时间，默认取项目开始时间，若结束时间大于等于10天 从结束时间取 格式 yyyy-MM-dd")
    private String ViewStartTime;

    @ApiModelProperty("燃尽图展示结束时间，默认取当前时间，若大于项目结束时间，取项目结束时间 格式 yyyy-MM-dd")
    private String ViewEndTime;

    @ApiModelProperty("燃尽图预测开始条数，统计项目下任务总条数，默认为0")
    private Integer initStartCount = 0;

    @ApiModelProperty("燃尽图预测结束条数，默认为0")
    private Integer initEndCount = 0;

    @ApiModelProperty("燃尽图预测 展示开始条数")
    private Integer viewStartCount=0;

    @ApiModelProperty("燃尽图预测 展示结束条数")
    private Integer viewEndCount=0;
    @ApiModelProperty("燃尽图实际的显示条数和时间")
    List<RealBurnDownChartVO> real;

    @ApiModelProperty("燃尽图预测显示条数和时间")
    List<ForecastBurnDownChartVO> forecast;

    @Data
    public static class RealBurnDownChartVO{
        @ApiModelProperty("显示日期")
        private String date;
        @ApiModelProperty("日期对应条数")
        private Integer count;

    }

    @Data
    public static class ForecastBurnDownChartVO{
        @ApiModelProperty("显示日期")
        private String date;
        @ApiModelProperty("日期对应条数")
        private Double count;

    }
}
