package com.sancai.oasystem.bean;

import com.dingtalk.api.response.OapiProcessinstanceGetResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author pisaquan
 * @since 2019/7/20 15:26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class DdExamineInstanceVO  extends OapiProcessinstanceGetResponse.ProcessInstanceTopVo implements Serializable{

    private static final long serialVersionUID = -7935915799246008978L;

    /**
     * 审批实例id
     */
    private String processInstanceId;

    /**
     * 审批模板标识
     */
    private String processCode;

    /**
     * 钉钉返回的审批实例
     */
    private OapiProcessinstanceGetResponse.ProcessInstanceTopVo processInstanceTopVo;
}
