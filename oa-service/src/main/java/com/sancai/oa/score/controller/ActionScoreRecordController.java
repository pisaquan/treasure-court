package com.sancai.oa.score.controller;


import com.github.pagehelper.PageInfo;
import com.sancai.oa.core.ApiResponse;
import com.sancai.oa.core.version.ApiVersion;
import com.sancai.oa.log.config.Log;
import com.sancai.oa.log.config.LogModelEnum;
import com.sancai.oa.log.config.LogOperationTypeEnum;
import com.sancai.oa.score.entity.ActionScoreRecord;
import com.sancai.oa.score.entity.ActionUserScoreDTO;
import com.sancai.oa.score.entity.ScoreRecordListDTO;
import com.sancai.oa.score.entity.ScoreRecordRequestDTO;
import com.sancai.oa.score.exception.EnumScoreError;
import com.sancai.oa.score.exception.OaScoreException;
import com.sancai.oa.score.service.IActionScoreRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 行为积分变动记录 前端控制器
 * </p>
 *
 * @author quanleilei
 * @since 2019-07-31
 */
@ApiVersion(1)
@RestController
@RequestMapping("{version}/score")
public class ActionScoreRecordController {

    @Autowired
    IActionScoreRecordService actionScoreRecordService;

    /**
     * 对一名员工进行积分的变动（增加/删除）
     *
     * @param actionScoreRecord 积分记录实体类封装前端传来的请求体
     * @return 返回码和返回码描述
     */
    @PostMapping("score_record_modify")
    @Log(type = LogOperationTypeEnum.UPDATE, model = LogModelEnum.SCORE)
    public ApiResponse modifyScoreRecord(@RequestBody ActionScoreRecord actionScoreRecord) {
        Integer result = null;
        if (StringUtils.isEmpty(actionScoreRecord.getSource()) || StringUtils.isEmpty(actionScoreRecord.getType())) {
            throw new OaScoreException(EnumScoreError.INSERT_USERSCORE_FAILURE_PARAM_ISNULL);
        }
        //对分数进行判断
        if (actionScoreRecord.getScore() <= 0 || actionScoreRecord.getScore() >= 100) {
            throw new OaScoreException(EnumScoreError.PARAM_SCORE_IS_BELOWSTANDARD);
        }
        try {
            result = actionScoreRecordService.modifyScoreRecord(actionScoreRecord);
        } catch (Exception e) {
            throw new OaScoreException(EnumScoreError.INSERT_RECORD_FAILURE);
        }
        if (result == 0) {
            throw new OaScoreException(EnumScoreError.UPDATE_SCORERECORD_FAILURE);
        }
        return ApiResponse.success();

    }


    /**
     * 积分变动列表
     *
     * @param scoreRecordRequestDTO 封装前端请求体的实体类
     * @return 返回积分变动列表
     */
    @PostMapping("score_record_list")
    public ApiResponse getScoreRecordList(@RequestBody ScoreRecordRequestDTO scoreRecordRequestDTO) {
        List<ScoreRecordListDTO> list = null;
        //判断公司id是否为空
        if (StringUtils.isEmpty(scoreRecordRequestDTO.getCompanyId())) {
            return ApiResponse.fail(EnumScoreError.QUERY_PARAM_COMPANYID_IS_EMPTY);
        }
        try {
            list = actionScoreRecordService.getScoreRecordList(scoreRecordRequestDTO);
        } catch (Exception e) {
            throw new OaScoreException(EnumScoreError.QUERY_SCORElIST_FAILURE);
        }
        return ApiResponse.success(new PageInfo<>(list));
    }

    /**
     * 查询一名员工的目前累计的积分总和（或指定月份的积分总和）
     *
     * @param user_id 员工id
     * @return 返回 user_id,user_name,score（总积分）
     */
    @GetMapping("/score_user_query/{user_id}/{company_id}")
    public ApiResponse queryUserScore(@PathVariable(value = "user_id", required = true) String user_id,
                                      @PathVariable(value = "company_id",required = true) String company_id) {
        ActionUserScoreDTO actionUserScoreDTO = null;
        if (StringUtils.isEmpty(user_id) || StringUtils.isEmpty(company_id)) {
            return ApiResponse.fail(EnumScoreError.QUERY_PARAM_COMPANYID_IS_EMPTY);
        }
        try {
            //查询员工目前累计积分总和，所将开始和结束时间设为null
            actionUserScoreDTO = actionScoreRecordService.queryUserScore(user_id,company_id,null, null);
        } catch (Exception e) {
            throw new OaScoreException(EnumScoreError.QUERY_USERSCORE_FAILURE);
        }
        if (actionUserScoreDTO == null) {
            return ApiResponse.fail(EnumScoreError.QUERY_USERSCORE_IS_EMPTY);
        }
        return ApiResponse.success(actionUserScoreDTO);
    }

    /**
     * 部门下的员工积分列表
     *
     * @param dept_id  部门id
     * @param page     页数
     * @param capacity 容量
     * @return 返回该部门下的所有用户信息（user_id,name,total_score）
     */
    @GetMapping("dept_user_list")
    public ApiResponse getDeptUserList(@RequestParam(value = "dept_id", required = true) String dept_id,
                                       @RequestParam(value = "company_id",required = true) String company_id,
                                       @RequestParam(value = "page", required = true) Integer page,
                                       @RequestParam(value = "capacity", required = true) Integer capacity) {
        List list = null;
        if (StringUtils.isEmpty(dept_id) || StringUtils.isEmpty(company_id)) {
            return ApiResponse.fail(EnumScoreError.QUERY_PARAM_IS_EMPTY);
        }
        try {
            list = actionScoreRecordService.getDeptUserList(dept_id, company_id,page, capacity);
        } catch (Exception e) {
            throw new OaScoreException(EnumScoreError.QUERY_USERSCORE_FAILURE);
        }
        return ApiResponse.success(new PageInfo<>(list));
    }

}
