package com.sancai.oa.log.config;


import com.alibaba.fastjson.JSONObject;
import com.sancai.oa.log.entity.OperationLog;
import com.sancai.oa.log.exception.EnumLogError;
import com.sancai.oa.log.exception.OaLogException;
import com.sancai.oa.log.service.impl.SysLogServiceImpl;
import com.sancai.oa.utils.UUIDS;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


/**
 * @author fanjing
 * @date 2019/7/24 15:31
 * 日志切面配置
 */

@Aspect
@Component
public class LogAspect {
    private final static Logger log = org.slf4j.LoggerFactory.getLogger(LogAspect.class);

    @Autowired
    private SysLogServiceImpl sysLogService;

    /**
     * 表示匹配带有自定义注解的方法
     */
    @Pointcut("@annotation(com.sancai.oa.log.config.Log)")
    public void pointcut() {
    }

    @Around("pointcut()")
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public Object around(ProceedingJoinPoint point) throws Throwable {
        Object result = null;
        result = point.proceed();
        try {
            insertLog(point);
        } catch (Throwable e) {
            throw new OaLogException(EnumLogError.LOGGING_FAILURE);
        }
        return result;
    }

    private void insertLog(ProceedingJoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        OperationLog syslog = new OperationLog();

        Log userAction = method.getAnnotation(Log.class);
        if (userAction != null) {
            // 注解上的描述 操作的模块名称+操作类型+详细说明
            syslog.setType(userAction.model().getKey() + "-" + userAction.type().getKey());
        }
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        // 请求的类名
        String className = point.getTarget().getClass().getName();
        // 请求的方法名
        String methodName = signature.getName();
        // 请求的方法参数值
        //将请求的方法名和方法参数存入Map中转化为json数据，然后存入操作详情字段
        Map<String, Object> map = new HashMap<>();
        map.put(methodName, point.getArgs());
        //设置变更内容
        syslog.setContent(JSONObject.toJSONString(map));
        //设置id
        syslog.setId(UUIDS.getID());
        //设置创建时间
        syslog.setCreateTime(System.currentTimeMillis());
        //是否删除：默认未删
        syslog.setDeleted(0);
        //从请求头中获取操作人的userId
        //获取PC端操作人的admin_id
        String operateUserId = request.getHeader("admin_id");
        if (operateUserId == null) {
            //获取移动端操作人的user_id
            operateUserId = request.getHeader("user_id");
            syslog.setOperatorUserId(operateUserId);
            log.info("当前登陆人：{},类名:{},方法名:{},参数：{}", operateUserId, className, methodName, point.getArgs());
            sysLogService.insertUserLog(syslog);
            return;
        }
        syslog.setOperatorUserId(operateUserId);
        log.info("当前登陆人：{},类名:{},方法名:{},参数：{}", operateUserId, className, methodName, point.getArgs());
        sysLogService.insertAdminLog(syslog);

    }

}
