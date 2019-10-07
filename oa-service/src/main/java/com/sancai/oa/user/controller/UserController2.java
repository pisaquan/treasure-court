package com.sancai.oa.user.controller;

import com.github.pagehelper.PageInfo;
import com.sancai.oa.core.ApiResponse;
import com.sancai.oa.core.version.ApiVersion;
import com.sancai.oa.user.entity.User;
import com.sancai.oa.user.entity.UserDTO;
import com.sancai.oa.user.exception.EnumUserError;
import com.sancai.oa.user.exception.OaUserlException;
import com.sancai.oa.user.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * @Author chenm
 * @create 2019/7/22 13:14
 */

@ApiVersion(2)
@RestController
@RequestMapping("{version}/user2")
public class UserController2 {

    @Autowired
    private UserServiceImpl userService;

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


    @PostMapping("create")
    public ApiResponse createUser(@RequestBody UserDTO param) {
        String id = param.getId();
        String name = param.getName();

        return ApiResponse.success();
    }

//    @GetMapping("createUserBatch")
//    public ApiResponse createUserBatch(
//            @RequestParam(value = "id") String id,
//            @RequestParam(value = "name") String name) {
//
//        userService.batchSaveUser(id, name);
//        return ApiResponse.success();
//    }

}