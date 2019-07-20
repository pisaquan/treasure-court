package com.sancai.oasystem.service.impl;

import com.sancai.oasystem.bean.TExamineHoliday;
import com.sancai.oasystem.dao.TExamineHolidayMapper;
import com.sancai.oasystem.service.TExamineHolidayService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;


/**
 * <p>
 * 公休假 服务实现类
 * </p>
 *
 * @author pisaquan
 * @since 2019-07-18
 */
@Service
public class TExamineHolidayServiceImpl extends ServiceImpl<TExamineHolidayMapper, TExamineHoliday> implements TExamineHolidayService {

    @Override
    public void pullDingTalkHolidayData() {

    }
}
