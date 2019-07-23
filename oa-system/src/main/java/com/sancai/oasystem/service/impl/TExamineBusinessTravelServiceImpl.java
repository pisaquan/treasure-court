package com.sancai.oasystem.service.impl;

import com.sancai.oasystem.bean.TExamineBusinessTravel;
import com.sancai.oasystem.bean.enums.ExamineFormCompEnum;
import com.sancai.oasystem.bean.enums.ExamineTypeEnum;
import com.sancai.oasystem.dao.TExamineBusinessTravelMapper;
import com.sancai.oasystem.service.IExamineDataService;
import com.sancai.oasystem.service.ITExamineBusinessTravelService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;


/**
 * <p>
 * 出差 服务实现类
 * </p>
 *
 * @author pisaquan
 * @since 2019-07-18
 */
@Service
public class TExamineBusinessTravelServiceImpl extends ServiceImpl<TExamineBusinessTravelMapper, TExamineBusinessTravel> implements ITExamineBusinessTravelService,IExamineDataService {


    @Override
    public void dealExamineData(String group) {

    }
}
