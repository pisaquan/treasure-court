package com.sancai.oasystem.service.impl;

import com.dingtalk.api.response.OapiProcessinstanceGetResponse;
import com.sancai.oasystem.bean.DdExamineInstanceVO;
import com.sancai.oasystem.bean.ExamineBaseVO;
import com.sancai.oasystem.bean.TExamineLeave;
import com.sancai.oasystem.bean.enums.ExamineTypeEnum;
import com.sancai.oasystem.dao.TExamineLeaveMapper;
import com.sancai.oasystem.service.IExamineCommonService;
import com.sancai.oasystem.service.TExamineLeaveService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
        List<TExamineLeave> examineEntityList = new ArrayList<>();
        List<DdExamineInstanceVO> ddExamineInstanceVOList = iExamineCommonService.pullDingTalkExamineData(ExamineTypeEnum.LEAVE.getValue());
        ddExamineInstanceVOList.stream().forEach(ddInstanceVo -> {
            OapiProcessinstanceGetResponse.ProcessInstanceTopVo instanceVo = ddInstanceVo.getProcessInstanceTopVo();
            TExamineLeave examineLeaveVO = new TExamineLeave();
            examineLeaveVO.setId(UUIDS.getID());
            examineLeaveVO.setUserId(instanceVo.getOriginatorUserid());
            examineLeaveVO.setProcessCode(ddInstanceVo.getProcessCode());
            examineLeaveVO.setProcessInstanceId(ddInstanceVo.getProcessInstanceId());
            examineLeaveVO.setProcessTitle(instanceVo.getTitle());
            examineLeaveVO.setProcessCreateTime(instanceVo.getCreateTime().getTime());
            examineLeaveVO.setProcessFinishTime(instanceVo.getFinishTime().getTime());
            examineLeaveVO.setProcessStatus(instanceVo.getStatus());
            examineLeaveVO.setProcessResult(instanceVo.getResult());

            leaveFormValue(examineLeaveVO,instanceVo.getFormComponentValues());

            examineLeaveVO.setCreateTime(Timestamp.valueOf(LocalDateTime.now()).getTime());
            examineLeaveVO.setModifyTime(Instant.now().toEpochMilli());
            examineLeaveVO.setDeleted(0);
            examineEntityList.add(examineLeaveVO);
        });
        return examineEntityList;
    }

    /**
     * 处理钉钉返回的表单数据 到 自定义
     * @param examineLeaveVO
     * @param formComponentValueVoList
     */
    public void leaveFormValue(TExamineLeave examineLeaveVO,List<OapiProcessinstanceGetResponse.FormComponentValueVo> formComponentValueVoList){
        formComponentValueVoList.stream().forEach(formComponentValueVo -> {

        });
        examineLeaveVO.setFormValueCompany("三彩科技");
        examineLeaveVO.setFormValueSalary("带薪");
        examineLeaveVO.setFormValueType("事假");
        examineLeaveVO.setFormValueStart(1496678400000L);
        examineLeaveVO.setFormValueFinish(1496678400000L);
        examineLeaveVO.setFormValueDays(1);
        examineLeaveVO.setFormValueReason("体检");
    }

}
