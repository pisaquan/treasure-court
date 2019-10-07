package com.sancai.oa.signin.exception;

import com.sancai.oa.core.exception.OaError;

/**
 * Signin
 *
 * @author fans
 * @date 2019-07-22 09:39
 * @since v1.0.0
 */


public enum EnumSigninError implements OaError {
	/**
	 * Error. 2002
	 */
	SIGNIN_NOT_DATA(20021001, "没有更多数据"),
	NO_OPERATION_OK(20021002,"请求处理失败"),
	PARAMETER_IS_NULL(20021003,"参数为空，请检查"),
	PARAMETER_IS_NULL_ID(20021004,"签到记录详情id参数为空，请检查"),
	PARAMETER_IS_NULL_ID_STATE(20021005,"签到记录详情id和状态参数为空，请检查"),
	NO_DATA_EXIST(20021006,"请求数据不存在"),
	PARAMETER_IS_NULL_COMPANYID(20021007,"导入签到数据companyId为空"),
	FAILED_TO_CHECK_IN_INSERT_DATA(20021008,"签到插入数据失败"),
	FAILED_DATA_INSERT_DEPARTMENT_RELATIONAL(20021009,"签到数据插入部门关系表失败"),
	DINGDING_ABNORMAL_INTERFACE(200210010,"根据部门id获取用户签到记录列表钉钉接口异常"),
	SUPERVISOR_INFORMATION_IS_EMPTY(200210011,"钉钉部门对应的主管信息为空"),
	NOTIFICATION_FAILED(200210012,"发link类型的通知失败"),
	MODIFY_SIGNIN_CONFIRM_ID_TASK_TABLE_TO_SHOW_AN_EXCEPTION(200210013,"修改外出申请表signin_confirm_id任务表的id出现异常"),
	THE_NEW_DATA_IS_ABNORMAL(200210014,"外出签到确认任务新增数据出现异常"),
	EXCEPTION__MODIFY__STATUS_OF_CHECK_IN_RECORD(200210015,"批量修改签到记录的状态发生异常"),
	SEND_OUTING_SIGNIN_TASK(200210016,"发送外出签到确认任务发生异常"),
	ILLEGAL_DEPARTMENT_ID(40009,"不合法的部门id")



	;
	    private Integer code;

	    private String message;

	EnumSigninError(Integer code, String message) {
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
