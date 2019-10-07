package com.sancai.oa.core;
import com.sancai.oa.core.redis.Employee;
import com.sancai.oa.core.redis.RedisUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.*;


@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisUtilTest {
    @Test
    public void contextLoads() {
    }

    @Resource
    private RedisUtil redisUtil;

    @Test
    public void testRedis(){

        //String
        redisUtil.set("Stringkey","5");
        System.out.println(redisUtil.get("Stringkey"));
        //  map
        HashMap hm = new HashMap();
        hm.put("name","sam");
        hm.put("sex","f");
        hm.put("age","23");
        redisUtil.hmset("hashmapkey",hm);
        Map<Object,Object> hmres = redisUtil.hmget("hashmapkey");
        for(Object key : hmres.keySet()){
            System.out.println("key:"+key +",value:"+hmres.get(key));
        }
        // list
        Employee e1 = Employee.builder().id(1).lastName("李四").email("zhangsan@xxx.com").gender(1).dId(1).build();
        Employee e2 =Employee.builder().id(2).lastName("张三").email("zhangsan@xxx.com").gender(1).dId(1).build();
        Employee e3 =Employee.builder().id(3).lastName("王五").email("zhangsan@xxx.com").gender(1).dId(1).build();

        List list = new ArrayList();
        list.add(e2);
        list.add(e3);
        redisUtil.lSet("listkey",e1);
        redisUtil.lSet("listkey",list);

        List listres = redisUtil.lGet("listkey",0,-1);
        for (Object e:listres){
            System.out.println(e);
        }
        // set
        redisUtil.sSet("setkey",e1);
        redisUtil.sSet("setkey",e1);
        redisUtil.sSet("setkey",e1);
        redisUtil.sSet("setkey",e2);

        Set<Object> setres= redisUtil.sGet("setkey");
        for (Object o:setres){
            System.out.println(o);
        }
    }
}
