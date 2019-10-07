package com.sancai.oa.examine.controller;



import com.github.pagehelper.PageInfo;
import com.sancai.oa.core.ApiResponse;
import com.sancai.oa.core.exception.OaException;
import com.sancai.oa.core.version.ApiVersion;
import com.sancai.oa.examine.entity.*;
import com.sancai.oa.examine.exception.EnumExamineError;
import com.sancai.oa.examine.exception.ExamineException;
import com.sancai.oa.examine.service.IExamineHolidayService;
import com.taobao.api.ApiException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * <p>
 * 公休假 前端控制器
 * </p>
 *
 * @author quanleilei
 * @since 2019-07-24
 */
@ApiVersion(1)
@RestController
@RequestMapping("{version}/examine_holiday")
public class ExamineHolidayController {

    @Autowired
    private IExamineHolidayService examineHolidayService;

    /**
     * 抓取休假审批数据
     *
     * @param companyId
     * @param taskInstanceId
     * @return
     * @throws ApiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class, OaException.class})
    public ApiResponse pullDingTalkExamineData(@RequestParam("companyId") String companyId, @RequestParam("taskInstanceId") String taskInstanceId) {
        if (StringUtils.isBlank(companyId)) {
            throw new ExamineException(EnumExamineError.COMPANY_ID_IS_EMPTY);
        }
        if (StringUtils.isBlank(taskInstanceId)) {
            throw new ExamineException(EnumExamineError.TASK_INSTANCE_ID_IS_EMPTY);
        }
        examineHolidayService.pullHolidayExamineData(companyId,taskInstanceId);
        return ApiResponse.success();
    }

    /**
     * 更新休假审批数据
     * @param taskInstanceId
     * @return
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class, OaException.class})
    public ApiResponse updateDingTalkExamineData(@RequestParam("companyId") String companyId, @RequestParam("taskInstanceId") String taskInstanceId) {
        examineHolidayService.updateExamineData(taskInstanceId);
        return ApiResponse.success();
    }

    /**
     * 公休列表查询，结果分页，按请假开始时间倒序
     *
     * @param requestEntity 前端请求体封装的实体类
     * @return 返回分页结果列表
     */
    @PostMapping("examine_holiday_list")
    public ApiResponse getExamineHolidayList(@RequestBody RequestEntity requestEntity) {
        List<ExamineHolidayDTO> list = null;
        if (StringUtils.isEmpty(requestEntity.getCompanyId())){
            return ApiResponse.fail(EnumExamineError.QUERY_PARAM_COMPANYID_IS_EMPTY);
        }
        try {
            list = examineHolidayService.getExamineHolidayList(requestEntity);

        } catch (Exception e) {
            throw new ExamineException(EnumExamineError.QUERY_HOLIDAY_LIST_FAILURE);
        }
        return ApiResponse.success(new PageInfo<>(list));

    }

    /**
     * 公休详情查询
     *
     * @param id 前端请求的记录id
     * @return 返回该记录详情
     */
    @GetMapping("/examine_holiday_detail/{id}")
    public ApiResponse getHolidayDetail(@PathVariable String id) {
        ExamineHolidayDetailDTO holidayDetail = null;
        if (StringUtils.isEmpty(id)){
            return ApiResponse.fail(EnumExamineError.QUERY_HOLIDAY_ID_IS_EMPTY);
        }
        try {
            holidayDetail = examineHolidayService.getExamineHolidayDetail(id);
        } catch (Exception e) {
            throw new ExamineException(EnumExamineError.QUERY_HOLIDAY_DETAIL_FAILURE);
        }
        if (holidayDetail == null) {
            return ApiResponse.fail(EnumExamineError.QUERY_HOLIDAY_DETAIL_IS_EMPTY);
        }
        return ApiResponse.success(holidayDetail);

    }
}
