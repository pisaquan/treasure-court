package com.sancai.oa.user.service;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiSmartworkHrmEmployeeListdimissionRequest;
import com.dingtalk.api.response.OapiSmartworkHrmEmployeeListdimissionResponse;
import com.sancai.oa.Application;
import com.sancai.oa.department.entity.Department;
import com.sancai.oa.department.entity.DepartmentDTO;
import com.sancai.oa.department.service.impl.DepartmentServiceImpl;
import com.sancai.oa.dingding.DingDingBase;
import com.sancai.oa.user.entity.User;
import com.sancai.oa.user.entity.UserDTO;
import com.sancai.oa.user.mapper.UserMapper;
import com.sancai.oa.user.service.impl.UserServiceImpl;
import com.taobao.api.ApiException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Author chenm
 * @create 2019/7/23 9:48
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class UserServiceTest {

    @Autowired
    private UserServiceImpl userService;
    @Autowired
    private DingDingBase dingDingBase;
    @Autowired
    private DepartmentServiceImpl departmentService;
    @Autowired
    private UserMapper userMapper;
    @Test
    public void listUserTest(){

//        List<User> users = userService.listUser(1,"2");
//
//        Assert.assertEquals(2,users.size());
//        List<Department> departments= departmentService.listDepartment("F4754EF7878141A5A52C833769064D1F");
//        for(Department d : departments){
//            System.out.println(d);
//        }
//        DepartmentDTO departmentDTO= departmentService.getDepartmentByCompayid("F4754EF7878141A5A52C833769064D1F");
//        System.out.println(departmentDTO);

//        List<Long> longList= departmentService.listSubDepartment("F4754EF7878141A5A52C833769064D1F","1");
//        for (Long l:longList){
//            System.out.println(l);
//        }
//        String str = departmentService.getSuperiorsDepartmentName("b88b72688abf42e4acb222e5cec21c8b","123851432");
//        System.out.println(str);
//        try {
//            DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/smartwork/hrm/employee/listdimission");
//            OapiSmartworkHrmEmployeeListdimissionRequest req = new OapiSmartworkHrmEmployeeListdimissionRequest();
//            req.setUseridList("173747413836020589");
//            OapiSmartworkHrmEmployeeListdimissionResponse response = client.execute(req , dingDingBase.getAccessToken("eac20c0302bb4b6ba8d0c9eaf452e4f4"));
//            System.out.println(response.getBody());
//        } catch (ApiException e) {
//            e.printStackTrace();
//        }

//        List<UserDTO> userDTOList= userMapper.listUserByWorkDay("b88b72688abf42e4acb222e5cec21c8b",0,0L,0L);
//        for(UserDTO u : userDTOList){
//            System.out.println(u);
//        }
        List<UserDTO> userDTOList= userMapper.listUserByCompany("b88b72688abf42e4acb222e5cec21c8b",null);
        for(UserDTO u : userDTOList){
            System.out.println(u);
        }
    }


}
