package com.sancai.oa.report.controller;

/**
 * @Author chenm
 * @create 2019/7/23 11:09
 */

import com.alibaba.fastjson.JSONObject;
import com.sancai.oa.Application;
import com.sancai.oa.core.test.ControllerTest;
import com.sancai.oa.user.entity.UserDTO;
import com.sancai.oa.user.exception.EnumUserError;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

public class ReportControllerTest extends ControllerTest {

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
}
