package com.sancai.oa.typestatus.service.impl;

import com.sancai.oa.typestatus.service.ITypeStatusEnumsService;
import org.springframework.stereotype.Service;

/**
 * 类型状态接口
 * @author fanjing
 * @date 2019/8/1
 * @description 实现类
 */
@Service
public class TypeStatusEnumsServiceImpl implements ITypeStatusEnumsService {


    @Override
    public Enum[] getTypeStatusEnumsList(String key) throws ClassNotFoundException {
        Class<?> aClass = Class.forName("com.sancai.oa.typestatus.enums" + "." + key);
        Enum[] enumConstants = null;
        if (aClass.isEnum()) {
            //反射获取枚举类
            Class<Enum> clazz = (Class<Enum>) Class.forName(aClass.getName());
            //获取所有枚举实例
            enumConstants = clazz.getEnumConstants();
        }
        return enumConstants;
    }


}
