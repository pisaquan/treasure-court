package com.sancai.oa.quartz.exception;

import com.sancai.oa.core.exception.OaError;

/**
 * API department
 *
 * @Author wangyl
 * @create 2019/7/25 09:14
 * @since v1.0.0
 */


public enum EnumTaskInstanceError implements OaError {
	/**
	 * Error. 2005
	 */
	TASK_INSTANCE_NOT_DATA(20071001, "没有定时任务数据"),
	TASK_INSTANCE_NO_OPERATION_OK(20071002,"操作失败"),
	TASK_INSTANCE_PARAMETER_IS_NULL(20071005,"请求参数为空，请检查")

	    ;

	    private Integer code;

	    private String message;

	EnumTaskInstanceError(Integer code, String message) {
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
