package com.sancai.oasystem.service.impl;

import com.sancai.oasystem.bean.ExamineBaseVO;
import com.sancai.oasystem.bean.TExamineLeave;
import com.sancai.oasystem.bean.enums.ExamineTypeEnum;
import com.sancai.oasystem.dao.TExamineLeaveMapper;
import com.sancai.oasystem.service.IExamineCommonService;
import com.sancai.oasystem.service.TExamineLeaveService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * <p>
 * 请假 服务实现类
 * </p>
 *
 * @author pisaquan
 * @since 2019-07-18
 */
@Service
public class TExamineLeaveServiceImpl extends ServiceImpl<TExamineLeaveMapper, TExamineLeave> implements TExamineLeaveService {

    @Autowired
    private IExamineCommonService iExamineCommonService;

    @Override
    public List<TExamineLeave> pullDingTalkLeaveData() {
        List<TExamineLeave>  leaveList = iExamineCommonService.pullDingTalkExamineData(ExamineTypeEnum.LEAVE.getValue());
        return leaveList;
    }
}
