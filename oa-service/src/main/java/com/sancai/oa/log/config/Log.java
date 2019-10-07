package com.sancai.oa.log.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**@author fanjing
 * @create 2019/7/24 13：35
 * @descrption 定义了一个方法级别的@Log注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Log {

    /**
     * 操作类型1-新增 2-修改 3-删除
     * @return
     */
    LogOperationTypeEnum type();

    /**
     * 操作模块
     * @return
     */
    LogModelEnum model();

}
