package com.sancai.oa.report.exception;

import com.sancai.oa.core.exception.OaError;

/**
 * Report
 *
 * @author fans
 * @date 2019-07-22 09:39
 * @since v1.0.0
 */


public enum EnumReportError implements OaError {
	/**
 * Error. 2003
 */
    REPORT_NOT_DATA(20031001, "日志没有数据"),
	NO_OPERATION_OK(20031002,"日志操作失败"),
	PARAMETER_IS_NULL(20031003,"分页参数或公司id参数为空，请检查"),
	NO_DATA_EXIST(20031004,"数据不存在"),
	PARAMETER_IS_NULL_COMPANYID(20031005,"入日报数据companyId为空"),
	EXCEPTION_OCCURRED_IN_DAILY_DATA_IMPORT(20031006,"日报数据导入发生异常"),
	FAILED_TO_INSERT_DEPARTMENT_RELATIONAL_TABLE(20031007,"日志数据插入部门关系表失败"),
	LOG_VALID_EXCEPTION(20031008,"日报数据校验是否有效方法发生异常(没有查出公司所有员工数据)"),
	IMPORT_DAILY_TEMPLATE_DATA_EXCEPTION(20031009,"导入日报模板数据发生异常"),
	PARAMETER_IS_NULL_ID_CODE(20031010,"日志模板id和code参数为空，请检查"),
	NO_OPERATION_OK_INSERT(20031011,"新增日志规则操作失败"),
	NO_OPERATION_OK_UPDATE(20031012,"修改日志规则操作失败"),
	NO_OPERATION_OK_DEL(20031013,"删除日志规则操作失败"),
	REPORT_NOT_DATA_RULE(20031014, "日志规则没有数据"),
	PARAMETER_IS_NULL_ID_TIME(20031015,"日志模板id开始结束时间参数为空，请检查"),
	REPORT_NOT_DATA_RULE_ID(20031016, "日志规则不存在"),
	NO_OPERATION_OK_REPORT_TEMPLATE(20031017,"日志模板导入操作失败"),
	NO_DATA_EXIST_AND_STATUS(20031018,"日志不存在或此状态不允许此操作")
	;

	private Integer code;

	private String message;

	EnumReportError(Integer code, String message) {
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
