package com.sancai.oa.dingding.notify;

import lombok.Data;

/**
 * 文本消息实体类
 * @Author fans
 */
@Data
public class TextNotifyDTO {
    /**
     *  公司id
     */
    private String companyId;

    /**
     * 发送人id
     */
    private String senderId;

    /**
     *  文本消息内容
     */
    private String content;

    /**
     * 是否发送给企业全部用户
     */
    private Boolean toAllUser;
}
