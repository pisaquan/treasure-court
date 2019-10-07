package com.sancai.oa.score.exception;

import com.sancai.oa.core.exception.OaError;
import com.sancai.oa.core.exception.OaException;

public class OaScoreException extends OaException {
    public OaScoreException(OaError error) {
        super(error);
    }
}
