package com.sancai.oa.department.exception;

import com.sancai.oa.core.exception.OaError;

/**
 * API department
 *
 * @Author wangyl
 * @create 2019/7/25 09:14
 * @since v1.0.0
 */


public enum EnumDepartmentError implements OaError {
	/**
	 * Error. 2000
	 */
	DEPARTMENT_NOT_FOUND(20081001, "部门未找到"),
	DEPARTMENT_NAME_DUPLICATE(20081002,"部门名重复"),
	DEPARTMENT_TREE_NOT_FOUND(20081003,"部门树未找到"),
	DEPARTMENT_LIST_NOT_FOUND(20081004,"部门列表未找到"),

	COMPANY_NOT_DATA(20081005, "没有公司数据"),
	NO_OPERATION_OK(20081006,"操作失败"),
	PARAMETER_IS_NULL(20081007,"参数为空，请检查"),
	WRONG_PARAMETER_FORMAT(20081008,"参数格式有误，请检查"),

	DEPARTMENT_ID_EMPTY(2008109,"部门ID为空"),
	COMPANY_ID_EMPTY(20081010,"公司ID为空"),
	THREAD_DATA_DEAL_ERROR(20081011,"线程数据操作异常"),
	    ;

	    private Integer code;

	    private String message;

	EnumDepartmentError(Integer code, String message) {
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
