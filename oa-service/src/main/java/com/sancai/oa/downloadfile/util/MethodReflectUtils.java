package com.sancai.oa.downloadfile.util;

import com.sancai.oa.clockin.entity.DownloadQueryConditionDTO;
import com.sancai.oa.quartz.util.SpringContextUtil;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * <p>
 * 方法反射
 * </p>
 *
 * @author fans
 * @since 2019-08-23
 */
public class MethodReflectUtils {

    /**
     * 通过类的路径与方法名和参数列表来反射调用方法
     * @param className
     *              类所在的路径
     * @param methodName
     *              方法的名称
     * @param downloadQueryConditionDTO 参数实体
     */
    public static Object methodReflect(String className, String methodName, DownloadQueryConditionDTO downloadQueryConditionDTO) {
            Object object = SpringContextUtil.getBean(className);
            Method method = ReflectionUtils.findMethod(object.getClass(), methodName,  DownloadQueryConditionDTO.class);
            Object obj =  ReflectionUtils.invokeMethod(method, object,  downloadQueryConditionDTO);
            return obj;
    }

}
