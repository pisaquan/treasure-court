package com.sancai.oa.report.exception;


import com.sancai.oa.core.exception.OaError;
import com.sancai.oa.core.exception.OaException;

/**
 * @author chenm
 */
public class OaReportlException extends OaException {

    public OaReportlException(OaError error){
        super(error);
 
    }

    
}
