package com.sancai.oa.examine.exception;


import com.sancai.oa.core.exception.OaError;
import com.sancai.oa.core.exception.OaException;

/**
 * <p>
 * 审批异常
 * </p>
 *
 * @author quanleilei
 * @since 2019-07-24
 */
public class ExamineException extends OaException {

    public ExamineException(OaError error){
        super(error);
    }

    
}
