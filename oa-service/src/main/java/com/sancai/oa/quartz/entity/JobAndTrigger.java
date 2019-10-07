package com.sancai.oa.quartz.entity;

import lombok.Data;

import java.math.BigInteger;

/**
 * @author wangyl
 * @date 2019/7/30 13:08
 */
@Data
public class JobAndTrigger {
        /**
         * job名称
         */
        private String JOB_NAME;
        /**
         *job组
         */
        private String JOB_GROUP;
        /**
         *job类
         */
        private String JOB_CLASS_NAME;
        /**
         *触发器名
         */
        private String TRIGGER_NAME;
        /**
         *触发器组
         */
        private String TRIGGER_GROUP;
        /**
         *
         */
        private BigInteger REPEAT_INTERVAL;
        /**
         *
         */
        private BigInteger TIMES_TRIGGERED;
        /**
         *cron表达式
         */
        private String CRON_EXPRESSION;
        /**
         *
         */
        private String TIME_ZONE_ID;
}
