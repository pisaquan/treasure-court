package com.sancai.oa.core.threadpool;

import lombok.Data;

/**
 * 是否回滚
 * @Author chenm
 * @create 2019/8/22 14:43
 */
@Data
public class RollBack {
    public RollBack(boolean needRoolBack) {
        this.needRoolBack = needRoolBack;
    }

    private boolean needRoolBack;


}
