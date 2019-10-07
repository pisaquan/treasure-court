package com.sancai.oa.dingding.backlog;

import com.dingtalk.api.request.OapiWorkrecordAddRequest;
import com.dingtalk.api.request.OapiWorkrecordUpdateRequest;
import com.dingtalk.api.response.OapiWorkrecordAddResponse;
import com.dingtalk.api.response.OapiWorkrecordUpdateResponse;
import com.sancai.oa.dingding.DingDingBase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


/**
 * 钉钉待办接口服务
 * @author fans
 */
@Service
public class DingDingBacklogService extends DingDingBase {

    /**
     * 钉钉待办发起的接口地址
     */
    @Value("${dingding.sendToWorkrecordAdd-url}")
    private String sendToWorkrecordAddurl;
    /**
     * 钉钉待办更新的接口地址
     */
    @Value("${dingding.sendToWorkrecordUpdate-url}")
    private String sendToWorkrecordUpdateUrl;

    /**
     * 发起待办
     * @param sendBacklogDTO 起待办实体类
     * @return String 待办id
     */
    public String sendToBacklog(SendBacklogDTO sendBacklogDTO ) {
        //接口失效暂时不调用
        if(sendBacklogDTO!=null){
            return "";
        }
        OapiWorkrecordAddRequest req = new OapiWorkrecordAddRequest();
        req.setUserid(sendBacklogDTO.getManagerId());
        req.setCreateTime(System.currentTimeMillis());
        req.setTitle(sendBacklogDTO.getTitle());
        req.setUrl(sendBacklogDTO.getUrl());
        req.setFormItemList(sendBacklogDTO.getFormItemList());
        OapiWorkrecordAddResponse  response = (OapiWorkrecordAddResponse)request(sendToWorkrecordAddurl, sendBacklogDTO.getCompanyId(), req);
            if(response.getErrcode() == 0){
                return response.getRecordId();
            }
        return null;
    }

    /**
     * 更新待办

     * @param updateBacklogDTO 更新待办事项实体类
     * @return boolean 成功true 失败false
     */

    public boolean updateToBacklog(UpdateBacklogDTO updateBacklogDTO) {
        OapiWorkrecordUpdateRequest req = new OapiWorkrecordUpdateRequest();
        req.setUserid(updateBacklogDTO.getManagerId());
        req.setRecordId(updateBacklogDTO.getRecordId());
        OapiWorkrecordUpdateResponse  response = (OapiWorkrecordUpdateResponse)request(sendToWorkrecordUpdateUrl, updateBacklogDTO.getCompanyId(), req);
        if(response.getErrcode() == 0){
            return true;
        }
        return false;
    }


}
