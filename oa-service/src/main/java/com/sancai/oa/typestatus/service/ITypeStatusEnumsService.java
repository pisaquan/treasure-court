package com.sancai.oa.typestatus.service;

/**
 * @author fanjing
 * @date 2019/8/1
 * @description 类型状态枚举接口
 */
public interface ITypeStatusEnumsService {

    Enum[] getTypeStatusEnumsList(String key) throws ClassNotFoundException;
}
