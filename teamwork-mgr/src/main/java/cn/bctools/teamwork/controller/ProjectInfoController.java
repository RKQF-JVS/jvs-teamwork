package cn.bctools.teamwork.controller;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.R;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.oss.dto.BaseFile;
import cn.bctools.oss.template.OssTemplate;
import cn.bctools.teamwork.common.common.config.CommonConfig;
import cn.bctools.teamwork.common.enums.CollectionTypeEnum;
import cn.bctools.teamwork.common.enums.FileRelationTypeEnum;
import cn.bctools.teamwork.common.enums.SelectTypeEnum;
import cn.bctools.teamwork.dto.ProjectFileUploadDTO;
import cn.bctools.teamwork.dto.ProjectMemberInvite;
import cn.bctools.teamwork.dto.ProjectQueryDTO;
import cn.bctools.teamwork.entity.ProjectCollection;
import cn.bctools.teamwork.entity.ProjectFileInfo;
import cn.bctools.teamwork.entity.ProjectInfo;
import cn.bctools.teamwork.entity.ProjectMember;
import cn.bctools.teamwork.service.ProjectCollectionService;
import cn.bctools.teamwork.service.ProjectInfoService;
import cn.bctools.teamwork.service.ProjectMemberService;
import cn.bctools.teamwork.vo.BurnDownChartVO;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 项目信息表
 * <p>
 * FIXME 需要注意，该类为自动生成，需结合真实业务进行修改
 *
 * @author Auto Generator
 */
@Api(tags = "项目信息表")
@RestController
@AllArgsConstructor
@RequestMapping("/ProjectInfo")
public class ProjectInfoController {
    private final ProjectInfoService service;
    private final OssTemplate ossTemplate;
    private final ProjectMemberService projectMemberService;
    private final ProjectCollectionService projectCollectionService;
    private final CommonConfig commonConfig;


    @Log
    @ApiOperation("分页")
    @GetMapping("/page")
    public R<Page<ProjectInfo>> page(Page<ProjectInfo> page, ProjectQueryDTO dto) {
        LambdaQueryWrapper<ProjectInfo> query = getQuery(dto);
        if (ObjectUtil.isNull(query)) {
            return R.ok(page);
        }
        Page<ProjectInfo> projectInfoPage = service.page(page, query);
        //设置创建人姓名
        if (projectInfoPage.getRecords().size() > BigDecimal.ROUND_UP) {
            List<String> projectIds = projectInfoPage.getRecords().stream().map(ProjectInfo::getId).collect(Collectors.toList());
            //是否收藏
            Map<String, ProjectCollection> collectionMap = projectCollectionService.list(new LambdaQueryWrapper<ProjectCollection>().in(ProjectCollection::getObjectId, projectIds).eq(ProjectCollection::getMemberId, UserCurrentUtils.getCurrentUser().getId()).eq(ProjectCollection::getType, CollectionTypeEnum.PROJECT.getCode()))
                    .stream().collect(Collectors.toMap(ProjectCollection::getObjectId, Function.identity()));
            projectInfoPage.getRecords()
                    .stream()
                    .peek(e -> e.setCollectStatus(collectionMap.containsKey(e.getId())))
                    .collect(Collectors.toList());

        }
        return R.ok(projectInfoPage);
    }


    @Log
    @ApiOperation("项目燃尽图")
    @GetMapping("/burnDownChart")
    public R<BurnDownChartVO> burnDownChart(String projectId) {
        if (StrUtil.isEmpty(projectId)) {
            throw new BusinessException("请选择项目！");
        }
        BurnDownChartVO result = service.burnDownChart(projectId);
        return R.ok(result);
    }

    /***
     * 功能描述: <br>
     * 〈条件构造器〉
     * @param dto 入参
     * @return com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<cn.bctools.teamwork.entity.ProjectInfo>
     */
    private LambdaQueryWrapper<ProjectInfo> getQuery(ProjectQueryDTO dto) {
        UserDto user = UserCurrentUtils.getCurrentUser();
        LambdaQueryWrapper<ProjectInfo> queryWrapper = new LambdaQueryWrapper<>();
        //获取当前登录用户所在的项目id
        List<ProjectMember> projectMembers = projectMemberService.list(new LambdaQueryWrapper<ProjectMember>().and(e -> e.eq(ProjectMember::getMemberId, user.getId()).or().eq(ProjectMember::getOwnerId, user.getId())));
        if (projectMembers.isEmpty()) {
            return null;
        }
        if (ObjectUtil.isNull(dto.getSelectType()) || SelectTypeEnum.MY.equals(dto.getSelectType())) {
            List<String> ids = projectMembers.stream().map(ProjectMember::getProjectId).collect(Collectors.toList());
            queryWrapper.and(e -> e.in(!projectMembers.isEmpty(), ProjectInfo::getId, ids).or().eq(ProjectInfo::getIsPrivate, BigDecimal.ROUND_UP))
                    .eq(ProjectInfo::getRecycleFlag, BigDecimal.ROUND_UP);
        }
        //类型不能为空
        if (ObjectUtil.isNotNull(dto.getSelectType()) && !SelectTypeEnum.MY.equals(dto.getSelectType())) {
            queryWrapper = getQueryType(dto, queryWrapper, projectMembers);
        }
        return queryWrapper;
    }

    /***
     * 功能描述: <br>
     * 〈通过type 生成条件〉
     * @param queryWrapper 已经定义好了的条件构造器
     * @param dto 入参
     * @param projectMembers 与当前登录人相关的数据
     * @Return: void
     */
    private LambdaQueryWrapper<ProjectInfo> getQueryType(ProjectQueryDTO dto, LambdaQueryWrapper<ProjectInfo> queryWrapper, List<ProjectMember> projectMembers) throws BusinessException {
        Integer delete = 0;
        List<String> ids = projectMembers.stream().map(ProjectMember::getProjectId).collect(Collectors.toList());
        switch (dto.getSelectType()) {
            case ARCHIVE:
                queryWrapper.eq(ProjectInfo::getIsArchive, BigDecimal.ROUND_DOWN).in(ProjectInfo::getId, ids);
                break;
            case COLLECT:
                //收藏需要去查询收藏的数据
                List<ProjectCollection> list = projectCollectionService.list(new LambdaQueryWrapper<ProjectCollection>().eq(ProjectCollection::getType, CollectionTypeEnum.PROJECT.getCode()).eq(ProjectCollection::getMemberId, UserCurrentUtils.getCurrentUser().getId()));
                if (list.isEmpty()) {
                    return null;
                }
                List<String> coll = list.stream().map(ProjectCollection::getObjectId).collect(Collectors.toList());
                queryWrapper.in(ProjectInfo::getId, coll);
                break;
            case DELETE:
                queryWrapper.in(ProjectInfo::getId, ids);
                delete = 1;
                break;
            default:
                throw new BusinessException("类型不匹配!");
        }
        queryWrapper.eq(ProjectInfo::getRecycleFlag, delete);
        return queryWrapper;
    }


    @Log
    @ApiOperation("详情")
    @GetMapping("/detail")
    public R<ProjectInfo> detail(ProjectInfo dto) {
        UserDto currentUser = UserCurrentUtils.getCurrentUser();
        //TODO 这里需要返回一个头像数据
        ProjectInfo projectInfo = service.detail(dto);
        ProjectCollection collection = projectCollectionService.lambdaQuery().eq(ProjectCollection::getObjectId, dto.getId())
                .eq(ProjectCollection::getMemberId, currentUser.getId())
                .eq(ProjectCollection::getType, CollectionTypeEnum.PROJECT.getCode())
                .one();
        boolean collectStatus = false;
        if (ObjectUtil.isNotNull(collection)) {
            collectStatus = true;
        }
        projectInfo.setCollectStatus(collectStatus);
        //获取外链
        if (StrUtil.isNotEmpty(projectInfo.getProjectCover())) {
            String s = ossTemplate.fileLink(projectInfo.getProjectCover(), commonConfig.getBucketName());
            projectInfo.setProjectCoverUrl(s);
        }
        return R.ok(projectInfo);
    }

    @Log
    @ApiOperation("所有未在回收站的项目信息")
    @GetMapping("/listAll")
    public R<List<ProjectInfo>> listAll() {
        LambdaQueryWrapper<ProjectInfo> query = getQuery(new ProjectQueryDTO());
        if (ObjectUtil.isEmpty(query)) {
            return R.ok(new ArrayList<>());
        }
        List<ProjectInfo> list = service.list(query);
        if (CollUtil.isNotEmpty(list)) {
            for (ProjectInfo projectInfo : list) {
                //获取外链
                if (StrUtil.isNotEmpty(projectInfo.getProjectCover())) {
                    String s = ossTemplate.fileLink(projectInfo.getProjectCover(), commonConfig.getBucketName());
                    projectInfo.setProjectCoverUrl(s);
                }
            }
        }
        return R.ok(list);
    }

    @Log
    @ApiOperation("新增")
    @PostMapping("/save")
    public R<ProjectInfo> save(@RequestBody ProjectInfo dto) {
        UserDto userDto = UserCurrentUtils.getCurrentUser();
        return service.saveProject(dto, userDto);
    }


    @Log
    @ApiOperation("修改")
    @PutMapping("/edit")
    public R<ProjectInfo> edit(@RequestBody ProjectInfo dto) {
        if (StrUtil.isEmpty(dto.getId())) {
            throw new BusinessException("修改项目缺少数据");
        }
        UserDto userDto = UserCurrentUtils.getCurrentUser();
        return R.ok(service.edit(dto, userDto));
    }

    @Log
    @ApiOperation("退出项目")
    @DeleteMapping("/exitProject")
    public R<Boolean> exitProject(@RequestParam String projectId) {
        if (StrUtil.isEmpty(projectId)) {
            throw new BusinessException("未获取到项目信息");
        }
        ProjectInfo byId = service.getById(projectId);
        if (BeanUtil.isEmpty(byId)) {
            throw new BusinessException("未获取到项目信息");
        }
        UserDto currentUser = UserCurrentUtils.getCurrentUser();
        String userId = currentUser.getId();
        if (userId.equals(byId.getCreateById())) {
            throw new BusinessException("创建者不能退出！");
        }
        ProjectMemberInvite dto = new ProjectMemberInvite();
        dto.setProjectId(projectId);
        dto.setMemberId(userId);
        dto.setMemberName(currentUser.getRealName());
        //todo 暂时就从项目成员中移除，其他逻辑待完善
        Boolean aBoolean = projectMemberService.removeMember(dto, currentUser, false);
        return R.ok(aBoolean);
    }

    @Log
    @ApiOperation("项目移入到回收站")
    @DeleteMapping("/recycle")
    public R<Boolean> recycle(@RequestBody ProjectInfo dto) {
        if (StrUtil.isEmpty(dto.getId())) {
            throw new BusinessException("未获取到项目信息");
        }
        UserDto userDto = UserCurrentUtils.getCurrentUser();
        return R.ok(service.recycle(dto, userDto));
    }


    @Log
    @ApiOperation("将项目从回收站恢复")
    @PostMapping("/recovery")
    public R<Boolean> recovery(@RequestBody ProjectInfo dto) {
        if (StrUtil.isEmpty(dto.getId())) {
            throw new BusinessException("未获取到项目信息");
        }
        UserDto userDto = UserCurrentUtils.getCurrentUser();
        return R.ok(service.recovery(dto, userDto));
    }

    @Log
    @ApiOperation("项目归档")
    @PostMapping("/archive")
    public R<Boolean> archive(@RequestBody ProjectInfo dto) {
        if (StrUtil.isEmpty(dto.getId())) {
            throw new BusinessException("未获取到项目信息");
        }
        UserDto userDto = UserCurrentUtils.getCurrentUser();
        return R.ok(service.archive(dto, userDto));
    }

    @Log
    @ApiOperation("项目归档恢复")
    @PostMapping("/recoveryArchive")
    public R<Boolean> recoveryArchive(@RequestBody ProjectInfo dto) {
        if (StrUtil.isEmpty(dto.getId())) {
            throw new BusinessException("未获取到项目信息");
        }
        UserDto userDto = UserCurrentUtils.getCurrentUser();
        return R.ok(service.recoveryArchive(dto, userDto));
    }

    @Log
    @ApiOperation("保存项目文件上传")
    @PostMapping("/uploadProjectFile")
    public R<Boolean> uploadProjectFile(@RequestBody ProjectFileUploadDTO dto) {
        if (StrUtil.isEmpty(dto.getId())) {
            throw new BusinessException("请选择项目！");
        }
        ProjectInfo projectInfo = service.getById(dto.getId());
        if (BeanUtil.isEmpty(projectInfo)) {
            throw new BusinessException("项目已失效！");
        }
        if ((projectInfo.getRecycleFlag() == 1)) {
            throw new BusinessException("项目在回收站，无法编辑！");
        }
        BaseFile baseFile = dto.getBaseFile();
        ProjectFileInfo fileInfo = new ProjectFileInfo();
        fileInfo.setFileName(baseFile.getFileName())
                .setFileType(baseFile.getFileType())
                .setProjectId(projectInfo.getId())
                .setOriginName(baseFile.getOriginalName())
                .setExtension(baseFile.getFileType())
                .setSize(baseFile.getSize())
                .setObjectType(FileRelationTypeEnum.PROJECT_UPLOAD.getCode());
        UserDto user = UserCurrentUtils.getCurrentUser();
        return R.ok(service.uploadProjectFile(fileInfo, projectInfo, user));
    }
}
