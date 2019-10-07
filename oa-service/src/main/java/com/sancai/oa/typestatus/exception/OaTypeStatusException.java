package com.sancai.oa.typestatus.exception;

import com.sancai.oa.core.exception.OaError;
import com.sancai.oa.core.exception.OaException;

public class OaTypeStatusException extends OaException {
    public OaTypeStatusException(OaError error) {
        super(error);
    }
}
