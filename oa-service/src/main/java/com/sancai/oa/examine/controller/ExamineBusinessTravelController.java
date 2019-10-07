package com.sancai.oa.examine.controller;



import com.sancai.oa.core.ApiResponse;
import com.sancai.oa.core.exception.OaException;
import com.sancai.oa.core.version.ApiVersion;
import com.sancai.oa.examine.entity.*;
import com.sancai.oa.examine.exception.EnumExamineError;
import com.sancai.oa.examine.exception.ExamineException;
import com.sancai.oa.examine.service.IExamineBusinessTravelService;
import com.taobao.api.ApiException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.github.pagehelper.PageInfo;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * <p>
 * 出差 前端控制器
 * </p>
 *
 * @author quanleilei
 * @since 2019-07-24
 */
@ApiVersion(1)
@RestController
@RequestMapping("{version}/examine_business_travel")
public class ExamineBusinessTravelController {

    @Autowired
    private IExamineBusinessTravelService examineBusinessTravelService;

    /**
     * 出差列表查询，按审批时间倒序，分页
     *
     * @param requestEntity 前端请求体封装的实体类
     * @return 返回出差列表
     */
    @PostMapping("examine_business_travel_list")
    public ApiResponse getBusinessTravelList(@RequestBody RequestEntity requestEntity) {
        List<ExamineBusinessTravelDTO> list = null;
        if (StringUtils.isEmpty(requestEntity.getCompanyId())){
            return ApiResponse.fail(EnumExamineError.QUERY_PARAM_COMPANYID_IS_EMPTY);
        }
        try {
            list = examineBusinessTravelService.getBusinessTravelList(requestEntity);
        } catch (Exception e) {
            throw new ExamineException(EnumExamineError.QUERY_BUSINESS_TRAVEL_LIST_FAILURE);
        }
        return ApiResponse.success(new PageInfo<>(list));

    }

    /**
     * 出差详情
     *
     * @param id 出差记录Id
     * @return 返回该id对应的出差详情
     */
    @GetMapping("/examine_business_travel_detail/{id}")
    public ApiResponse getBusinessTravelDetails(@PathVariable String id) {
        ExamineBusinessTravelDetailDTO detail = null;
        if (StringUtils.isEmpty(id)){
            return ApiResponse.fail(EnumExamineError.QUERY_BUSINESS_TRAVEL_ID_IS_EMPTY);
        }
        try {
            detail = examineBusinessTravelService.getBusinessTravelDetails(id);
        } catch (Exception e) {
            throw new ExamineException(EnumExamineError.QUERY_BUSINESS_TRAVEL_DETAIL_FAILURE);
        }
        if (detail == null) {
            return ApiResponse.fail(EnumExamineError.QUERY_BUSINESS_TRAVEL_DETAIL_IS_EMPTY);
        }
        return ApiResponse.success(detail);

    }


    /**
     * 抓取出差审批数据
     * @param companyId
     * @param taskInstanceId
     * @return
     * @throws ApiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class, OaException.class})
    public ApiResponse pullDingTalkExamineData(@RequestParam("companyId") String companyId,@RequestParam("taskInstanceId") String taskInstanceId){
        if(StringUtils.isBlank(companyId)){
            throw new ExamineException(EnumExamineError.COMPANY_ID_IS_EMPTY);
        }
        if(StringUtils.isBlank(taskInstanceId)){
            throw new ExamineException(EnumExamineError.TASK_INSTANCE_ID_IS_EMPTY);
        }
        examineBusinessTravelService.pullBusinessTravelExamineData(companyId,taskInstanceId);
        return ApiResponse.success();
    }


    /**
     * 更新出差审批数据
     * @param taskInstanceId
     * @return
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class, OaException.class})
    public ApiResponse updateDingTalkExamineData(@RequestParam("companyId") String companyId, @RequestParam("taskInstanceId") String taskInstanceId) {
        examineBusinessTravelService.updateExamineData(taskInstanceId);
        return ApiResponse.success();
    }
}
