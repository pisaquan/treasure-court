package com.sancai.oa.user.exception;

import com.sancai.oa.core.exception.OaError;

/**
 * API user
 *
 * @author chenming
 * @date 2019-07-22 09:39
 * @since v1.0.0
 */


public enum EnumUserError implements OaError {
	/**
	 * Error. 2000
	 */
	USER_NOT_FOUND(20001001, "用户未找到"),
	USER_NAME_DUPLICATE(20001002, "用户名重复"),

	QUERY_USERID_FAILURE(20001003,"userid查询失败"),
	QUERY_USERID_IS_EMPTY(20001004,"userid不存在"),

	INIT_OFFUSER_EXCEL(20001216,"初始化离职用户信息失败"),
	FILE_ISNULL(20001217,"文件为空"),
	COMPANY_ID_IS_NULL(20001218,"参数公司id为空"),
	UNSUPPORTED_FILE_TYPES(20001219,"不支持的文件类型"),
	ID_IS_NULL(20001220,"参数日志id为空");


	private Integer code;

	private String message;

	EnumUserError(Integer code, String message) {
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


