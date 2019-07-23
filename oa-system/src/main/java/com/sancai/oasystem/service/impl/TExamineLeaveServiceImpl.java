package com.sancai.oasystem.service.impl;

import com.dingtalk.api.response.OapiProcessinstanceGetResponse;
import com.sancai.oasystem.bean.DdExamineInstanceVO;
import com.sancai.oasystem.bean.TExamineLeave;
import com.sancai.oasystem.bean.enums.ExamineFormCompEnum;
import com.sancai.oasystem.dao.TExamineLeaveMapper;
import com.sancai.oasystem.service.IExamineDataService;
import com.sancai.oasystem.service.TExamineLeaveService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * <p>
 * 请假 服务实现类
 * </p>
 *
 * @author pisaquan
 * @since 2019-07-18
 */
@Service
public class TExamineLeaveServiceImpl extends ServiceImpl<TExamineLeaveMapper, TExamineLeave> implements TExamineLeaveService,IExamineDataService {


    @Override
    public void dealExamineData(String group) {
        List<TExamineLeave> examineEntityList = new ArrayList<>();
        List<DdExamineInstanceVO> ddExamineInstanceVOList = ExamineCommonServiceImpl.getDingTalkExamineData(group);
        ddExamineInstanceVOList.stream().forEach(ddInstanceVo -> {
            OapiProcessinstanceGetResponse.ProcessInstanceTopVo instanceVo = ddInstanceVo.getProcessInstanceTopVo();
            TExamineLeave examineLeaveVO = new TExamineLeave();
            examineLeaveVO.setId(UUIDS.getID());
            examineLeaveVO.setUserId(instanceVo.getOriginatorUserid());
            examineLeaveVO.setProcessCode(ddInstanceVo.getProcessCode());
            examineLeaveVO.setProcessInstanceId(ddInstanceVo.getProcessInstanceId());
            examineLeaveVO.setProcessTitle(instanceVo.getTitle());
            examineLeaveVO.setProcessCreateTime(instanceVo.getCreateTime().getTime());
            examineLeaveVO.setProcessFinishTime(instanceVo.getFinishTime() != null ? instanceVo.getFinishTime().getTime(): null);
            examineLeaveVO.setProcessStatus(instanceVo.getStatus());
            examineLeaveVO.setProcessResult(instanceVo.getResult());

            leaveFormValue(examineLeaveVO,instanceVo.getFormComponentValues());

            examineLeaveVO.setCreateTime(Timestamp.valueOf(LocalDateTime.now()).getTime());
            examineLeaveVO.setModifyTime(Instant.now().toEpochMilli());
            examineLeaveVO.setDeleted(0);
            examineEntityList.add(examineLeaveVO);
        });
        this.saveBatch(examineEntityList);
    }



    /**
     * 处理钉钉返回的表单数据 到 自定义
     * @param examineLeaveVO
     * @param formComponentValueVoList
     */
    public void leaveFormValue(TExamineLeave examineLeaveVO,List<OapiProcessinstanceGetResponse.FormComponentValueVo> formComponentValueVoList){

        //todo判空
        formComponentValueVoList.stream().forEach(formComponentValueVo -> {
            if(ExamineFormCompEnum.COMPANY.getDesc().equals(formComponentValueVo.getName())){
                examineLeaveVO.setFormValueCompany(formComponentValueVo.getValue());
            }
            if(ExamineFormCompEnum.SALARY.getDesc().equals(formComponentValueVo.getName())){
                examineLeaveVO.setFormValueSalary(formComponentValueVo.getValue());
            }
            if(ExamineFormCompEnum.DDHOLIDAYFIELD.getValue().equals(formComponentValueVo.getComponentType())){
                String extValue = formComponentValueVo.getExtValue();
                String value = formComponentValueVo.getValue();
                String [] valueArray = StringUtils.substringsBetween(value,"\"","\"");
                Map<String, Object> formMap = JsonToMap.jsonToMap(extValue);
                String extension =  formMap.get("extension").toString();
                Map<String, Object> extensionMap = JsonToMap.jsonToMap(extension);
                examineLeaveVO.setFormValueType(extensionMap.get("tag").toString());
                examineLeaveVO.setFormValueDays(Integer.parseInt(formMap.get("durationInDay").toString()));
                String startTime = valueArray[0];
                String endTime = valueArray[1];

                if(startTime.endsWith(ExamineFormCompEnum.MORNING.getDesc())){
                    startTime = startTime.replace(ExamineFormCompEnum.MORNING.getDesc(),"9:00:00");
                }
                if(endTime.endsWith(ExamineFormCompEnum.AFTERNOON.getDesc())){
                    endTime = endTime.replace(ExamineFormCompEnum.AFTERNOON.getDesc(),"18:00:00");
                }

                try {
                    examineLeaveVO.setFormValueStart(Long.valueOf(TimeConversionUtil.stringLongToMillisecond(startTime)));
                    examineLeaveVO.setFormValueFinish(Long.valueOf(TimeConversionUtil.stringLongToMillisecond(endTime)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if(ExamineFormCompEnum.REASON.getDesc().equals(formComponentValueVo.getName())){
                examineLeaveVO.setFormValueReason(formComponentValueVo.getValue());
            }
        });

    }


}
