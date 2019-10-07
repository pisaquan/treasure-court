package com.sancai.oa.examine.controller;


import com.sancai.oa.core.ApiResponse;
import com.sancai.oa.core.version.ApiVersion;
import com.sancai.oa.examine.entity.ExamineDTO;
import com.sancai.oa.examine.entity.ExamineTypeDTO;
import com.sancai.oa.examine.entity.enums.ExamineTypeEnum;
import com.sancai.oa.examine.exception.EnumExamineError;
import com.sancai.oa.examine.service.IExamineService;
import com.sancai.oa.log.config.Log;
import com.sancai.oa.log.config.LogModelEnum;
import com.sancai.oa.log.config.LogOperationTypeEnum;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * <p>
 * 审批表单 前端控制器
 * </p>
 *
 * @author quanleilei
 * @since 2019-07-24
 */
@ApiVersion(1)
@RestController
@RequestMapping("{version}/examine")
public class ExamineController {

    @Autowired
    private IExamineService examineService;

    /**
     * 新增审批模板
     * @param examineDTO
     * @return
     */
    @PostMapping("examine_create")
    @Log(type=LogOperationTypeEnum.SAVE,model=LogModelEnum.EXAMINE_TEMPLATE)
    public ApiResponse examineCreate(@RequestBody ExamineDTO examineDTO) {
        if(StringUtils.isBlank(examineDTO.getCode())){
            return ApiResponse.fail(EnumExamineError.EXAMINE_CODE_IS_EMPTY);
        }
        if(StringUtils.isBlank(examineDTO.getName())){
            return ApiResponse.fail(EnumExamineError.EXAMINE_NAME_IS_EMPTY);
        }
        if(StringUtils.isBlank(examineDTO.getExamineGroup())){
            return ApiResponse.fail(EnumExamineError.EXAMINE_GROUP_IS_EMPTY);
        }
        if(StringUtils.isBlank(examineDTO.getCompanyId())){
            return ApiResponse.fail(EnumExamineError.COMPANY_ID_IS_EMPTY);
        }
        boolean isRepetition = examineService.repetitionExamineAdd(examineDTO);
        if(isRepetition){
            return ApiResponse.fail(EnumExamineError.EXAMINE_NAME_GROUP_REPTITION);
        }
        boolean isSuccess = examineService.examineCreate(examineDTO);
        if(!isSuccess){
            return ApiResponse.fail(EnumExamineError.EXAMINE_CREATE_FAIL);
        }
        return ApiResponse.success();
    }

    /**
     * 修改审批模板
     * @param examineDTO
     * @return
     */
    @PostMapping("examine_modify")
    @Log(type=LogOperationTypeEnum.UPDATE,model=LogModelEnum.EXAMINE_TEMPLATE)
    public ApiResponse examineModify(@RequestBody ExamineDTO examineDTO) {

        if(StringUtils.isBlank(examineDTO.getCode())){
            return ApiResponse.fail(EnumExamineError.EXAMINE_CODE_IS_EMPTY);
        }
        if(StringUtils.isBlank(examineDTO.getName())){
            return ApiResponse.fail(EnumExamineError.EXAMINE_NAME_IS_EMPTY);
        }
        if(StringUtils.isBlank(examineDTO.getExamineGroup())){
            return ApiResponse.fail(EnumExamineError.EXAMINE_GROUP_IS_EMPTY);
        }
        if(StringUtils.isBlank(examineDTO.getId())){
            return ApiResponse.fail(EnumExamineError.EXAMINE_ID_IS_EMPTY);
        }
        boolean isRepetition = examineService.repetitionExamineModify(examineDTO);
        if(isRepetition){
            return ApiResponse.fail(EnumExamineError.EXAMINE_NAME_GROUP_REPTITION);
        }
        boolean isSuccess = examineService.examineModify(examineDTO);
        if(!isSuccess){
            return ApiResponse.fail(EnumExamineError.EXAMINE_UPDATE_FAIL);
        }
        return ApiResponse.success();
    }

    /**
     * 删除审批模板
     * @param examineDTO
     * @return
     */
    @PostMapping("examine_delete")
    @Log(type=LogOperationTypeEnum.DELETE,model=LogModelEnum.EXAMINE_TEMPLATE)
    public ApiResponse examineDelete(@RequestBody ExamineDTO examineDTO) {
        if(StringUtils.isBlank(examineDTO.getId())){
            return ApiResponse.fail(EnumExamineError.EXAMINE_ID_IS_EMPTY);
        }
        boolean isSuccess = examineService.examineDelete(examineDTO);
        if(!isSuccess){
            return ApiResponse.fail(EnumExamineError.EXAMINE_DELETE_FAIL);
        }
        return ApiResponse.success();
    }

    /**
     * 获取审批模板类型
     * @return
     */
    @GetMapping("examine_type_list")
    public ApiResponse getExamineType() {
        List<ExamineTypeDTO> examineTypeDTOList = new ArrayList<>();
        for (ExamineTypeEnum examineTypeEnum : ExamineTypeEnum.values()) {
            ExamineTypeDTO examineTypeDTO = new ExamineTypeDTO();
            examineTypeDTO.setExamineId(examineTypeEnum.getKey());
            examineTypeDTO.setExamineName(examineTypeEnum.getValue());
            examineTypeDTOList.add(examineTypeDTO);
        }
        if(CollectionUtils.isEmpty(examineTypeDTOList)){
            return ApiResponse.fail(EnumExamineError.EXAMINE_TYPE_IS_EMPTY);
        }
        Collections.sort(examineTypeDTOList, Comparator.comparing(ExamineTypeDTO::getExamineName));
        return ApiResponse.success(examineTypeDTOList);
    }

    /**
     * 根据公司id获取审批模板列表
     * @param companyId
     * @return
     */
    @GetMapping("examine_list/{companyId}")
    public ApiResponse getExamineList(@PathVariable String companyId){
        if(StringUtils.isBlank(companyId)){
            return ApiResponse.fail(EnumExamineError.COMPANY_ID_IS_EMPTY);
        }
        List<ExamineDTO> examineDTOList = examineService.getExamineList(companyId);
        return ApiResponse.success(examineDTOList);
    }

    /**
     * 根据id取审批模板详情
     * @param id
     * @return
     */
    @GetMapping("examine_detail_id/{id}")
    public ApiResponse getExamineDetailById(@PathVariable String id){
        if(StringUtils.isBlank(id)){
            return ApiResponse.fail(EnumExamineError.EXAMINE_ID_IS_EMPTY);
        }
        ExamineDTO examineDTO = examineService.getExamineDetailById(id);
        if(examineDTO == null){
            return ApiResponse.fail(EnumExamineError.EXAMINE_DETAIL_NOT_EXIST);
        }
        return ApiResponse.success(examineDTO);
    }

    /**
     * 根据模板组和公司取审批模板详情
     * @param group
     * @param companyId
     * @return
     */
    @GetMapping("examine_detail_query/{group}/{companyId}")
    public ApiResponse getExamineDetailById(@PathVariable String group,@PathVariable String companyId){

        if(StringUtils.isBlank(group)){
            return ApiResponse.fail(EnumExamineError.EXAMINE_GROUP_IS_EMPTY);
        }
        if(StringUtils.isBlank(companyId)){
            return ApiResponse.fail(EnumExamineError.COMPANY_ID_IS_EMPTY);
        }
        ExamineDTO examineDTO = examineService.getExamineDetail(group,companyId);
        if(examineDTO == null){
            return ApiResponse.fail(EnumExamineError.EXAMINE_DETAIL_NOT_EXIST);
        }
        return ApiResponse.success(examineDTO);
    }

    /**
     * 抓取用户考勤组并缓存到redis中
     * @param companyId
     * @return
     */
    public ApiResponse graspAttendanceGroup(@RequestParam("companyId") String companyId, @RequestParam("taskInstanceId") String taskInstanceId){
        if(StringUtils.isBlank(companyId)){
            return ApiResponse.fail(EnumExamineError.COMPANY_ID_IS_EMPTY);
        }
        try {
            examineService.saveUserAttendanceByCompany(companyId);
        } catch (Exception e) {
            throw e;
        }
        return ApiResponse.success();
    }
}
