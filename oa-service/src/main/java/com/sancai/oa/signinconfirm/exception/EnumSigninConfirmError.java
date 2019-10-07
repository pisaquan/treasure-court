package com.sancai.oa.signinconfirm.exception;

import com.sancai.oa.core.exception.OaError;

/**
 * Signin user
 *
 * @author wangyl
 * @date 2019-07-22 09:39
 * @since v1.0.0
 */


public enum EnumSigninConfirmError implements OaError {
	/**
	 * Error. 20001
	 */
	SIGNINCONFIRM_NOT_DATA(20001110, "没有更多数据"),
	SIGNINCONFIRM_NO_OPERATION_OK(20001111,"操作失败"),
	SIGNINCONFIRM_PARAMETER_IS_NULL(20001112,"参数为空，请检查"),
	SIGNINCONFIRM_NO_DATA_EXIST(20001113,"请求数据不存在"),
	SIGNINCONFIRM_ALREADY_CONRIRM(20001114,"签到已确认");
	    private Integer code;

	    private String message;

		EnumSigninConfirmError(Integer code, String message) {
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
