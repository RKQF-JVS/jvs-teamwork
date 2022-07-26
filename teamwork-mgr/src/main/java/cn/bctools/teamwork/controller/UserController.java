package cn.bctools.teamwork.controller;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.R;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * @author admin
 * @date ：Created in 2022/1/18 16:11
 * [description]：
 */
@Api(tags = "用户体系")
@RestController
@AllArgsConstructor
@RequestMapping("/user")
public class UserController {

    @GetMapping
    @ApiOperation("获取当前用户")
    public R<List<UserDto>> getUserList() {
        List<UserDto> userDtoS = Arrays.asList(UserCurrentUtils.getCurrentUser());
        return R.ok(userDtoS);
    }

}
