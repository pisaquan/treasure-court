package com.sancai.oa.dingding.notify;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiMessageCorpconversationAsyncsendV2Request;
import com.dingtalk.api.request.OapiMessageCorpconversationRecallRequest;
import com.dingtalk.api.response.*;
import com.sancai.oa.clockin.entity.AttendanceRecord;
import com.sancai.oa.clockin.mapper.AttendanceRecordMapper;
import com.sancai.oa.clockin.service.IAttendanceRecordService;
import com.sancai.oa.company.entity.Company;
import com.sancai.oa.company.mapper.CompanyMapper;
import com.sancai.oa.dingding.DingDingBase;
import com.sancai.oa.dingding.report.DingDingReportService;
import com.sancai.oa.dingding.role.DingDingRoleService;
import com.sancai.oa.examine.entity.ExamineLeave;
import com.sancai.oa.examine.entity.enums.ExamineStatusEnum;
import com.sancai.oa.examine.entity.enums.LeaveTypeEnum;
import com.sancai.oa.examine.service.IExamineLeaveService;
import com.sancai.oa.signinconfirm.entity.SigninConfirm;
import com.sancai.oa.utils.ListUtils;
import com.sancai.oa.utils.LocalDateTimeUtils;
import com.sancai.oa.utils.OaMapUtils;
import com.taobao.api.ApiException;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 钉钉消息通知接口服务
 *
 * @author fans
 */
@Service
@Slf4j
public class DingDingNotifyService extends DingDingBase {

    /**
     * 钉钉发送工作通知接口地址
     */
    @Value("${dingding.sendToConversation-url}")
    private String sendToConversationUrl;
    /**
     * 钉钉发送考勤确认工作通知跳转链接地址
     */
    @Value("${notify.recordConfirm}")
    private String recordConfirmUrl;
    /**
     * 带薪病假员工上传病例证明通知跳转地址
     */
    @Value("${notify.uploadCaseCertificate}")
    private String uploadCaseCertificate;
    /**
     * 测试环境给员工发送考勤确认人员id
     */
    @Value("${notify.recordConfirm-test-deptid}")
    private String recordConfirmTestDeptid;
    /**
     * 测试环境给测试人员发送上传病例证明通知
     */
    @Value("${notify.uploads-certificate-test-userid}")
    private String uploadsCertificateTestUserid;
    /**
     * 定时任务异常通知用户列表
     */
    @Value("${task-exception-notice-userid}")
    private String taskExceptionNoticeUserid;
    /**
     * 工作通知消息撤回
     */
    @Value("${dingding.recallNotify-url}")
    private String recallNotify;
    @Autowired
    CompanyMapper companyMapper;
    @Autowired
    DingDingRoleService dingDingRoleService;
    @Autowired
    DingDingReportService dingDingReportService;
    @Autowired
    AttendanceRecordMapper attendanceRecordMapper;
    @Autowired
    IAttendanceRecordService attendanceRecordService;
    @Autowired
    IExamineLeaveService examineLeaveService;


    /**
     * 钉钉文本消息通知接口
     *
     * @param textNotifyDTO 文本消息实体类
     * @return boolean
     * @throws ApiException 钉钉api异常
     */
    public boolean sendToNotify(TextNotifyDTO textNotifyDTO){
        OapiMessageCorpconversationAsyncsendV2Request request = new OapiMessageCorpconversationAsyncsendV2Request();
        Company companyVO = companyMapper.selectOne(new QueryWrapper<Company>()
                .lambda().eq(Company::getId, textNotifyDTO.getCompanyId()).and(u -> u.eq(Company::getDeleted, 0)));
        request.setUseridList(textNotifyDTO.getSenderId());
        request.setAgentId(Long.valueOf(companyVO.getAgentId()));
        request.setToAllUser(textNotifyDTO.getToAllUser());
        OapiMessageCorpconversationAsyncsendV2Request.Msg msg = new OapiMessageCorpconversationAsyncsendV2Request.Msg();
        // 文本消息
        msg.setMsgtype(MsgTypeEnum.TEXT.getKey());
        msg.setText(new OapiMessageCorpconversationAsyncsendV2Request.Text());
        msg.getText().setContent(textNotifyDTO.getContent());
        request.setMsg(msg);
        OapiMessageCorpconversationAsyncsendV2Response response = (OapiMessageCorpconversationAsyncsendV2Response) request(sendToConversationUrl, textNotifyDTO.getCompanyId(), request);
        if (response.getErrcode() == 0) {
            return true;
        }
        return false;
    }

    /**
     * 发送OA类型消息
     * @param oANotifyDTO
     * @return 消息任务id
     */
    public Long sendOAMessage(OANotifyDTO oANotifyDTO){
        Company companyVO = companyMapper.selectOne(new QueryWrapper<Company>()
                .lambda().eq(Company::getId, oANotifyDTO.getCompanyId()).and(u -> u.eq(Company::getDeleted, 0)));
        if(companyVO == null){
            return null;
        }

        OapiMessageCorpconversationAsyncsendV2Request request = new OapiMessageCorpconversationAsyncsendV2Request();
        request.setUseridList(oANotifyDTO.getSenderId());
        request.setAgentId(Long.valueOf(companyVO.getAgentId()));
        request.setToAllUser(false);

        OapiMessageCorpconversationAsyncsendV2Request.Msg msg = new OapiMessageCorpconversationAsyncsendV2Request.Msg();
        msg.setMsgtype(MsgTypeEnum.OA.getKey());
        msg.setOa(new OapiMessageCorpconversationAsyncsendV2Request.OA());
        msg.getOa().setHead(new OapiMessageCorpconversationAsyncsendV2Request.Head());
        msg.getOa().setBody(new OapiMessageCorpconversationAsyncsendV2Request.Body());

        msg.getOa().getBody().setTitle(oANotifyDTO.getTitle());
        msg.getOa().setMessageUrl(oANotifyDTO.getMessageUrl());
        msg.getOa().setPcMessageUrl(oANotifyDTO.getMessageUrl());
        Iterator<Map.Entry<String, String>> it = oANotifyDTO.getNotifyParam().entrySet().iterator();

        List<OapiMessageCorpconversationAsyncsendV2Request.Form> forms = new ArrayList<>();
        while(it.hasNext()){
            Map.Entry<String, String> i = it.next();
            OapiMessageCorpconversationAsyncsendV2Request.Form form = new OapiMessageCorpconversationAsyncsendV2Request.Form();
            form.setKey(i.getKey()+":");
            form.setValue(i.getValue());
            forms.add(form);
        }
        msg.getOa().getBody().setForm(forms);

        request.setMsg(msg);

        try {
            OapiMessageCorpconversationAsyncsendV2Response response = (OapiMessageCorpconversationAsyncsendV2Response) request(sendToConversationUrl,oANotifyDTO.getCompanyId(), request);
            if (response.getErrcode() != 0) {
                return null;
            }
            return response.getTaskId();
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }


    }


    /**
     * 钉钉消息撤回通知接口
     * @param msgTaskId 消息任务id
     * @param companyId 公司id
     * @return
     */
    public boolean recallNotify(Long agentId ,Long msgTaskId , String companyId){
        OapiMessageCorpconversationRecallRequest request = new OapiMessageCorpconversationRecallRequest();
        request.setAgentId(agentId);
        request.setMsgTaskId(msgTaskId);
        OapiMessageCorpconversationRecallResponse response = (OapiMessageCorpconversationRecallResponse) request(recallNotify, companyId, request);
        if (response.getErrcode() == 0) {
            return true;
        }
        return false;
    }


    /**
     * 定时任务异常重试三次失败后发送通知消息
     * @param textNotifyDTO
     */
    public void taskExceptionNotice(TextNotifyDTO textNotifyDTO){

        textNotifyDTO.setSenderId(taskExceptionNoticeUserid);

        sendToNotify(textNotifyDTO);
    }

    /**
     * 考勤合并已完成发送通知给分公司的人事专员
     * 接收者的用户userid列表
     * @param companyId 公司id
     * @param  time 年-月份（2019-08）
     * @return
     */

    public  boolean sendToHrNotify(String companyId,String time){
        Company companyVO = companyMapper.selectOne(new QueryWrapper<Company>().lambda().eq(Company::getId, companyId).and(u -> u.eq(Company::getDeleted, 0)));
        //根据考勤结果id，获取月份
        TextNotifyDTO textNotifyDTO = new TextNotifyDTO();
        textNotifyDTO.setCompanyId(companyId);
        textNotifyDTO.setContent(companyVO.getName()+time+"月考勤数据统计已经完成，请查看");
        textNotifyDTO.setToAllUser(false);
        //测试环境发送测试人员
            List<OapiRoleSimplelistResponse.OpenEmpSimple> openEmpSimpleList = dingDingRoleService.getUserListByHrSpecialist(companyId);
            if(openEmpSimpleList == null || openEmpSimpleList.size() == 0){
                log.warn("获取公司行政人事下人事专员角色的用户列表为空");
                return false;
            }
            List<List<OapiRoleSimplelistResponse.OpenEmpSimple>> openEmpSimples = ListUtils.fixedGrouping(openEmpSimpleList,50);
            System.out.println(JSONObject.toJSONString(openEmpSimples));
            for (List<OapiRoleSimplelistResponse.OpenEmpSimple> openEmpSimpleList1:openEmpSimples){
                if(openEmpSimpleList1!=null&&openEmpSimpleList1.size()>0){
                    String  userIds = getUserListString(openEmpSimpleList1);
                    textNotifyDTO.setSenderId(userIds);
                   sendToNotify(textNotifyDTO);
                }
            }
        return true;
    }

    /**
     * 人事人员在考勤列表中点击“发送员工确认”按钮后，系统给该分公司下全体人员发送通知（link类型消息，点击进入‘考勤确认页面’）
     * @param companyId 公司id
     * @param time 时间（2019-08）
     * @return
     */
    public  boolean sendToAttendanceConfirmation(String companyId,String time){
        QueryWrapper<AttendanceRecord> attendanceRecordQueryWrapper = new QueryWrapper<>();
        attendanceRecordQueryWrapper.lambda().eq(AttendanceRecord::getMonth,time);
        attendanceRecordQueryWrapper.lambda().eq(AttendanceRecord::getDeleted,0);
        attendanceRecordQueryWrapper.lambda().eq(AttendanceRecord::getUserConfirm,0);
        attendanceRecordQueryWrapper.lambda().eq(AttendanceRecord::getCompanyId,companyId);
        //测试环境给测试人员发送考勤确认id
        if(StringUtils.isNotBlank(recordConfirmTestDeptid)){
            Set<String> userList =  userList(recordConfirmTestDeptid,companyId);
            if(userList == null || userList.size() == 0){
                return true;
            }
            attendanceRecordQueryWrapper.lambda().in(AttendanceRecord::getUserId,userList);
        }
        List<AttendanceRecord>  attendanceList = attendanceRecordMapper.selectList(attendanceRecordQueryWrapper);
        if(attendanceList == null || attendanceList.size() ==0){
            return true;
        }
        List<AttendanceRecord> attendanceRecordList = new ArrayList<>();
        attendanceList.stream().collect(Collectors.groupingBy(d -> d.getUserId())).entrySet().forEach(entry -> {
            attendanceRecordList.add(entry.getValue().stream().max(Comparator.comparing(AttendanceRecord::getCreateTime)).get());
        });
        for (AttendanceRecord attendanceRecord :attendanceRecordList) {

            if(attendanceRecord!=null){
                String id = attendanceRecord.getId();
                String title = "员工"+attendanceRecord.getUserName()+time+"考勤确认";
                String url = MessageFormat.format(recordConfirmUrl,id);
                OANotifyDTO notify = new OANotifyDTO(companyId, attendanceRecord.getUserId(),url , title);
                notify.addParam("姓名",attendanceRecord.getUserName());
                notify.addParam("考勤月份",time);
                Long   whetherSuccess = sendOAMessage(notify);
                //0:未发送,1:已发送未确认，
                if(whetherSuccess == null){
                    attendanceRecord.setUserConfirm(0);
                    attendanceRecordMapper.updateById(attendanceRecord);
                }else{
                    attendanceRecord.setUserConfirm(1);
                    attendanceRecordMapper.updateById(attendanceRecord);
                }
            }
        }
        return true;
    }

    /**
     * 获取配置文件部门用户集合
     * @param recordConfirmTestDeptid
     * @param companyId
     * @return
     */
        private Set<String> userList (String recordConfirmTestDeptid ,String companyId){

            Object[] array= JSONArray.fromObject(recordConfirmTestDeptid).toArray();
            Set<String> userList = new HashSet<>();
            for(int i=0; i<array.length; i++) {
                JSONObject jsonObj = (JSONObject)JSONObject.toJSON(array[i]);
                if(jsonObj.get("companyId").toString().equals(companyId)){
                    List<Integer> deptid = (List) jsonObj.get("depts");
                    for (Integer dept : deptid){
                        List<String> stringList = dingDingReportService.allUserIdByDeptId(String.valueOf(dept),companyId);
                        userList.addAll(stringList);
                    }
                    break;
                }
            }
            return userList;
        }

    /**
     * 人事人员在考勤列表中点击“发送员工确认”按钮后（个人发送）
     * @param id 考勤id
     * @return
     */
    public  boolean sendToAttendanceConfirmationPerson(String id){
        AttendanceRecord  attendanceRecord  = attendanceRecordService.getById(id);
        if(attendanceRecord!=null){
            //测试环境给测试人员发送考勤确认id

            String  userId = attendanceRecord.getUserId();

            String title = "员工"+attendanceRecord.getUserName()+attendanceRecord.getMonth()+"考勤确认";
            String url = MessageFormat.format(recordConfirmUrl,id);
            OANotifyDTO notify = new OANotifyDTO(attendanceRecord.getCompanyId(), userId,url , title);
            notify.addParam("姓名",attendanceRecord.getUserName());
            notify.addParam("考勤月份",attendanceRecord.getMonth());

            Long whetherSuccess = sendOAMessage(notify);

            //0:未发送,1:已发送未确认，
            if(whetherSuccess == null){
                attendanceRecord.setUserConfirm(0);
                attendanceRecordMapper.updateById(attendanceRecord);
            }else{
                attendanceRecord.setUserConfirm(1);
                attendanceRecordMapper.updateById(attendanceRecord);
            }
        }

        return true;
    }
    /**
     * 获取人事专员角色用户字符串集合
     * @param openEmpSimpleList1 人事专员角色用户集合
     * @return 用户idString
     */
    public String getUserListString(List<OapiRoleSimplelistResponse.OpenEmpSimple> openEmpSimpleList1){
        StringBuilder userIds = new StringBuilder();
        for (OapiRoleSimplelistResponse.OpenEmpSimple openEmpSimple : openEmpSimpleList1 ){
            if(userIds.length()==0){
                userIds.append(openEmpSimple.getUserid());
            }else{
                userIds.append(","+openEmpSimple.getUserid());
            }
        }
        return userIds.toString();
    }

    private Long getBeforeFirstMonthDate(Long nowTimes){
        Date nowTime = new Date(nowTimes);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(nowTime);
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd 00:00:00");
        calendar.add(Calendar.MONTH, -1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return LocalDateTimeUtils.convertTimeToLong(format.format(calendar.getTime()));
    }
    /**
     * 病假带薪发送通知给员工（个人发送）
     * @param companyId 公司id
     * @return
     */
    public  void sendToSickLeavePerson(String companyId){
        //查询 ：请假类型是 病假 form_value_type = SICKLEAVE
        //查询 ：并且是 带薪 form_value_salary = PAID
        //查询 ：审批状态 process_status = COMPLETED
        //查询 ：审批结果 process_result = agree
        //查询 ：审批结果 send_notify_status = 0 带薪病假通知是未发送
        QueryWrapper<ExamineLeave> examineLeaveQueryWrapper = new QueryWrapper<>();
        examineLeaveQueryWrapper.lambda().eq(ExamineLeave::getDeleted,0);
        examineLeaveQueryWrapper.lambda().eq(ExamineLeave::getSendNotifyStatus,0);
        examineLeaveQueryWrapper.lambda().eq(ExamineLeave::getFormValueSalary, LeaveTypeEnum.PAID.getKey());
        examineLeaveQueryWrapper.lambda().eq(ExamineLeave::getFormValueType, LeaveTypeEnum.SICKLEAVE.getKey());
        examineLeaveQueryWrapper.lambda().eq(ExamineLeave::getProcessResult, ExamineStatusEnum.AGREE.getKey());
        examineLeaveQueryWrapper.lambda().eq(ExamineLeave::getProcessStatus, ExamineStatusEnum.COMPLETED.getKey());
        examineLeaveQueryWrapper.lambda().eq(ExamineLeave::getCompanyId,companyId);
        List<ExamineLeave> examineLeaveList  = examineLeaveService.list(examineLeaveQueryWrapper);

        if(examineLeaveList == null || examineLeaveList.size() == 0){
            return;
        }

        for (ExamineLeave examineLeave : examineLeaveList){
            //测试环境给病假带薪发送通知给员工
            String  userId = "";
            if(StringUtils.isNotBlank(uploadsCertificateTestUserid)){
                Map mapTypes = JSON.parseObject(uploadsCertificateTestUserid);
                //刘鹏测试
                if(!OaMapUtils.mapIsAnyBlank(mapTypes,"companyId","userId1","userId2")){
                    if(companyId.equals(mapTypes.get("companyId"))){
                        userId = mapTypes.get("userId1").toString();
                    }else {
                        userId = mapTypes.get("userId2").toString();
                    }
                }
            } else {
                userId = examineLeave.getUserId();
            }
            String title = "员工"+examineLeave.getUserName()+"申请带薪病假上传病例证明通知";
            String url = MessageFormat.format(uploadCaseCertificate,examineLeave.getId());
            OANotifyDTO notify = new OANotifyDTO(examineLeave.getCompanyId(), userId,url , title);
            notify.addParam("姓名",examineLeave.getUserName());
            if(StringUtils.isNotBlank(examineLeave.getFormValueStartOriginal()) && StringUtils.isNotBlank(examineLeave.getFormValueFinishOriginal())){
                notify.addParam("请假时间",examineLeave.getFormValueStartOriginal() +" 至 "+ examineLeave.getFormValueFinishOriginal() );
            }else{
                notify.addParam("请假时间",LocalDateTimeUtils.formatDateTime(examineLeave.getFormValueStart()) +"至"+ LocalDateTimeUtils.formatDateTime(examineLeave.getFormValueFinish()) );
            }
            Long whetherSuccess = sendOAMessage(notify);

            //0:未发送,1:已发送未上传
            if(whetherSuccess == null){
                examineLeave.setSendNotifyStatus(0);
                examineLeaveService.updateById(examineLeave);
            }else{
                examineLeave.setSendNotifyStatus(1);
                examineLeaveService.updateById(examineLeave);
            }

        }




    }


}
