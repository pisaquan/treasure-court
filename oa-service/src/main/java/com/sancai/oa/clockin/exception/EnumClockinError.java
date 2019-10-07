package com.sancai.oa.clockin.exception;

import com.sancai.oa.core.exception.OaError;

/**
 * API department
 *
 * @Author fans
 * @create 2019/7/25 09:14
 * @since v1.0.0
 */


public enum EnumClockinError implements OaError {
	/**
	 * Error. 2005
	 */
	CLOCKIN_NOT_DATA(20051001, "没有打卡数据"),
	NO_OPERATION_OK(20051002,"查询操作失败"),
	PARAMETER_IS_NULL_COMPANYID_PAGE(20051003,"请求参数公司id和分页参数为空，请检查"),
	PARAMETER_COMPANY_ID_IS_NULL(20051004,"请求参数公司id为空，请检查"),
	TASK_INSTANCE_ID_IS_EMPTY(20051005, "抓取任务id为空"),
	TASK_ERROR(20051006, "任务失败");


	private Integer code;

	private String message;

	EnumClockinError(Integer code, String message) {
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
