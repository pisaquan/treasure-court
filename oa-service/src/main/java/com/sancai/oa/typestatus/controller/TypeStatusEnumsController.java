package com.sancai.oa.typestatus.controller;

import com.sancai.oa.core.ApiResponse;
import com.sancai.oa.core.version.ApiVersion;
import com.sancai.oa.typestatus.exception.EnumTypeStatusError;
import com.sancai.oa.typestatus.exception.OaTypeStatusException;
import com.sancai.oa.typestatus.service.ITypeStatusEnumsService;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author fanjing
 * @create 2019/8/1
 * @description 类型状态枚举接口
 */

@ApiVersion(1)
@RestController
@RequestMapping("{version}/system")
public class TypeStatusEnumsController {

    @Autowired
    ITypeStatusEnumsService typeStatusEnumsService;


    @GetMapping("/type_status_enums/{key}")
    public ApiResponse getTypeStatusEnumsList(@PathVariable("key") String key) {

        Enum[] typeStatusEnumsArr = new Enum[0];
        try {
            typeStatusEnumsArr = typeStatusEnumsService.getTypeStatusEnumsList(key);
        } catch (ClassNotFoundException e) {
            throw new OaTypeStatusException(EnumTypeStatusError.QUERY_ENUM_FAILURE);
        }
        if (ArrayUtils.isEmpty(typeStatusEnumsArr)) {
            throw new OaTypeStatusException(EnumTypeStatusError.QUERY_IS_EMPTY);
        }
        return ApiResponse.success(typeStatusEnumsArr);

    }
}
