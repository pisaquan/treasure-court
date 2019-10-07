package com.sancai.oa.dingding.clockin.user;

import com.sancai.oa.Application;
import com.sancai.oa.dingding.user.DingDingUserService;
import com.sancai.oa.user.entity.User;
import com.sancai.oa.user.entity.UserDTO;
import com.sancai.oa.user.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * 考勤测试
 * @Author chenm
 * @create 2019/7/23 9:48
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
@Slf4j
public class TestDingDingUserService {


    @Autowired
    private DingDingUserService dingDingUserService;

    @Autowired
    private IUserService userService;

    @Test
    /**
     * 排班
     */
    public void testAlluser(){

        try {
            List<UserDTO> users = userService.listUser("C5BD8B0F1F4C40AEA94E7A4294EEC228",0,0L,System.currentTimeMillis());

            System.out.println(users.size());
        } catch (Exception e) {
           log.error(e.getMessage());
        }

    }

}