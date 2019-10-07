package com.sancai.oa.clockin.exception;

import com.sancai.oa.core.exception.OaError;

/**
 * API department
 *
 * @Author wangyl
 * @create 2019/7/25 09:14
 * @since v1.0.0
 */


public enum EnumAttendanceRecordError implements OaError {
    /**
     * Error. 2006
     */
    ATTENDANCE_RECORD_NOT_DATA(20061001, "没有考勤数据"),
    ATTENDANCE_RECORD_CHK_REEOR(20061002, "操作失败"),

    ATTENDANCE_RECORD_PARAM_EMPTY(20061003, "请求参数为空，请检查"),
    /**
     * fanjing
     */
    ATTENDANCE_RECORD_NO_CORRESPONDING_RECORD(20061005, "缺卡点日期没有对应的数据"),
    UPDATE_NOT_SIGNED_POINT_FAILURE(20061006, "考勤缺卡点订正失败"),
    STATISTIC_ATTENDANCE_ERROR_EXCEPTION(20061007, "统计考勤数据异常"),
    PARAMETER_COMPANY_ID_IS_NULL(20061010,"公司id为空，请检查"),
    TASK_INSTANCE_ID_IS_EMPTY(20061011, "合并任务id为空"),
    CONSOLIDATED_MONTH_IS_EMPTY(20061012,"合并月份为空"),
    CONSOLIDATED_ATTENDANCE_ERROR_EXCEPTION(20061013,"合并考勤数据异常"),
    CONSOLIDATED_INSTANCE_IS_EMPTY(20061014,"合并任务实例为空"),
    STATISTIC_INSTANCE_IS_EMPTY(20061014,"统计任务实例为空"),
    STATISTIC_MONTH_IS_EMPTY(20061015,"统计月份为空"),
    EXPORT_EXCEL_FAILURE(20061016,"导出考勤Excel失败"),
    EXPORT_EXCEL_ZIP_FAILURE(20061017,"导出考勤zip压缩包失败"),
    QUERY_ATTENDANCE_LIST_FAILURE(20061018,"查询考勤Excel数据失败"),
    COPY_EXCEL_FAILURE(20061019,"Excel复制失败");


    private Integer code;

    private String message;

    EnumAttendanceRecordError(Integer code, String message) {
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
