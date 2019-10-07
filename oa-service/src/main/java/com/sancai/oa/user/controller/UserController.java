package com.sancai.oa.user.controller;

import com.github.pagehelper.PageInfo;
import com.sancai.oa.core.ApiResponse;
import com.sancai.oa.core.redis.RedisUtil;
import com.sancai.oa.core.version.ApiVersion;
import com.sancai.oa.department.entity.DepartmentDTO;
import com.sancai.oa.department.exception.EnumDepartmentError;
import com.sancai.oa.user.entity.User;
import com.sancai.oa.user.exception.EnumUserError;
import com.sancai.oa.user.exception.OaUserlException;
import com.sancai.oa.user.service.IUserService;
import com.taobao.api.ApiException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @Author chenm
 * @create 2019/7/22 13:14
 */

@ApiVersion(1)
@RestController
@RequestMapping("{version}/user")
public class UserController {

    @Autowired
    private IUserService userService;

    @Autowired
    RedisUtil redisUtil;



    class TestUser{
        private String token;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
    @GetMapping("signin")
    public ApiResponse signin() {
        TestUser user = new TestUser();
        user.setToken("admin-token");
        return ApiResponse.success(user);
    }

    @GetMapping("get_user/{id}")
    public ApiResponse getUser(@PathVariable String id) {
        User user = userService.getById(id);
        if (user == null) {
            return ApiResponse.fail(EnumUserError.USER_NOT_FOUND);
        }

        return ApiResponse.success(user);
    }

    @GetMapping("get_user_info")
    public ApiResponse getUserInfo(@RequestParam("id") String id) {
        User user = userService.getById(id);
        if (user == null) {
            throw new OaUserlException(EnumUserError.USER_NOT_FOUND);
        }
        return ApiResponse.success(user);
    }

    @GetMapping("list_user")
    public ApiResponse listUser(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "capacity", required = false, defaultValue = "5") int capacity,
            @RequestParam(value = "companyId", required = false) String companyId) {

        List<User> result = userService.listUser(page, capacity,companyId);

        //分页对象
        return ApiResponse.success(new PageInfo<>(result));
    }

    /**
     *  根据名称查询用户信息
     * @param map
     * @return
     */
    @PostMapping("userinfo")
    public ApiResponse getUserInfo(@RequestBody Map<String, Object> map) {
        String name= map.get("name")+"";
        String company_id = map.get("company_id")+"";
        int type = Integer.parseInt(map.get("type")+"");

        List<Map> result = new ArrayList<Map>();
        try {
             result = userService.getUserInfo(company_id,name, type);
        } catch (Exception e) {
            throw new OaUserlException(EnumUserError.USER_NOT_FOUND);
        }
        //分页对象
        return ApiResponse.success(result);
    }

    /**
     * 根据钉钉登录授权码取用户id
     *
     * @param type SIGNIN_CONFIRM、ATTENDANCE_RECORD两种
     * @param code 钉钉登录授权码
     * @param id   任务id
     * @return 返回userId
     */
    @GetMapping("/get_user_id/{type}-{id}-{code}")
    public ApiResponse getUserIdByCode(@PathVariable String type, @PathVariable String id, @PathVariable String code) {
        String userid = null;
        try {
            userid = userService.getUserIdByCode(type, code, id);
        } catch (ApiException e) {
            throw new OaUserlException(EnumUserError.QUERY_USERID_FAILURE);
        }
        if (StringUtils.isBlank(userid)) {
            return ApiResponse.fail(EnumUserError.QUERY_USERID_IS_EMPTY);
        }
        return ApiResponse.success(userid);
    }
    /**
     * 更新离职用户状态
     * @param companyId
     * @param taskInstanceId
     * @return
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public ApiResponse updateOffUserinfoStatus(@RequestParam("companyId") String companyId,@RequestParam("taskInstanceId") String taskInstanceId) throws Exception{
        if(StringUtils.isBlank(companyId)){
            return ApiResponse.fail(EnumUserError.QUERY_USERID_FAILURE);
        }
        userService.updateOffUserinfoStatus(companyId,taskInstanceId);
        return ApiResponse.success();
    }
    /**
     *
     *导入离职员工信息
     * @param
     */
    @PostMapping(value = "/init_offuser_excel")
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public ApiResponse importExcel(@RequestParam(value = "file") MultipartFile multipartFile , @RequestParam(value = "companyId") String companyId){
        if(StringUtils.isBlank(companyId)){
            return ApiResponse.fail(EnumUserError.COMPANY_ID_IS_NULL);
        }
        if(multipartFile == null){
            return ApiResponse.fail(EnumUserError.FILE_ISNULL);
        }
        try {
             userService.initUserInfoByExcel(multipartFile, companyId);
             userService.insertDeptId(companyId);
        } catch (Exception e) {
            throw new OaUserlException(EnumUserError.INIT_OFFUSER_EXCEL);
        }
        return  ApiResponse.success();
    }
}