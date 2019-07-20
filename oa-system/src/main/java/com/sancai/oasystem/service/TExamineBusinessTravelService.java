package com.sancai.oasystem.service;

import com.sancai.oasystem.bean.TExamineBusinessTravel;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 出差 服务类
 * </p>
 *
 * @author pisaquan
 * @since 2019-07-18
 */
public interface TExamineBusinessTravelService extends IService<TExamineBusinessTravel> {

    /**
     *  拉取钉钉出差数据
     */
    void pullDingTalkBusinessTravelData();

}
