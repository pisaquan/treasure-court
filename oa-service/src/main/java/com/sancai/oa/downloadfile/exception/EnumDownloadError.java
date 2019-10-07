package com.sancai.oa.downloadfile.exception;

import com.sancai.oa.core.exception.OaError;

/**
 * Download
 *
 * @author fans
 * @date 2019-08-22
 * @since v1.0.0
 */


public enum EnumDownloadError implements OaError {
	/**
 * Error. 2009
 */
    REPORT_NOT_DATA(20091001, "没有下载列表数据"),
	NO_OPERATION_OK(20091002,"下载操作失败"),
	PARAMETER_IS_NULL(20091003,"分页参数或用户为空，请检查"),
	BEING_DOWNLOADED(20091004,"文件正在下载中，请耐心等待"),
	PARAMETER_IS_NULL_REQ_DOWNLOADED(20091005,"用户、公司信息或下载类型参数为空，请检查"),
	DOWNLOADED_TYPE_NOT_DATA(20091006, "下载文件类型不存在，请检查"),
	DOWNLOADED_DOWNLOAD_EXCEPTION(20091007, "文件压缩到指定输出流中出现异常"),
	DOWNLOADED_FILE_NOT_DATA(20091008, "下载文件未申请"),
	DOWNLOADED_FAILURE(20091009,"文件下失败，请重新申请"),
	DOWNLOADED_METHOD_NOT_DATA(20091010, "下载文件服务路径有误"),
	DOWNLOADED_FILE_NOT_PATH(20091011, "下载文件路径不能为空"),
	PARAMETER_IS_NULL_COMPANY_MONTH(20091012,"下载文件公司id和月份参数为空，请检查"),
	DOWNLOADED_NOT_DATA(20091013, "申请下载数据为空")
	;

	private Integer code;

	private String message;

	EnumDownloadError(Integer code, String message) {
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
