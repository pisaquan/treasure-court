package com.sancai.oasystem.service;

import com.sancai.oasystem.bean.TExamineHoliday;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 公休假 服务类
 * </p>
 *
 * @author pisaquan
 * @since 2019-07-18
 */
public interface TExamineHolidayService extends IService<TExamineHoliday> {

    /**
     *  拉取钉钉公休数据
     */
    void pullDingTalkHolidayData();

}
