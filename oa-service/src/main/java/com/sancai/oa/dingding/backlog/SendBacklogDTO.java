package com.sancai.oa.dingding.backlog;

import com.dingtalk.api.request.OapiWorkrecordAddRequest;
import lombok.Data;

import java.util.List;

/**
 * 发送待办实体类
 * @Author fans
 */
@Data
public class SendBacklogDTO {
    /**
     *  公司ID
     */
    private String companyId;
    /**
     *  待办事项对应的用户id
     */
    private String managerId;
    /**
     *  待办事项的跳转链接
     */
    private String url;
    /**
     * 待办事项的标题，最多50个字符
     */
    private String title;

    /**
     * 待办事项表单集合
     */
    private List<OapiWorkrecordAddRequest.FormItemVo> formItemList;



}
