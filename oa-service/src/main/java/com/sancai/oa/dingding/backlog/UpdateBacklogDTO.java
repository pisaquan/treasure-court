package com.sancai.oa.dingding.backlog;

import com.dingtalk.api.request.OapiWorkrecordAddRequest;
import lombok.Data;

import java.util.List;

/**
 * 发送待办实体类
 * @Author fans
 */
@Data
public class UpdateBacklogDTO {
    /**
     *  公司ID
     */
    private String companyId;
    /**
     *  待办事项对应的用户id
     */
    private String managerId;
    /**
     *  待办事项id
     */
    private String recordId;

}
