package com.sancai.oa.dingding;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiGettokenRequest;
import com.dingtalk.api.response.OapiGettokenResponse;
import com.sancai.oa.company.entity.Company;
import com.sancai.oa.company.service.ICompanyService;
import com.sancai.oa.core.cache.LocalCache;
import com.sancai.oa.core.exception.EnumSystemError;
import com.sancai.oa.core.exception.OaException;
import com.taobao.api.ApiException;
import com.taobao.api.BaseTaobaoRequest;
import com.taobao.api.TaobaoResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 钉钉
 *
 * @Author chenm
 * @create 2019/7/25 13:41
 */
@Slf4j
@Service
public class DingDingBase {


    /**
     * 钉钉取token的接口地址
     */
    @Value("${dingding.token-url}")
    private String getTokenUrl;

    @Autowired
    private LocalCache localCache;

    @Autowired
    private ICompanyService companyService;

    /**
     * 请求钉钉接口
     *
     * @param api
     * @param companyId
     * @param request
     * @return
     * @throws ApiException
     */
    public TaobaoResponse request(String api, String companyId, BaseTaobaoRequest request) {
        try {
            DingTalkClient client = new DefaultDingTalkClient(api);

            String token = getAccessToken(companyId);
            TaobaoResponse response = client.execute(request,token);
            String successCode = "0";
            if (!response.getErrorCode().equals(successCode)) {
                /**
                 * （1）每个企业的每个appkey调用单个接口的频率不可超过40次/秒，否则返回错误码90018。
                 * （2）每个企业的每个appkey调用单个接口的频率不可超过1500次/分，否则返回错误码90006。
                 * （3）每个企业调用单个接口的频率不可超过1500次/分，否则返回错误码90005
                 */
                if("90018".equals(response.getErrorCode())){
                    System.out.println("--单个接口的频率不可超过40次/秒,休息一下");
                    Thread.sleep(1000L);
                    return request(api, companyId, request);
                }
                if("90005".equals(response.getErrorCode()) || "90006".equals(response.getErrorCode())){
                    System.out.println("--单个接口的频率不可超过1500次/分,休息一下");
                    Thread.sleep(20000L);
                    return request(api, companyId, request);
                }
                throw new RuntimeException(response.getErrorCode() + ":" + response.getMsg());
            }
            return response;
        } catch (ApiException e) {
            log.error(e.getErrCode() + ":" + e.getErrMsg());
            throw new OaException(EnumSystemError.DINGDING_ERROR);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            throw new OaException(EnumSystemError.DINGDING_ERROR);
        }

    }

    ///**
    // * 请求钉钉接口
    // * @param api
    // * @param companyId
    // * @param request
    // * @return
    // * @throws ApiException
    // */
    //public TaobaoResponse request(String api,String companyId,String token,BaseTaobaoRequest request) throws ApiException {
    //    DingTalkClient client = new DefaultDingTalkClient(api);
    //    TaobaoResponse response = client.execute(request,token);
    //    if("40014".equals(response.getErrorCode())){
    //        token = getAccessToken(companyId);
    //        response = client.execute(request,token);
    //    }
    //    return response;
    //}

    /**
     * 获取access_token
     *
     * @param companyId
     * @return
     * @throws ApiException
     */
    public synchronized String getAccessToken(String companyId) {

        try {
            String key = "dingding_token_" + companyId;
            String token = (String) localCache.getValue(key);
            if (StringUtils.isNotBlank(token)) {
                return token;
            }

            Company companyVO = companyService.companyDetail(companyId);
            String dingDingCorpId = companyVO.getAppKey();
            String dingDingCorpSecret = companyVO.getAppSecret();

            DefaultDingTalkClient client = new DefaultDingTalkClient(getTokenUrl);
            OapiGettokenRequest request = new OapiGettokenRequest();
            request.setAppkey(dingDingCorpId);
            request.setAppsecret(dingDingCorpSecret);
            request.setHttpMethod("GET");
            OapiGettokenResponse response = client.execute(request);

            token = response.getAccessToken();

            //钉钉token有效期是7200，rcache设置7100即可
            localCache.putValue(key, token, 7100);
            return token;
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }


    }
}
