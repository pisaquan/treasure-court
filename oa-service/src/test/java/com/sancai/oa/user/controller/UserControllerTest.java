package com.sancai.oa.user.controller;

/**
 * @Author chenm
 * @create 2019/7/23 11:09
 */

import com.alibaba.fastjson.JSONObject;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.request.OapiGettokenRequest;
import com.dingtalk.api.response.OapiGettokenResponse;
import com.sancai.oa.Application;
import com.sancai.oa.core.test.ControllerTest;
import com.sancai.oa.user.entity.UserDTO;
import com.sancai.oa.user.exception.EnumUserError;
import com.taobao.api.ApiException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

public class UserControllerTest extends ControllerTest {

    @Test
    public void testGetUser() throws Exception {

        JSONObject result = get("/v1/user/get_user/aa");
        System.out.println(result.toJSONString());

        Assert.assertTrue(result.getIntValue("code") == 0);
        Assert.assertEquals("aa",result.getJSONObject("data").getString("id"));


        result = get("/v1/user/get_user/aaxx");

        Assert.assertTrue(result.getIntValue("code") == EnumUserError.USER_NOT_FOUND.getCode());
    }

    @Test
    public void testSaveUser() throws Exception {

        UserDTO u = new UserDTO();
        u.setId("xxx12");
        u.setName("yyy12");

        JSONObject result = post("/v1/user/create/",u);
        System.out.println(result.toJSONString());

        Assert.assertTrue(result.getIntValue("code") == 0);

    }

    @Test
    public void testtocak(){
        DefaultDingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/gettoken");
        OapiGettokenRequest request = new OapiGettokenRequest();
        request.setAppkey("dingqnltvrgfj1onwbvr");
        request.setAppsecret("mRbS00ycGtrd7GBpnQEHo5i5rh5N0nyaZdouI-k5iAr9bEWcb8PGC4W0e2m5bJES");
        request.setHttpMethod("GET");
        OapiGettokenResponse response = null;
        try {
            response = client.execute(request);
            System.out.println(response.getAccessToken());
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }
}
