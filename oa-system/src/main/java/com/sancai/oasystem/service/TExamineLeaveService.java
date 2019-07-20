package com.sancai.oasystem.service;

import com.sancai.oasystem.bean.ExamineBaseVO;
import com.sancai.oasystem.bean.TExamineLeave;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;


/**
 * <p>
 * 请假 服务类
 * </p>
 *
 * @author pisaquan
 * @since 2019-07-18
 */
public interface TExamineLeaveService extends IService<TExamineLeave> {
    /**
     *  拉取钉钉请假数据
     */
    List<TExamineLeave> pullDingTalkLeaveData();
}
