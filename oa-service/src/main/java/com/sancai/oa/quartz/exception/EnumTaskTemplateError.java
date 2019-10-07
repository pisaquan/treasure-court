package com.sancai.oa.quartz.exception;

import com.sancai.oa.core.exception.OaError;

/**
 * API department
 *
 * @Author wangyl
 * @create 2019/7/25 09:14
 * @since v1.0.0
 */


public enum EnumTaskTemplateError implements OaError {
	/**
	 * Error. 2005
	 */
	TASK_TASK_TEMPLATE_NOT_DATA(20081001, "没有定时任务数据"),
	TASK_TASK_TEMPLATE_OPERATION_OK(20081002,"操作失败"),
	TASK_TASK_TEMPLATE_PARAMETER_IS_NULL(20081005,"请求参数为空，请检查"),
	TASK_TIMEZONE_IS_REPETITION(20081006,"已存在重复时间区间的定时任务，请重新选择合适的时间区间")

	    ;

	    private Integer code;

	    private String message;

	EnumTaskTemplateError(Integer code, String message) {
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
