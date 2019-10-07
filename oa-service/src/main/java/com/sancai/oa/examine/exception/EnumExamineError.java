package com.sancai.oa.examine.exception;

import com.sancai.oa.core.exception.OaError;

/**
 * <p>
 * 审批错误码
 * </p>
 *
 * @author quanleilei
 * @since 2019-07-24
 */

public enum EnumExamineError implements OaError {
    /**
     * Error. 2001
     */
    EXAMINE_CODE_IS_EMPTY(20011001, "审批模板code为空"),
    EXAMINE_NAME_IS_EMPTY(20011002,"审批模板名称为空"),
    COMPANY_ID_IS_EMPTY(20011003, "公司id为空"),
    EXAMINE_GROUP_IS_EMPTY(20011004, "审批表单group为空"),
    EXAMINE_ID_IS_EMPTY(20011005, "审批模板id为空"),
    EXAMINE_LEAVE_ERROR_EXCEPTION(20011006, "抓取请假审批数据异常"),
    EXAMINE_HOLIDAY_ERROR_EXCEPTION(20011007, "抓取休假审批数据异常"),
    EXAMINE_BUSINESS_ERROR_EXCEPTION(20011008, "抓取出差审批数据异常"),
    EXAMINE_POSITION_ERROR_EXCEPTION(20011009, "抓取岗位考核审批数据异常"),
    EXAMINE_ACTION_ERROR_EXCEPTION(20011010, "抓取行为考核审批数据异常"),
    EXAMINE_OUT_ERROR_EXCEPTION(20011011, "抓取外出申请审批数据异常"),
    TASK_INSTANCE_ID_IS_EMPTY(20011012, "抓取任务id为空"),
    TASK_INSTANCE_IS_EMPTY(20011013, "抓取任务实例为空"),
    EXAMINE_IS_EMPTY(20011014, "审批模板为空"),

    EXAMINE_CREATE_FAIL(20011015, "新增审批模板失败"),
    EXAMINE_UPDATE_FAIL(20011016, "更新审批模板失败"),
    EXAMINE_DELETE_FAIL(20011017, "删除审批模板失败"),
    EXAMINE_TYPE_IS_EMPTY(20011018, "审批模板类型为空"),
    EXAMINE_LIST_IS_EMPTY(20011019, "审批模板列表为空"),
    EXAMINE_DETAIL_NOT_EXIST(20011020, "审批模板不存在"),
    EXAMINE_NAME_GROUP_REPTITION(20011021, "该子公司下已存在相同的审批名称或者审批类型"),

    QUERY_EXAMINE_ACTION_LIST_FAILED(20011022,"查询行为考核列表失败"),

    QUERY_BUSINESS_TRAVEL_LIST_FAILURE(20011023,"查询出差列表失败"),
    QUERY_BUSINESS_TRAVEL_ID_IS_EMPTY(20011024,"出差记录id不能为空"),
    QUERY_BUSINESS_TRAVEL_DETAIL_FAILURE(20011025,"查询出差详情失败"),
    QUERY_BUSINESS_TRAVEL_DETAIL_IS_EMPTY(20011026,"查询出差详情为空"),

    QUERY_HOLIDAY_LIST_FAILURE(20011027,"查询公休列表失败"),
    QUERY_HOLIDAY_ID_IS_EMPTY(20011028,"公休记录id不能为空"),
    QUERY_HOLIDAY_DETAIL_FAILURE(20011029,"查询公休详情记录失败"),
    QUERY_HOLIDAY_DETAIL_IS_EMPTY(20011030,"查询公休详情为空"),

    QUERY_LEAVE_ID_IS_EMPTY(20011031,"请假记录id不能为空"),
    QUERY_LEAVE_LIST_FAILURE(20011032,"查询请假列表失败"),
    QUERY_LEAVE_DETAIL_FAILURE(20011033,"查询请假详情失败"),
    QUERY_LEAVE_DETAIL_IS_EMPTY(20011034,"查询请假详情为空"),

    QUERY_POSITION_LIST_FAILED(20011035,"查询岗位考核列表失败"),
    QUERY_PARAM_COMPANYID_IS_EMPTY(20011036,"请求参数公司id不能为空"),
    CONFIRM_POSITION_LIST_FAILED(20011037,"确认岗位考核用户失败"),
    CONFIRM_ACTION_LIST_FAILED(20011038,"确认行为考核用户失败"),
    USER_IS_EMPTY(20011039,"用户表没有对应的用户"),
    USER_DEPART_IS_EMPTY(20011039,"用户没有对应的部门"),

    UPLOADS_MEDICAL_CERTIFICATE_FAILURE(20011040,"用户上传病例证明失败"),
    UPLOADS_MEDICAL_CERTIFICATE_USER_ERROR(20011041,"请假用户与上传信息用户不匹配"),
    UPLOADS_MEDICAL_CERTIFICATE_FILE_MAX(20011042,"用户上传病例证明文件数量超限"),
    UPLOADS_MEDICAL_CERTIFICATE_FILE_EXIST(20011043,"用户病例证明已存在");



    private Integer code;

    private String message;

    EnumExamineError(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public Integer getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }


}
