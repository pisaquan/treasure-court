package com.sancai.oa.quartz.util;

import com.sancai.oa.quartz.entity.BaseJob;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author wangyl
 * @date 2019/7/30 13:08
 */
public class StringUtil extends BaseJob {

    public void chk() {
        System.out.println("当前时间 :" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        System.out.println("Stringutil.chk is running !!! ");
    }
    public void search() {
        System.out.println("当前时间 :" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        System.out.println("Stringutil.searching  is running !!! ");
    }
}
