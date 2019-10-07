package com.sancai.oa.log.exception;

import com.sancai.oa.core.exception.OaError;
import com.sancai.oa.core.exception.OaException;


/**
 * @author fanjing
 * @date  2019/7/25
 */
public class OaLogException extends OaException {
    public OaLogException(OaError error) {
        super(error);
    }
}
