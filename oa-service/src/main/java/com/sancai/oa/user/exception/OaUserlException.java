package com.sancai.oa.user.exception;


import com.sancai.oa.core.exception.OaError;
import com.sancai.oa.core.exception.OaException;

/**
 * @author chenm
 */
public class OaUserlException extends OaException {

    public OaUserlException(OaError error){
        super(error);
 
    }

    
}
