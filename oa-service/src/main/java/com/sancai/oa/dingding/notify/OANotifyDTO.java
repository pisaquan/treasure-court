package com.sancai.oa.dingding.notify;

import lombok.Data;

import java.util.LinkedHashMap;

/**
 * OA消息实体类
 * @Author fans
 */
@Data
public class OANotifyDTO {
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

    private LinkedHashMap<String, String> notifyParam;
    /**
     * 消息标题
     */
    private String title;


    public OANotifyDTO(String companyId, String senderId, String messageUrl, String title) {
        this.companyId = companyId;
        this.senderId = senderId;
        this.messageUrl = messageUrl;
        this.title = title;
    }

    public void addParam(String key, String text){
        if(notifyParam == null){
            notifyParam = new LinkedHashMap();
        }

        notifyParam.put(key,text);
    }

}
