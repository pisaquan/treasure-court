package com.sancai.oa.core;


import com.github.pagehelper.PageInfo;
import com.sancai.oa.core.exception.OaError;

/**
 * 返回结果参数封装
 *  @author chenm
 *  @create 2019/7/22 13:11
 */
public class ApiResponse <T> {

    private static final int CODE_SUCCESS = 0;


    private int code;
    private String message = "success";
    private int page_count;

    private T data;
    

    public ApiResponse(int code){
        this.code = code;
    }

    private ApiResponse(int code, T data){
        this.code = code;
        this.data = data;
    }
    private ApiResponse(int code, T data,int pageCount){
        this.code = code;
        this.data = data;
        this.page_count = pageCount;
    }
    
    private ApiResponse(OaError error){
        this.code = error.getCode();
        this.message = error.getMessage();
    }

    public static ApiResponse success(){
        return new ApiResponse(CODE_SUCCESS);
    }

    public static ApiResponse success(Object data){
        return new ApiResponse(CODE_SUCCESS, data);
    }

    public static ApiResponse success(PageInfo page){
        return new ApiResponse(CODE_SUCCESS, page.getList(),page.getPages());
    }

    public static ApiResponse fail(OaError error){
        return new ApiResponse(error);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String msg) {
        this.message = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getPage_count() {
        return page_count;
    }

    public void setPage_count(int pageCount) {
        this.page_count = pageCount;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}

