package com.sancai.oa.core.exception;

import com.sancai.oa.core.ApiResponse;
import com.sancai.oa.quartz.util.TaskMessage;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * 全局异常处理
 * @Author chenm
 * @create 2019/7/22 16:27
 */
@ControllerAdvice
@ResponseBody
class OaExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    public ApiResponse exceptionHandler(HttpServletRequest request, Exception e){
        //全局异常处理
        if(e instanceof  OaException){
            TaskMessage.addException(((OaException) e).getError().getMessage());
            return ApiResponse.fail(((OaException) e).getError());
        }
        ApiResponse response = new ApiResponse(EnumSystemError.SYSTEM_ERROR.getCode());
        response.setMessage(e.getMessage());
        response.setData(e.getStackTrace());

        for(StackTraceElement ste: e.getStackTrace()){
            TaskMessage.addException(ste.toString());
        }
        return response;
    }
}