package com.sancai.oa.clockin.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sancai.oa.clockin.entity.ClockinDepartment;
import com.sancai.oa.clockin.entity.ClockinPoint;
import com.sancai.oa.clockin.entity.ClockinRecord;
import com.sancai.oa.clockin.entity.ClockinRecordGrapDTO;
import com.sancai.oa.clockin.enums.EnumClockinPointStatus;
import com.sancai.oa.clockin.mapper.ClockinDepartmentMapper;
import com.sancai.oa.clockin.mapper.ClockinRecordMapper;
import com.sancai.oa.clockin.service.ClockinService;
import com.sancai.oa.clockin.service.IClockinRecordService;
import com.sancai.oa.clockin.threadpool.ClockinTask;
import com.sancai.oa.core.threadpool.ThreadPoolTool;
import com.sancai.oa.core.threadpool.ThreadResult;
import com.sancai.oa.dingding.clockin.DingDingClockinService;
import com.sancai.oa.examine.utils.UUIDS;
import com.sancai.oa.quartz.entity.TaskInstance;
import com.sancai.oa.quartz.entity.TaskInstanceTime;
import com.sancai.oa.quartz.service.ITaskInstanceService;
import com.sancai.oa.quartz.util.TaskMessage;
import com.sancai.oa.signin.entity.SigninRecord;
import com.sancai.oa.signin.service.ISigninRecordService;
import com.sancai.oa.user.entity.UserDTO;
import com.sancai.oa.user.entity.UserDepartment;
import com.sancai.oa.user.service.IUserService;
import com.sancai.oa.utils.ListUtils;
import com.sancai.oa.utils.LocalDateTimeUtils;
import com.taobao.api.ApiException;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * 打卡service
 * @Author chenm
 * @create 2019/8/1 14:06
 */
@Slf4j
@Service
public class ClockinServiceImpl extends ServiceImpl<ClockinRecordMapper, ClockinRecord>  implements ClockinService {

    @Autowired
    private DingDingClockinService dingDingClockinService;

    @Autowired
    private ClockinServiceImpl clockinService;
    @Autowired
    IClockinRecordService clockinRecordService;

    @Autowired
    private ITaskInstanceService iTaskInstanceService;

    @Autowired
    private ISigninRecordService signinRecordService;

    @Autowired
    private IUserService userService;

    @Autowired
    ClockinDepartmentMapper clockinDepartmentMapper;

    @Autowired
    private DataSourceTransactionManager transactionManager;

    @Autowired
    private ThreadPoolTool threadPoolTool;
    @Override
    /**
     * 抓取公司下的考勤记录
     */
    public void graspClockinRecord(String taskInstanceId,String companyId) throws Exception {
        TaskInstance taskInstance = iTaskInstanceService.getById(taskInstanceId);
        if(taskInstance == null){
            TaskMessage.addMessage(taskInstanceId,"任务实例不存在："+taskInstanceId);
            return;
        }

        log.error("test log === companyId:"+companyId);

//        int row = clockinRecordService.deleteByTaskInstanceId(taskInstanceId);
        TaskInstanceTime taskInstanceTime = iTaskInstanceService.resetStartAndEndTime(taskInstance);

        log.error("test log === taskInstanceTime:"+taskInstanceTime.getStartTime()+"----"+taskInstanceTime.getEndTime());

        //最多抓取7天的，如果超过7天，需要拆分为多段
        List<Pair> intervalTimes = LocalDateTimeUtils.getIntervalTimes(taskInstanceTime.getStartTime(), taskInstanceTime.getEndTime(), 7);

        List<UserDTO> users = userService.listUser(companyId,0,taskInstanceTime.getStartTime(),taskInstanceTime.getEndTime());
        System.out.println("总用户数："+users.size());
        TaskMessage.addMessage(taskInstanceId,"总用户数："+users.size());
        //钉钉考勤一次只能取50个用户
        List<List<UserDTO>> userIdBatch = ListUtils.fixedGrouping(users,50);

        boolean isFinish = false;
        int index = 0;
        for (Pair intervalTime : intervalTimes) {
            long intervalTimeStart = (long) intervalTime.getKey();
            long intervalTimeEnd = (long) intervalTime.getValue();
            //抓取公司下的考勤记录

            index ++;
            if(index == intervalTimes.size()){
                isFinish = true;
            }
            graspClockinRecord(taskInstanceId,companyId,userIdBatch,intervalTimeStart,intervalTimeEnd,isFinish);
            TaskMessage.addMessage(taskInstanceId,"==========================================================================");
        }

    }

    @Override
    /**
     * 抓取公司下的考勤记录
     */
    public void graspClockinRecord(String taskInstanceId, String companyId,List<List<UserDTO>> userIdBatch, long start, long end,boolean isFinish) throws Exception {
        TaskMessage.addMessage(taskInstanceId,"考勤抓取  companyId："+companyId);
        TaskMessage.addMessage(taskInstanceId,"抓取时间范围: "+LocalDateTimeUtils.formatDateTime(start)+" 到 "+ LocalDateTimeUtils.formatDateTime(end));

        log.error("test log ===companyId:"+companyId+" 抓取时间范围:"+LocalDateTimeUtils.formatDateTime(start)+" 到 "+ LocalDateTimeUtils.formatDateTime(end));

        TaskInstance taskInstance = iTaskInstanceService.getById(taskInstanceId);
        if(taskInstance == null){
            return;
        }
        int threadCount = 5;

        long startTime = System.currentTimeMillis();
        Map<String,Object> params = new HashMap<>();
        params.put("taskInstanceId",taskInstanceId);
        params.put("companyId",companyId);
        params.put("start",start);
        params.put("end",end);
        params.put("clockinService",clockinService);
        params.put("startTime",startTime);
        params.put("isFinish",isFinish);
        ThreadResult result =  threadPoolTool.excuteTaskFuture(transactionManager,taskInstanceId,userIdBatch,threadCount,params,isFinish, ClockinTask.class);

        List<Future<ThreadResult>> resList = new ArrayList<Future<ThreadResult>>();
        if(result.getFlag()){
            resList = result.getData();
        }else{
            throw result.getE();
        }

        for(Future<ThreadResult> future: resList){
            ThreadResult r = future.get();
            List<ClockinRecordGrapDTO> list = r.getData();
            for(ClockinRecordGrapDTO grapDTO : list){
                Map<String, UserDTO> users = grapDTO.getUserMap();
                Map<String, Map<Long,List<ClockinPoint>>> userClockinPointMap = grapDTO.getClockinMap();
                for(Map.Entry<String,Map<Long,List<ClockinPoint>>> entry:userClockinPointMap.entrySet()){
                    //entry是一个员工的本次抓取的考勤数据
                    String userId = entry.getKey();
                    UserDTO user = users.get(userId);

                    Map<Long,List<ClockinPoint>> cpMap = entry.getValue();
                    //存储一个员工的本次抓取的考勤数据
                    saveUserClockin(taskInstanceId,companyId,user,cpMap);
                }
            }

        }

    }




    @Override
    public Map<String, Map<Long,List<ClockinPoint>>> graspClockin(String taskInstanceId, String companyId, long start, long end,Map<String,UserDTO> users){
        List<String> userIds = new ArrayList<>(users.keySet());
        Map<String, Map<Long,List<ClockinPoint>>> userClockinPointMap = dingDingClockinService.onedayClockinRecords(companyId, start, end,userIds );
        return userClockinPointMap;
    }


    @Override
    /**
     *  取一个人一个月的考勤数据
     */
    public ClockinRecord getUserMonthClockinRecord(String companyId,String userId, String month) {
        QueryWrapper<ClockinRecord> wrapper = new QueryWrapper<ClockinRecord>();
        wrapper.eq("company_id",companyId);
        wrapper.eq("user_id",userId);
        wrapper.eq("month",month);
        wrapper.eq("deleted",0);

        ClockinRecord record = this.getOne(wrapper);
        return record;
    }

    @Override
    /**
     * 取一个人一段时间内的缺卡的打卡点
     * @param userId
     * @param start
     * @param end
     * @return
     */
    public List<JSONObject> getNotSignedCheckPoints(String companyId,String userId,long start,long end){
        //得到月份
        String month = new SimpleDateFormat("yyyy-MM").format(start);
        //得到当天日期时间戳
        long day = LocalDateTimeUtils.getDayStart(start);
        //取一个人一个月的考勤数据
        ClockinRecord clockinRecord = getUserMonthClockinRecord(companyId,userId,month);
        if(clockinRecord == null || clockinRecord.getContent() == null){
            return null;
        }
        String content = clockinRecord.getContent();
        Map<Long,List<JSONObject>> crpMap = JSON.parseObject(content,Map.class);
        //取出当天的打卡点
        List<JSONObject> clockinPoints = crpMap.get(day+"");
        if(clockinPoints == null || clockinPoints.size() == 0){
            return null;
        }
        List<JSONObject> result = new ArrayList<JSONObject>();
        for (JSONObject cp : clockinPoints) {
            //是否在区间内
            long id = cp.getLong("id");
            long baseCheckTime = cp.getLong("baseCheckTime");
            String status = cp.getString("status");
            boolean isBetween = start <=  baseCheckTime &&  baseCheckTime <= end;
            //是否是未打卡
            boolean isNotSigned = EnumClockinPointStatus.NOTSIGNED.toString().equals(status);
            //是否已关联了签到
            SigninRecord signinRecord = signinRecordService.signinDetailByAttendanceId(id);
            boolean isConfirmSignin = (signinRecord != null);
            if(isNotSigned && isBetween && !isConfirmSignin){
                result.add(cp);
            }
        }

        return result;
    }

    /**
     * 存储一个员工的本次抓取的考勤数据
     * @param taskInstanceId
     * @param companyId
     * @param user
     * @param cpMap
     */
    private void saveUserClockin(String taskInstanceId,String companyId,UserDTO user, Map<Long, List<ClockinPoint>> cpMap) {
        //一个员工的本次的考勤数据有可能跨月，按月份拆分开
        Map<String,Map<Long,List<ClockinPoint>>> monthMap = getMonthMap(cpMap);

        JSONObject jo = (JSONObject) JSON.toJSON(cpMap);
        log.error("test log ===saveUserClockin companyId:"+companyId+",userId:"+user.getUserId()+"考勤 record -- cpMap:"+jo.toJSONString());
        for(Map.Entry<String,Map<Long,List<ClockinPoint>>> monthEntry : monthMap.entrySet()){
            String month = monthEntry.getKey();
            Map<Long,List<ClockinPoint>> oneMonthCpMap = monthEntry.getValue();

            ClockinRecord cr  = getUserMonthClockinRecord(user.getCompanyId(),user.getUserId(),month);
            if(cr == null){
                //第一天的数据
                saveClockinRecord(taskInstanceId,companyId,user,month,oneMonthCpMap);
            }else{
                //追加
                updateClockinRecord(user,cr,oneMonthCpMap);
            }
        }
    }

    /**
     * 更新打卡记录
     * @param cr
     * @param cpMap
     * @return
     */
    private boolean updateClockinRecord(UserDTO user,ClockinRecord cr, Map<Long, List<ClockinPoint>> cpMap) {

        List<UserDepartment> userDepartments= user.getUserDepartments();

        if(null!=userDepartments&&userDepartments.size()>0){
            for(UserDepartment userDepartment : userDepartments){
                String deptId = userDepartment.getDeptId();
                QueryWrapper<ClockinDepartment> queryWrapper = new QueryWrapper();
                queryWrapper.eq("clockin_record_id",cr.getId());
                queryWrapper.eq("deleted",0);
                queryWrapper.eq("dept_id",Integer.parseInt(deptId));
                ClockinDepartment cd = clockinDepartmentMapper.selectOne(queryWrapper);
                if(cd == null){
                    cd = new ClockinDepartment(UUIDS.getID(),cr.getId(),Integer.parseInt(deptId),System.currentTimeMillis(),null , 0);
                    clockinDepartmentMapper.insert(cd);
                }
            }
        }

        Map<Long,List<ClockinPoint>> crpMap = JSON.parseObject(cr.getContent(), ConcurrentHashMap.class);

        Iterator<Map.Entry<Long,List<ClockinPoint>>> iterator = cpMap.entrySet().iterator();

        while(iterator.hasNext()){
            Map.Entry entry=iterator.next();
            Long day = Long.parseLong(entry.getKey().toString());
            crpMap.remove(day+"");
            crpMap.put(day,(List<ClockinPoint>) entry.getValue());
        }


        String content = clockinPointMap2Str(crpMap);
        cr.setContent(content);

        log.error("test log updateClockinRecord userId:"+user.getUserId()+" ===update:"+cr.getCompanyId()+" content:"+content);
        cr.setModifyTime(System.currentTimeMillis());
        boolean r = updateById(cr);
        return r;
    }

    /**
     * 新增打卡记录
     * @param companyId
     * @param user
     * @param month
     * @param cpMap
     * @return
     * @throws ApiException
     */
    private boolean saveClockinRecord(String taskInstanceId,String companyId,UserDTO user,String month,Map<Long,List<ClockinPoint>> cpMap){


        ClockinRecord cr = new ClockinRecord();
        cr.setId(UUIDS.getID());
        cr.setUserId(user.getUserId());
        cr.setMonth(month);
        cr.setUserName(user.getName());
        cr.setCompanyId(companyId);
        cr.setCreateTime(LocalDateTime.now().toInstant(ZoneOffset.ofHours(8)).toEpochMilli());
        cr.setDeleted(0);
        cr.setTaskInstanceId(taskInstanceId);

        String content = clockinPointMap2Str(cpMap);
        cr.setContent(content);
        log.error("test log saveClockinRecord userId:"+user.getUserId()+" ===insert:"+companyId+" content:"+content);

        boolean saveResult = save(cr);
        if(!saveResult){
            return false;
        }

        List<UserDepartment> userDepartments= user.getUserDepartments();

        if(CollectionUtils.isEmpty(userDepartments)){
            return false;
        }

        userDepartments.stream().forEach(userDepartment -> {
            ClockinDepartment cd = new ClockinDepartment(UUIDS.getID(),cr.getId(),Integer.parseInt(userDepartment.getDeptId()),System.currentTimeMillis(),null , 0);
            clockinDepartmentMapper.insert(cd);
        });

        return true;

    }

    private String clockinPointMap2Str(Map<Long, List<ClockinPoint>> cpMap) {
        Iterator<Map.Entry<Long,List<ClockinPoint>>> iterator = cpMap.entrySet().iterator();

        Map result = new HashMap<>();
        while(iterator.hasNext()){
            Map.Entry entry=iterator.next();
            Long day = Long.parseLong(entry.getKey().toString());
            List<ClockinPoint> cps = (List<ClockinPoint>) entry.getValue();
            HashSet h = new HashSet(cps);
            List<ClockinPoint> newCps = new ArrayList<>();
            newCps.addAll(h);
            result.put(day,newCps);
        }
        JSONObject jo = (JSONObject) JSON.toJSON(result);
        return  jo.toJSONString();
    }

    /**
     * 一个员工的本次的考勤数据有可能跨月，按月份拆分开
     * @param cpMap
     * @return
     */
    private Map<String,Map<Long,List<ClockinPoint>>> getMonthMap(Map<Long,List<ClockinPoint>> cpMap){
        Map<String,Map<Long,List<ClockinPoint>>> monthMap = new HashMap<String,Map<Long,List<ClockinPoint>>>();
        for(Map.Entry<Long,List<ClockinPoint>> entry : cpMap.entrySet()){
            Long day = entry.getKey();
            List<ClockinPoint>  cps = entry.getValue();
            String month = LocalDateTimeUtils.formatDateTime(day,"yyyy-MM");
            Map<Long,List<ClockinPoint>> oneMonth = monthMap.get(month);
            if(oneMonth == null){
                oneMonth = new HashMap<Long,List<ClockinPoint>>();
            }

            oneMonth.put(day,cps);
            monthMap.put(month,oneMonth);
        }
        return monthMap;

    }
}
