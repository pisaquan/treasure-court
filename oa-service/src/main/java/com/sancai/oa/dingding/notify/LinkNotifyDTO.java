package com.sancai.oa.dingding.notify;

import lombok.Data;

/**
 * link消息实体类
 * @Author fans
 */
@Data
public class LinkNotifyDTO {
    /**
     *  公司id
     */
    private String companyId;

    /**
     * 发送人id
     */
    private String senderId;
    /**
     *  消息链接
     */
    private String messageUrl;

    /**
     * 图片地址
     */
    private String picUrl;
    /**
     * 消息文本
     */
    private String text;
    /**
     * 消息标题
     */
    private String title;
    /**
     * 是否发送给企业全部用户
     */
    private Boolean toAllUser;

}
