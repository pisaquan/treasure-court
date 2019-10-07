package com.sancai.oa.company.exception;

import com.sancai.oa.core.exception.OaError;

/**
 * API department
 *
 * @Author wangyl
 * @create 2019/7/25 09:14
 * @since v1.0.0
 */


public enum EnumCompanyError implements OaError {
	/**
	 * Error. 2004
	 */
	COMPANY_NOT_DATA(20041001, "没有公司数据"),
	NO_OPERATION_OK(20041002,"操作失败"),
	PARAMETER_IS_NULL(20041003,"参数为空，请检查"),
	DATA_DOES_NOT_MATCH(20041004,"应用key和应用密钥与应用id不匹配"),
	WRONG_PARAMETER_FORMAT(20041005,"参数格式有误，请检查"),
	COMPANY_NAME_EXIST(20041007,"公司名称已存在"),

	COMPANY_ID_EMPTY(20041006,"公司ID为空"),

	FAIL_TO_GET_CORPID(20041008,"获取企业id失败"),
	QUERY_IS_EMPTY(20041009,"查询结果为空")
	    ;

	    private Integer code;

	    private String message;

	EnumCompanyError(Integer code, String message) {
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
