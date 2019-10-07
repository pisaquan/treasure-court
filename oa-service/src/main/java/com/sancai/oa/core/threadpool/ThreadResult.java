package com.sancai.oa.core.threadpool;

import lombok.Data;

import java.util.List;

@Data
public class ThreadResult<T> {
    /**
     * 标志位 true 执行成功 false 执行失败
     */
    private Boolean flag;
    /**
     * 返回数据
     */
    private List<T> data;

    /**
     * 返回异常信息 llag 为false 时使用
     */
    private Exception e;
}
