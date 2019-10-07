package com.sancai.oa.signinconfirm.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.sancai.oa.clockin.entity.AttendanceRecord;
import com.sancai.oa.clockin.entity.ClockinRecordMerge;
import com.sancai.oa.clockin.entity.ClockinRecordMergeDTO;
import com.sancai.oa.clockin.enums.EnumClockinMergeStatus;
import com.sancai.oa.clockin.enums.EnumClockinPointStatus;
import com.sancai.oa.clockin.mapper.ClockinRecordMergeMapper;
import com.sancai.oa.clockin.service.ClockinService;
import com.sancai.oa.clockin.service.IAttendanceRecordService;
import com.sancai.oa.clockin.service.IClockinRecordMergeService;
import com.sancai.oa.department.entity.Department;
import com.sancai.oa.department.service.IDepartmentService;
import com.sancai.oa.examine.entity.ExamineOutApply;
import com.sancai.oa.examine.mapper.ExamineOutApplyMapper;
import com.sancai.oa.examine.utils.MapUtils;
import com.sancai.oa.signin.entity.SigninRecord;
import com.sancai.oa.signin.service.ISigninRecordService;
import com.sancai.oa.signinconfirm.entity.SigninConfirm;
import com.sancai.oa.signinconfirm.exception.EnumSigninConfirmError;
import com.sancai.oa.signinconfirm.exception.OaSigninConfirmlException;
import com.sancai.oa.signinconfirm.mapper.SigninConfirmMapper;
import com.sancai.oa.signinconfirm.service.ISigninConfirmService;
import com.sancai.oa.typestatus.enums.SigninConfirmStatusEnum;
import com.sancai.oa.typestatus.enums.SigninRecordStatusEnum;
import com.sancai.oa.user.entity.User;
import com.sancai.oa.user.entity.UserDTO;
import com.sancai.oa.user.service.IUserService;
import com.sancai.oa.utils.DateCalUtil;
import com.sancai.oa.utils.LocalDateTimeUtils;
import com.sancai.oa.utils.OaMapUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;


/**
 * <p>
 * 签到确认记录 服务实现类
 * </p>
 *
 * @author wangyl
 * @since 2019-08-06
 */
@Service
@Slf4j
public class SigninConfirmServiceImpl extends ServiceImpl<SigninConfirmMapper, SigninConfirm> implements ISigninConfirmService {
    @Autowired
    private IDepartmentService departmentService;
    @Autowired
    private SigninConfirmMapper tsigninconfirmmapper;
    @Autowired
    private ISigninRecordService siginRecordService;
    @Autowired
    private ClockinService clockinService;
    @Autowired
    private IUserService userService;
    @Autowired
    private ClockinRecordMergeMapper clockinRecordMergeMapper;
    @Autowired
    private IClockinRecordMergeService clockinRecordMergeService;
    @Autowired
    private IAttendanceRecordService attendanceRecordService;
    @Autowired
    private ExamineOutApplyMapper examineOutApplyMapper;


    @Override
    public List<Map> signinConfirmListByCompany(Map<String, Object> map) throws Exception {
        if (OaMapUtils.mapIsAnyBlank(map, "page", "capacity", "company_id")) {
            return new ArrayList<Map>();
        }

        //PLSQL对于字段类型限制是很严格,数据类型转换后传入:
        if (!OaMapUtils.mapIsAnyBlank(map, "dept_id")) {
            List<Long> longList = departmentService.listSubDepartment(map.get("company_id").toString(),map.get("dept_id").toString());
            map.put("deptList", longList);
        }

        if (!OaMapUtils.mapIsAnyBlank(map, "start_time")) {
            map.put("start_time", Long.valueOf(map.get("start_time").toString()));
        }

        if (!OaMapUtils.mapIsAnyBlank(map, "end_time")) {
            map.put("end_time", Long.valueOf(map.get("end_time").toString()));
        }
        int pages = Integer.valueOf(map.get("page").toString());
        int capacity = Integer.valueOf(map.get("capacity").toString());
        //每页的大小为capacity，查询第page页的结果
        PageHelper.startPage(pages, capacity);

        List<Map> signinConfirmRecord  = new ArrayList<Map>();

        signinConfirmRecord = tsigninconfirmmapper.signinConfirmListByCompany(map);

        // 部门id转化为部门名称，使用id从redis中获取

        List<Department> result = departmentService.listDepartment(map.get("company_id").toString());
        List<String> deptlist = new ArrayList<String>();
        Map deptmap = new HashMap();
        for (Department d : result) {
            deptmap.put(d.getId(), d.getName());
        }

        for (Map scr : signinConfirmRecord) {
            scr.put("dept_name", deptmap.get(scr.get("dept_id") + ""));
            scr.put("dept_id",scr.get("dept_id") + "");
        }

        //分页对象2
        return signinConfirmRecord;
    }

    @Override
    public Map signinConfirmDetailById(String id) throws Exception {
        Map res = new HashMap();
        res = tsigninconfirmmapper.signinConfirmDetailById(id);
        // 处理缺卡打卡点


        String companyId ="" ;
        if (!OaMapUtils.mapIsAnyBlank(res, "company_id")) {
            companyId = res.get("company_id")+"";
        }
        String userId ="" ;
        if (!OaMapUtils.mapIsAnyBlank(res, "user_id")) {
            userId = res.get("user_id")+"";
        }
        String day ="";
        if (!OaMapUtils.mapIsAnyBlank(res, "day")) {
            day = res.get("day")+"";
        }

        List<Map> signrecordmaplist =(List<Map>)res.get("signin_record");
        // 合并缺卡打卡点 逻辑
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        Date date=sdf.parse(day);

        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        System.out.println("开始时间："+calendar.getTime());
        long start = calendar.getTime().getTime();
        calendar.set(Calendar.HOUR,23);
        calendar.set(Calendar.MINUTE,59);
        calendar.set(Calendar.SECOND,59);
        calendar.set(Calendar.MILLISECOND,999);
        System.out.println("结束时间："+calendar.getTime());

        long end = calendar.getTime().getTime();

        List<JSONObject> cps = clockinService.getNotSignedCheckPoints(companyId,userId,start,end);

        // 按照打卡时间正序排序

        if(null!=cps){
            Collections.sort(cps, new Comparator<JSONObject>(){
                @Override
                public int compare(JSONObject o1, JSONObject o2) {
                    String s1 = String.valueOf(o1.get("baseCheckTime"));
                    String s2 = String.valueOf(o2.get("baseCheckTime"));
                    return s1.compareTo(s2);
                }
            });
        }

        res.put("attendance_point",cps);

        return res;
    }

    /**
     *
     * @param id 签到id
     * @param status 状态
     * @param confirm_user_id 审批者id
     * @param attendance_id 打卡点id
     * @throws Exception
     * 1 确认签到为’有效‘时confirm=1,status='VALID',confirm_user_id='(主管id)',attendance_id='缺卡记录的id')，
     * 2 确认签到为’无效‘时(confirm=1,status='INVALID',invalid_reason='(主管姓名)确认无效',confirm_user_id='(主管id)')
     * 3 当‘外出签到确认任务’下的所有签到的确认字段均为‘已确认’(confirm=1)时，更新这条外出申请对应的待办的状态为‘完成’
     *      *  ，并修改‘外出签到确认任务’的状态为’已完成‘
     *      confirm_user_id 主管id     admin_id 人事id   ，主管确认confirm 不变，人事确认confirm = 1
     */
    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void signinConfirm(String id, String status, String confirm_user_id, String admin_id, String attendance_id) throws Exception {
        String[] ids = id.split(",");
        for(String idtmp : ids){
            SigninRecord signinRecord = siginRecordService.getById(idtmp);
            //校验关联的签到确认单是否有效
//            ExamineOutApply examineOutApply  = examineOutApplyMapper.selectById(signinRecord.getOutApplyId());
//            if(examineOutApply == null || examineOutApply.getDeleted() == 1){
//                throw new OaSigninConfirmlException(EnumSigninConfirmError.SIGNINCONFIRM_NO_DATA_EXIST);
//            }
            if(null!=signinRecord&&null!=signinRecord.getConfirm()&&signinRecord.getConfirm()==1){
                throw new OaSigninConfirmlException(EnumSigninConfirmError.SIGNINCONFIRM_ALREADY_CONRIRM);
            }
             SigninConfirm signinConfirms = tsigninconfirmmapper.selectById(signinRecord.getSigninConfirmId());
            if(signinConfirms == null || signinConfirms.getDeleted() == 1){
                throw new OaSigninConfirmlException(EnumSigninConfirmError.SIGNINCONFIRM_NO_DATA_EXIST);
            }
            if(!StringUtils.isEmpty(admin_id)){
                signinRecord.setConfirm(1);
                signinRecord.setConfirmUserId(admin_id);
            }
            if(!StringUtils.isEmpty(confirm_user_id)){
                signinRecord.setConfirmUserId(confirm_user_id);
            }

            if(SigninRecordStatusEnum.VALID.getKey().equals(status)){
                signinRecord.setStatus(SigninRecordStatusEnum.VALID.getKey());
                signinRecord.setAttendanceId(Long.parseLong(attendance_id));
                // 人事修改为有效后，清除确认无效原因
                signinRecord.setInvalidReason(" ");
            }else if(SigninRecordStatusEnum.INVALID.getKey().equals(status)){
                signinRecord.setStatus(SigninRecordStatusEnum.INVALID.getKey());
                signinRecord.setAttendanceId(0L);
                // 人事不为空，则存储人事账户id 否则存储主管名称
                String userName = admin_id;
                UserDTO user = userService.getUserByUserId(confirm_user_id,signinRecord.getCompanyId());
                if(null!=user){
                    userName = user.getName();
                }

                signinRecord.setInvalidReason("("+userName+")确认无效");
            }
            siginRecordService.saveOrUpdate(signinRecord);
            //签到确认之后判断是否同步合并和统计数据
            if(StringUtils.isNotBlank(attendance_id)){
                mergeStatisticAttendance(signinRecord,attendance_id);
            }

            // 根据签到记录id获取所属签到确认id
            String signConfirmId = tsigninconfirmmapper.signConfirmIdBySignRecordId(idtmp);
            if(StringUtils.isEmpty(signConfirmId)){
                log.error("获取签到确认信息："+signConfirmId+"失败");
                throw new OaSigninConfirmlException(EnumSigninConfirmError.SIGNINCONFIRM_NOT_DATA);
            }
            // 判断签到确认下所有签到记录是否均已confirm=1
            int num = tsigninconfirmmapper.signConfirmAllDone(signConfirmId);
            if(StringUtils.isEmpty(signConfirmId)){
                log.error("获取签到确认下所有签到记录确认信息信息："+signConfirmId+"失败");
                throw new OaSigninConfirmlException(EnumSigninConfirmError.SIGNINCONFIRM_NOT_DATA);
            }
            // yes 1 发送代办变更  2 更新signconfirm表
            if( 0==num ){
                // num 为0 则表示全部记录已经确认
                // 修改‘外出签到确认任务’的状态为’已完成‘
                SigninConfirm signinConfirm = tsigninconfirmmapper.selectById(signConfirmId);
                signinConfirm.setStatus(SigninConfirmStatusEnum.COMPLETED.getKey());
                tsigninconfirmmapper.updateById(signinConfirm);
                // 更新这条外出申请对应的待办的状态为‘完成’
//                String workRecordId = signinConfirm.getDdWorkrecordId();
//                String userId = signinRecord.getUserId();
//                String companyId = signinRecord.getCompanyId();
//                UpdateBacklogDTO updateBacklogDTO = new UpdateBacklogDTO();
//                updateBacklogDTO.setCompanyId(companyId);
//                updateBacklogDTO.setManagerId(confirm_user_id);
//                updateBacklogDTO.setRecordId(workRecordId);
//                dingDingBacklogService.updateToBacklog(updateBacklogDTO);
            }
        }
    }

    /**
     * 签到确认之后判断是否同步合并和统计数据
     * @param signinRecord
     */
    private void mergeStatisticAttendance(SigninRecord signinRecord,String attendanceId){
        String userId = signinRecord.getUserId();
        String companyId = signinRecord.getCompanyId();
        Long checkinTime = signinRecord.getCheckinTime();
        String month = LocalDateTimeUtils.formatDateTime(checkinTime,"yyyy-MM");
        List<ClockinRecordMergeDTO> clockinRecordMergeDTOList = clockinRecordMergeMapper.clockinRecordMergeByUserId(companyId,month,userId);
        //员工这个月还没有合并过考勤数据
        if(CollectionUtils.isEmpty(clockinRecordMergeDTOList)){
            return;
        }
        //修改merge表数据
        ClockinRecordMergeDTO clockinRecordMergeDTO = clockinRecordMergeDTOList.get(0);
        ClockinRecordMerge clockinRecordMerge = new ClockinRecordMerge();
        clockinRecordMerge.setId(clockinRecordMergeDTO.getMergeRecordId());
        clockinRecordMerge.setModifyTime(LocalDateTimeUtils.getMilliByTime(LocalDateTime.now()));
        String content = clockinRecordMergeDTO.getContent();
        if(StringUtils.isBlank(content)){
            return;
        }
        Map<String,Object> originalDataMap = MapUtils.stringToMap(content);
        long checkinDay = LocalDateTimeUtils.getDayStart(checkinTime);
        List<Map<String,Object>> valMapList = (List)originalDataMap.get(String.valueOf(checkinDay));
        if(CollectionUtils.isEmpty(valMapList)){
            return;
        }
        valMapList.stream().forEach(val ->{
            if(attendanceId.equals(val.get(EnumClockinMergeStatus.ID.getKey()).toString())){
                String status = signinRecord.getStatus();
                if(SigninRecordStatusEnum.VALID.getKey().equals(status)){
                    val.put(EnumClockinMergeStatus.STATUS.getKey(), EnumClockinPointStatus.SIGNIN.getKey());
                }else if(SigninRecordStatusEnum.INVALID.getKey().equals(status)){
                    val.put(EnumClockinMergeStatus.STATUS.getKey(), EnumClockinPointStatus.NOTSIGNED.getKey());
                }
            }
        });

        JSONObject jsonObject = (JSONObject)JSON.toJSON(originalDataMap);
        String finalData = jsonObject.toString();
        clockinRecordMerge.setContent(finalData);
        clockinRecordMergeService.updateById(clockinRecordMerge);
        //判断员工这个月有没有统计过考勤数据
        QueryWrapper<AttendanceRecord> attendanceRecordQueryWrapper = new QueryWrapper<>();
        attendanceRecordQueryWrapper.lambda().eq(AttendanceRecord::getUserId,userId);
        attendanceRecordQueryWrapper.lambda().eq(AttendanceRecord::getCompanyId,companyId);
        attendanceRecordQueryWrapper.lambda().eq(AttendanceRecord::getMonth,month);
        attendanceRecordQueryWrapper.lambda().eq(AttendanceRecord::getDeleted,0);
        List<AttendanceRecord> attendanceRecordList = attendanceRecordService.list(attendanceRecordQueryWrapper);
        //这个员工这个月还没有统计过考勤数据
        if(CollectionUtils.isEmpty(attendanceRecordList)){
            return;
        }
        //修改统计表数据
        AttendanceRecord attendanceRecord = attendanceRecordList.get(0);
        ClockinRecordMergeDTO recordMergeDTO = new ClockinRecordMergeDTO();
        recordMergeDTO.setAttendanceRecordId(attendanceRecord.getId());
        recordMergeDTO.setUserId(attendanceRecord.getUserId());
        recordMergeDTO.setUserName(attendanceRecord.getUserName());
        recordMergeDTO.setContent(finalData);
        recordMergeDTO.setCompanyId(attendanceRecord.getCompanyId());
        recordMergeDTO.setStatisticalMonth(attendanceRecord.getMonth());
        recordMergeDTO.setTaskInstanceId(attendanceRecord.getTaskInstanceId());
        attendanceRecordService.updateAttendanceResult(recordMergeDTO);
    }



}
