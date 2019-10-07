package com.sancai.oa.core.redis;

import com.sancai.oa.clockin.exception.OaClockinlException;
import com.sancai.oa.core.ApiResponse;
import com.sancai.oa.core.exception.EnumSystemError;
import com.sancai.oa.core.version.ApiVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @Author wangyl
 * @create 2019/7/24 09:39
 */
@ApiVersion(3)
@RestController
@RequestMapping("{version}/redis")
public class Redistest {

    @Autowired
    private RedisUtil redisUtil;

    private final String redisVisitIdentity = "4C40AEA94E7A4294EEC228070061213424525531156586";

    @GetMapping("string/{identity}/{key}")
    public ApiResponse getString(@PathVariable String identity,@PathVariable String key) {
        chkPermission(identity);
        //String
        String str = redisUtil.get(key)+"";

        return ApiResponse.success(str);
    }
    @GetMapping("list/{identity}/{key}")
    public ApiResponse getList(@PathVariable String identity,@PathVariable String key) {
        chkPermission(identity);
        List listres = redisUtil.lGet(key,0,-1);
        for (Object e:listres){
            System.out.println(e);
        }

        return ApiResponse.success(listres);
    }
    @GetMapping("set/{identity}/{key}")
    public ApiResponse getSet(@PathVariable String identity,@PathVariable String key) {
        chkPermission(identity);

        Set<Object> setres= redisUtil.sGet(key);

        return ApiResponse.success(setres);
    }

    @GetMapping("map/{identity}/{key}")
    public ApiResponse getMap(@PathVariable String identity,@PathVariable String key) {
        chkPermission(identity);
        Map<Object,Object> hmres = redisUtil.hmget(key);
        for(Object keys : hmres.keySet()){
            System.out.println("key:"+keys +",value:"+hmres.get(keys));
        }
        return ApiResponse.success(hmres);
    }
    @GetMapping("get/{identity}/{key}")
    public ApiResponse get(@PathVariable String identity,@PathVariable String key) {
        chkPermission(identity);
        String strRes="" ;
        List listRes = null;
        Set<Object> setRes= null;
        Map<Object,Object> hmRes = null;
        try {
            Object o = redisUtil.get(key);
            if(null!=o){
                strRes = o.toString();
            }

        } catch (Exception e) {
//            e.printStackTrace();
        }

        try {

            hmRes = redisUtil.hmget(key);
        } catch (Exception e) {
//            e.printStackTrace();
        }

        try {
            listRes = redisUtil.lGet(key,0,-1);

        } catch (Exception e) {
//            e.printStackTrace();
        }

        try {

            setRes = redisUtil.sGet(key);
        } catch (Exception e) {
//            e.printStackTrace();
        }

        if(!StringUtils.isEmpty(strRes)){
            return ApiResponse.success(strRes);
        }
        if(null!=listRes&&listRes.size()>0){
            return ApiResponse.success(listRes);
        }
        if(null!=listRes&&listRes.size()>0){
            return ApiResponse.success(listRes);
        }
        if(null!=setRes&&setRes.size()>0){
            return ApiResponse.success(setRes);
        }
        if(null!=hmRes.keySet()&&hmRes.keySet().size()>0){
            return ApiResponse.success(hmRes);
        }
        return ApiResponse.success();
    }


    @GetMapping("del/{identity}/{key}")
    public ApiResponse del(@PathVariable String identity,@PathVariable String  key ) {
        chkPermission(identity);
        redisUtil.del(key);
        return ApiResponse.success();
    }

    @GetMapping("get_keys/{identity}")
    public ApiResponse getAllKeys(@PathVariable String identity) {
        chkPermission(identity);
        Set<String> set = redisUtil.getAllKeys();
        return ApiResponse.success(set);
    }
    @GetMapping("get_key_expire/{identity}/{key}")
    public ApiResponse getKeyExpire(@PathVariable String identity,@PathVariable String  key ) {
        chkPermission(identity);
        long l = redisUtil.getExpire(key);
        return ApiResponse.success(l);
    }
    /**
     * 判断是否有权限访问
     * @param identity
     */
    private void chkPermission(String identity){
        if(!allowed(identity)){
            throw new OaClockinlException(EnumSystemError.NO_ACCESS_PERMISSION);
        }
    }

    /**
     * 判断是否有权限访问
     * @param identity
     * @return
     */
    private boolean allowed( String identity){
        boolean flag = false;
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String compare = redisVisitIdentity+sdf.format(date);
        if(compare.equals(identity)){
            flag = true;
        }
        return flag;
    }

}
