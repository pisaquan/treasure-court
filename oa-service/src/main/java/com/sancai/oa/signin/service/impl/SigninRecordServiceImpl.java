package com.sancai.oa.signin.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dingtalk.api.response.OapiCheckinRecordGetResponse;
import com.dingtalk.api.response.OapiCheckinRecordResponse;
import com.dingtalk.api.response.OapiDepartmentGetResponse;
import com.github.pagehelper.PageHelper;
import com.sancai.oa.core.exception.OaException;
import com.sancai.oa.core.redis.RedisUtil;
import com.sancai.oa.department.entity.Department;
import com.sancai.oa.department.service.IDepartmentService;
import com.sancai.oa.dingding.backlog.DingDingBacklogService;
import com.sancai.oa.dingding.notify.DingDingNotifyService;
import com.sancai.oa.dingding.notify.OANotifyDTO;
import com.sancai.oa.dingding.report.DingDingReportService;
import com.sancai.oa.dingding.user.DingDingUserService;
import com.sancai.oa.examine.entity.ExamineOutApply;
import com.sancai.oa.examine.entity.enums.ExamineStatusEnum;
import com.sancai.oa.examine.mapper.ExamineOutApplyMapper;
import com.sancai.oa.quartz.entity.TaskInstance;
import com.sancai.oa.quartz.entity.TaskInstanceTime;
import com.sancai.oa.quartz.service.ITaskInstanceService;
import com.sancai.oa.quartz.util.TaskMessage;
import com.sancai.oa.report.entity.modify.DataMap;
import com.sancai.oa.signin.entity.SigninDepartment;
import com.sancai.oa.signin.entity.SigninRecord;
import com.sancai.oa.signin.entity.SigninRecordDTO;
import com.sancai.oa.signin.entity.enums.SigninTypeEnum;
import com.sancai.oa.signin.exception.EnumSigninError;
import com.sancai.oa.signin.exception.OaSigninlException;
import com.sancai.oa.signin.mapper.SigninDepartmentMapper;
import com.sancai.oa.signin.mapper.SigninRecordMapper;
import com.sancai.oa.signin.service.ISigninDepartmentService;
import com.sancai.oa.signin.service.ISigninRecordService;
import com.sancai.oa.signinconfirm.entity.SigninConfirm;
import com.sancai.oa.signinconfirm.mapper.SigninConfirmMapper;
import com.sancai.oa.typestatus.enums.TimedTaskStatusEnum;
import com.sancai.oa.user.entity.User;
import com.sancai.oa.user.entity.UserDTO;
import com.sancai.oa.user.entity.UserDepartment;
import com.sancai.oa.user.service.IUserService;
import com.sancai.oa.utils.ListUtils;
import com.sancai.oa.utils.LocalDateTimeUtils;
import com.sancai.oa.utils.OaMapUtils;
import com.sancai.oa.utils.UUIDS;
import com.taobao.api.ApiException;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 签到记录 服务实现类
 * </p>
 *
 * @author fans
 * @since 2019-07-19
 */
@Service
@Slf4j
public class SigninRecordServiceImpl extends ServiceImpl<SigninRecordMapper, SigninRecord> implements ISigninRecordService {

    @Autowired
    private SigninRecordMapper signinRecordMapper;
    @Autowired
    private SigninDepartmentMapper signinDepartmentMapper;
    @Autowired
    private IDepartmentService departmentService;
    @Autowired
    private ExamineOutApplyMapper examineOutApplyMapper;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private SigninConfirmMapper signinConfirmMapper;
    @Autowired
    private DingDingReportService dingDingReportService;
    @Autowired
    private DingDingNotifyService dingDingNotifyService;
    @Autowired
    private DingDingBacklogService dingDingBacklogService;
    @Autowired
    private ITaskInstanceService iTaskInstanceService;
    @Autowired
    private DingDingUserService dingDingUserService;
    @Autowired
    private IUserService iUserService;
    @Autowired
    private ISigninDepartmentService iSigninDepartmentService;
    @Autowired
    private ITaskInstanceService taskInstanceService;

    /**
     * 钉钉通知、待办跳转域名
     */
    @Value("${notify.signinConfirm}")
    private String signinConfirmUrl;
    /**
     * 测试环境给部门主管发送签到确认人员id
     */
    @Value("${notify.signinConfirm-test-deptid}")
    private String signinConfirmTestDeptid;
    /**
     * 获取子公司每天所有用户签到记录(根据部门获取签到记录)
     * @return LinkedHashSet<Map<String, Object>>（用户属于多部门的数据去重）
     */
    @Override
    public  LinkedHashSet<OapiCheckinRecordResponse.Data> checkinListByDeptList(String companyId,long intervalTimeStart , long intervalTimeEnd ,String taskInstanceId){
        if(StringUtils.isAllBlank(companyId)){
            log.error("取子公司每天所有用户签到记录companyId为空");
            throw new OaSigninlException(EnumSigninError.PARAMETER_IS_NULL_COMPANYID);
        }
        LinkedHashSet<OapiCheckinRecordResponse.Data> allCheckDataList = new LinkedHashSet<>();
        //初始部门先用顶级部门1去查，可以查到直接返回，查不到递归子部门
        Long deptId = 1L;
        LinkedHashSet<OapiCheckinRecordResponse.Data>  allCheckData = checkinList(deptId,companyId,intervalTimeStart,intervalTimeEnd,taskInstanceId);
        if(allCheckData !=null && allCheckData.size()>0){
            allCheckDataList.addAll(allCheckData);
        }
        TaskMessage.addMessage(taskInstanceId,"签到抓取:总数量"+allCheckDataList.size());
        return allCheckDataList;

    }

    /**
     * 递归获取用户签到记录数据
     * 备注：在根据部门获取用户签到记录时，由于（钉钉部门获取用户签到记录接口：目前最多获取1000人以内的签到数据，如果所传部门ID及其子部门下的user超过1000，会报错）情况，
     * 当部门的用户超过1000人递归子部门时会遗漏与子部门同级的部分用户，目前根据部门id获取用户集合又不能区分子部门内和子部门外（就是遗漏）的用户，所以这种情况会导致和子部门同级存在的用户数据遗漏
     * @param deptId 部门id
     * @param companyId 公司id
     * @param intervalTimeStart 开始时间
     * @param intervalTimeEnd 结束时间
     * @return
     */
    private  LinkedHashSet<OapiCheckinRecordResponse.Data>  checkinList(Long deptId ,String companyId ,long intervalTimeStart ,long intervalTimeEnd,String taskInstanceId){
        Long size = 100L;
        LinkedHashSet<OapiCheckinRecordResponse.Data> allCheckDataList = new LinkedHashSet<>();
        Long pageNo = 0L;
        while (pageNo != null) {
            Map<String, List<OapiCheckinRecordResponse.Data>>  checkMap = dingDingReportService.checkinUserByDepartmentId(String.valueOf(deptId), pageNo,size,  companyId,intervalTimeStart,intervalTimeEnd);
            if (!OaMapUtils.mapIsAnyBlank(checkMap,"data")) {
                //有数据添加
                allCheckDataList.addAll(checkMap.get("data"));
                pageNo += size;
            }else if(checkMap!=null&&checkMap.get("data") == null){
                //无数据退出
                pageNo = null;
            }else{
                //当返回数据为null表示部门超过一千人报错，根据该部门递归查询
                TaskMessage.addMessage(taskInstanceId,"签到抓取部门"+deptId+"部门超过一千人部门递归查询");
                List<Long> allDepartmentIdSet = dingDingReportService.allDepartmentIds(companyId, String.valueOf(deptId));
                TaskMessage.addMessage(taskInstanceId,"签到抓取部门"+deptId+"部门超过一千人部门递归查询到部门："+allDepartmentIdSet.size()+"个，"+JSONObject.toJSON(allDepartmentIdSet));
                for (Long deptIds :allDepartmentIdSet){
                    //递归查询
                    LinkedHashSet<OapiCheckinRecordResponse.Data> data = checkinList(deptIds,companyId ,intervalTimeStart,intervalTimeEnd ,taskInstanceId);
                    if(data != null && data.size()> 0){
                        TaskMessage.addMessage(taskInstanceId,"签到抓取部门"+deptId+"数据"+data.size()+"条");
                        allCheckDataList.addAll(data);
                    }
                }
                break;
            }
        }
        return allCheckDataList;
    }

    /**
     * 取部门下遗漏的的用户签到记录
     * @param deptId
     * @param companyId
     * @param intervalTimeStart
     * @param intervalTimeEnd
     * @return
     */
    private  LinkedHashSet<OapiCheckinRecordResponse.Data>  checkinRecordDataVo (String deptId ,String companyId ,long intervalTimeStart ,long intervalTimeEnd){
        LinkedHashSet<OapiCheckinRecordResponse.Data> allCheckDataList = new LinkedHashSet<>();
        List<String> userIdListByDeptId= dingDingReportService.allUserIdByDeptId(deptId,companyId);
        List<List<String>> openEmpSimples = ListUtils.fixedGrouping(userIdListByDeptId,10);
        for (List<String> userIdList :openEmpSimples){
            Long pageNo1 = 0L;
            while (pageNo1 != null){
                String userIds = String.join(",", userIdList);
                OapiCheckinRecordGetResponse.PageResult pageResult = dingDingReportService.checkinUser(userIds,pageNo1,100L,companyId,intervalTimeStart,intervalTimeEnd);
                if(pageResult!=null&&pageResult.getPageList().size()>0){
                    List<OapiCheckinRecordGetResponse.CheckinRecordVo> checkinRecordVos1 = pageResult.getPageList();
                    //根据用户查询签到数据返回实体统一转化为部门获取签到记录实体
                    LinkedHashSet<OapiCheckinRecordResponse.Data> data = checkinRecordVo(checkinRecordVos1,companyId);
                    allCheckDataList.addAll(data);
                    pageNo1 = pageResult.getNextCursor();
                }else {
                    pageNo1 = null;
                }
            }
        }
        return allCheckDataList;
    }
    /**
     * 根据用户查询签到数据返回实体统一转化为部门获取签到记录实体
     * @param checkinRecordVos1
     * @param companyId
     * @return
     */
    private LinkedHashSet<OapiCheckinRecordResponse.Data>  checkinRecordVo ( List<OapiCheckinRecordGetResponse.CheckinRecordVo> checkinRecordVos1, String companyId){
        LinkedHashSet<OapiCheckinRecordResponse.Data> allCheckDataList = new LinkedHashSet<>();
        for (OapiCheckinRecordGetResponse.CheckinRecordVo vo :checkinRecordVos1){
            OapiCheckinRecordResponse.Data data = new OapiCheckinRecordResponse.Data();
            data.setDetailPlace(vo.getDetailPlace());
            data.setImageList(vo.getImageList());
            data.setLatitude(vo.getLongitude());
            data.setLongitude(vo.getLatitude());
            data.setPlace(vo.getPlace());
            data.setRemark(vo.getRemark());
            data.setUserId(vo.getUserid());
            data.setTimestamp(vo.getCheckinTime());
            String userName = dingDingUserService.getUsetName(companyId,vo.getUserid());
            data.setName(userName);
            allCheckDataList.add(data);
        }
        return allCheckDataList;

    }
    /**
     * 用户签到记录封装成实体类集合
     * @return List
     */
    private List<SigninRecord> checkin(String companyId , long intervalTimeStart , long intervalTimeEnd,String taskInstanceId) {
        List<SigninRecord> allCheckVoList = new ArrayList<>();
        LinkedHashSet<OapiCheckinRecordResponse.Data> checkDataList =  checkinListByDeptList(companyId,intervalTimeStart,intervalTimeEnd,taskInstanceId);
        List<OapiCheckinRecordResponse.Data> allCheckDataList = checkDataList.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(o -> o.getUserId() + ";" + o.getTimestamp()))), ArrayList::new));

        TaskMessage.addMessage(taskInstanceId,"抓取时间范围: "+LocalDateTimeUtils.formatDateTime(intervalTimeStart)+" 到 "+ LocalDateTimeUtils.formatDateTime(intervalTimeEnd)+"签到数据"+allCheckDataList.size()+"条");
        //封装成实体类集合
        if(allCheckDataList!=null&&allCheckDataList.size()>0){
            Iterator<OapiCheckinRecordResponse.Data> it = allCheckDataList.iterator();
            while (it.hasNext()) {
                SigninRecord tsr = new SigninRecord();
                String id  = UUIDS.getID();
                tsr.setId(id);
                tsr.setStatus(SigninTypeEnum.NEW.getKey());
                tsr.setDeleted(0);
                tsr.setConfirm(0);
                OapiCheckinRecordResponse.Data checkMap = it.next();
                tsr.setCreateTime(System.currentTimeMillis());
                tsr.setCheckinTime(checkMap.getTimestamp());
                tsr.setDetailPlace(checkMap.getDetailPlace());
                tsr.setImageList(JSONObject.toJSONString(checkMap.getImageList()));
                tsr.setLatitude(Float.valueOf(checkMap.getLatitude()));
                tsr.setLongitude(Float.valueOf(checkMap.getLongitude()));
                tsr.setPlace(checkMap.getPlace());
                tsr.setRemark(checkMap.getRemark());
                tsr.setUserId(checkMap.getUserId());
                tsr.setUserName(checkMap.getName().replaceAll(" ", ""));
                tsr.setCompanyId(companyId);
                allCheckVoList.add(tsr);
            }
            return allCheckVoList;
        }
        return null;
    }

    /**
     * 根据实例id查询签到数据
     * @param instanceId
     * @return
     */
    private List<SigninRecord> signinRecordListByInstanceId(String instanceId) {
        QueryWrapper<SigninRecord> signinRecordQueryWrapper = new QueryWrapper<>();
        signinRecordQueryWrapper.lambda().eq(SigninRecord::getTaskInstanceId,instanceId);
        signinRecordQueryWrapper.lambda().eq(SigninRecord::getDeleted,0);
        List<SigninRecord> signinRecordList = list(signinRecordQueryWrapper);
        return signinRecordList;
    }

    /**
     * 删除签到数据
     * @param signinRecordList
     */
    private void signinRecordDataDelete(List<SigninRecord> signinRecordList) {
        List<String> ids = new ArrayList<>();
        //删除记录
        signinRecordList.stream().forEach(SigninRecord -> {SigninRecord.setDeleted(1);ids.add(SigninRecord.getId());});
        for(SigninRecord signinRecord:signinRecordList){
            updateById(signinRecord);
        }
        //删除部门关系表
        SigninDepartment signinDepartment = new SigninDepartment();
        signinDepartment.setDeleted(1);
        UpdateWrapper<SigninDepartment> signinDepartmentUpdateWrapper = new UpdateWrapper<>();
        signinDepartmentUpdateWrapper.lambda().in(SigninDepartment::getSigninInstanceId,ids);
        iSigninDepartmentService.update(signinDepartment,signinDepartmentUpdateWrapper);
    }


    /**
     * 根据companyId + UserId + checkinTime + taskInstanceId 把记录对应的部门集合存入缓存
     * @param recordLists 记录集合
     */
    @Override
    public void recordOriginDeptIdsSaveRedis ( List<DataMap> recordLists){
        try {
            if(recordLists != null && recordLists.size()>0){
                Map<String, List<DataMap>> signinListmap = recordLists.stream() .collect(Collectors.groupingBy(d -> OaMapUtils.fetchKey(String.valueOf(d.get("company_id")),String.valueOf(d.get("user_id")),String.valueOf(d.get("times")).replaceAll(" ",""),String.valueOf(d.get("task_instance_id"))) ));
               if(signinListmap == null){
                   return;
               }
                for (String fetchKey : signinListmap.keySet()) {
                    List<DataMap> recordList = signinListmap.get(fetchKey);
                    if(recordList != null && recordList.size()>0){
                        DataMap signinRecord = recordList.get(0);
                        if(!OaMapUtils.mapIsAnyBlank(signinRecord,"deptList")){
                            redisUtil.set(fetchKey,signinRecord.get("deptList"), 1 * 60 * 60);
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
    }
    /**
     * 导入签到数据
     */
    @Override
    public boolean importEveryDayCheckinData(String taskInstanceId, String companyId){
        TaskInstance taskInstance = iTaskInstanceService.getById(taskInstanceId);
        if(taskInstance == null){
            return false;
        }
        //如果根据实例id查到值,根据实例id删除之前的数据 , 重新获取抓取开始时间和结束时间
        List<SigninRecord> signinRecordList = signinRecordListByInstanceId(taskInstanceId);
        if(CollectionUtils.isNotEmpty(signinRecordList)){
            List<DataMap> signinRecordLists = signinRecordMapper.signinRecordDeptIdList(companyId,taskInstanceId);
            //TODO 根据companyId + UserId + checkinTime + taskInstanceId 把记录对应的部门集合存入缓存
            if(!TimedTaskStatusEnum.EXECUTING.getKey().equals(taskInstance.getStatus())){
                recordOriginDeptIdsSaveRedis(signinRecordLists);
                //删除签到数据
                signinRecordDataDelete(signinRecordList);
            }

        }
        TaskInstanceTime taskInstanceTime = taskInstanceService.resetStartAndEndTime(taskInstance);
        long start = taskInstanceTime.getStartTime();
        long end = taskInstanceTime.getEndTime();
        //钉钉抓取签到数据，时间跨度如果超过7天，需要拆分为多段
        List<Pair> intervalTimes = LocalDateTimeUtils.getIntervalTimes(start,end,7);
        for (Pair intervalTime : intervalTimes) {
            long intervalTimeStart = (long) intervalTime.getKey();
            long intervalTimeEnd = (long) intervalTime.getValue();
            //导入签到数据
            importEveryDayCheckinData(companyId,intervalTimeStart,intervalTimeEnd, taskInstanceId);

        }
        TaskMessage.finishMessage(taskInstanceId);
        return true;
    }

    /**
     * 导入签到数据
     * @param companyId 公司id
     * @return boolean
     */

    @Override
    public  boolean importEveryDayCheckinData(String companyId,long intervalTimeStart , long intervalTimeEnd ,String taskInstanceId ){
        if(StringUtils.isAllBlank(companyId)){
            throw new OaSigninlException(EnumSigninError.PARAMETER_IS_NULL_COMPANYID);
        }
        TaskMessage.addMessage(taskInstanceId,"签到数据抓取  companyId："+companyId);
        TaskMessage.addMessage(taskInstanceId,"抓取时间范围: "+LocalDateTimeUtils.formatDateTime(intervalTimeStart)+" 到 "+ LocalDateTimeUtils.formatDateTime(intervalTimeEnd));
        List<SigninRecord> list = checkin(companyId,intervalTimeStart,intervalTimeEnd,taskInstanceId);
        if(list == null || list.size()==0){
            return true;
        }
        List<SigninRecord> signinRecordListRs = new ArrayList<>();
        List<UserDTO> userList = iUserService.listUser(companyId,0,intervalTimeStart,intervalTimeEnd);
        List<List<SigninRecord>> lists = ListUtils.fixedGrouping(list,100);
        TaskMessage.addMessage(taskInstanceId,"签到抓取到"+list.size()+"条数据"+"按100条分组入库，共分"+lists.size()+"组");
        int group = 0;
        int userNoFind = 0;
        TaskMessage.addMessage(taskInstanceId,"签到抓取数据导入开始");
        for (List<SigninRecord> signinRecordList : lists){
            group++;
            if(signinRecordList == null || signinRecordList.size() == 0){
                continue;
            }
            for (SigninRecord trt : signinRecordList) {
                UserDTO userMap = userList.stream().filter(User -> User.getUserId().equals(trt.getUserId())).findAny().orElse(null);
                String userName = " ";
                if(userMap != null && StringUtils.isNotBlank(userMap.getName())){
                    userName = userMap.getName();
                }
                trt.setUserName(userName);
                trt.setCompanyId(companyId);
                trt.setTaskInstanceId(taskInstanceId);
                signinRecordListRs.add(trt);

                if (userMap == null ||userMap.getUserDepartments() == null || userMap.getUserDepartments().size() == 0) {
                    continue;
                }
                List<Integer> deptList = userMap.getUserDepartments().stream().map(UserDepartment -> Integer.valueOf(UserDepartment.getDeptId())).collect(Collectors.toList());
                //TODO 根据companyId + UserId + checkinTime + taskInstanceId 缓存查历史部门集合，deptList不为空重新赋值，为空按用户当前的部门插入
                String fetchKey = OaMapUtils.fetchKey(trt.getCompanyId(), trt.getUserId(), String.valueOf(trt.getCheckinTime()), trt.getTaskInstanceId());
                String deptIds = (String) redisUtil.get(fetchKey);
                if(StringUtils.isNotBlank(deptIds)){
                     deptList = Arrays.asList(deptIds .split(",")).stream().map(s -> (Integer.valueOf(s.trim()))).collect(Collectors.toList());
                    //TODO 根据companyId + UserId + checkinTime + taskInstanceId 缓存删除
                     redisUtil.del(fetchKey);
                }
                //插入签到部门多对多关系表
                addSigninDepartmentData(deptList,trt.getId());
            }

            signinRecordMapper.batchSave(signinRecordListRs);

            signinRecordListRs.clear();

            TaskMessage.addMessage(taskInstanceId,"签到抓取数据导入，第"+group+"组导入完成");
        }
        int sum = list.size() - userNoFind;
        TaskMessage.addMessage(taskInstanceId,"签到抓取数据导入完成,有效数据"+sum+"条，其中有"+userNoFind+"条数据发现用户不存在，没有部门关系数据");
        return true;
    }


    /**
     * 插入签到部门多对多关系表
     * @param deptList 部门集合
     * @param id 签到记录id
     * @return boolean
     * @throws ApiException
     */
    public  void addSigninDepartmentData( List<Integer> deptList , String id){
        if(deptList == null|| deptList.size() == 0) {
            return;
        }
        int isSorN = 0;
        for (Integer deptId : deptList){
                //2.遍历部门插入数据
                SigninDepartment signinDepartment = new SigninDepartment();
                signinDepartment.setDeleted(0);
                signinDepartment.setCreateTime(System.currentTimeMillis());
                signinDepartment.setDeptId(deptId);
                signinDepartment.setSigninInstanceId(id);
                signinDepartment.setId(UUIDS.getID());
                int isNot = signinDepartmentMapper.insert(signinDepartment);
                if(isNot <= 0){
                    isSorN++;
                }
        }
            if(isSorN>0){
                log.warn("钉钉获取用户信息异常，查表获取用户信息");
                throw new OaSigninlException(EnumSigninError.FAILED_DATA_INSERT_DEPARTMENT_RELATIONAL);
            }

    }

    /**
     *
     *
     * 公司签到记录列表
     * @param signinRecordDTO
     * @return
     */
    @Override
    public List<DataMap> signinRecordListByCompany(SigninRecordDTO signinRecordDTO){
        if(StringUtils.isBlank(signinRecordDTO.getCompanyId())){
            throw new OaSigninlException(EnumSigninError.PARAMETER_IS_NULL);
        }
        int pages = signinRecordDTO.getPage();
        int capacity = signinRecordDTO.getCapacity();
        List<Department>  result = departmentService.listDepartment(signinRecordDTO.getCompanyId());
        if(StringUtils.isNotBlank(signinRecordDTO.getDeptId())){
            List<Long> longList = departmentService.listSubDepartment(signinRecordDTO.getCompanyId(),signinRecordDTO.getDeptId());
            signinRecordDTO.setDeptList(longList);
        }
        //每页的大小为capacity，查询第page页的结果
        PageHelper.startPage(pages, capacity);
        List<DataMap> signinRecord = signinRecordMapper.signinRecordList(signinRecordDTO);
        if(signinRecord!=null&&signinRecord.size() > 0){
            for (DataMap dataMap:signinRecord) {
                String deptId = dataMap.get("dept_ids").toString();
                String deptName = getDeptName(result,deptId);
                dataMap.put("dept_name", deptName);
                if(!OaMapUtils.mapIsAnyBlank(dataMap,"status")){
                    String  status = dataMap.get("status").toString();
                    dataMap.put("status",SigninTypeEnum.getMsgByValue(status) );
                }
            }
        }
        return signinRecord;
    }
    /**
     *  从集合中获取部门名称
     * @param list 部门集合
     * @param deptIds 部门ids
     * @return String 部门名称
     */
    @Override
    public  String getDeptName(List<Department> list, String deptIds) {
        List<Department> var5 = list;
        int var4 = list.size();
        List<String> idsList;
        StringBuilder  stringBuilderDeptName = new StringBuilder ();
        if(StringUtils.isNotBlank(deptIds)){
            idsList= Arrays.asList(deptIds .split(",")).stream().map(s -> (s.trim())).collect(Collectors.toList());
            for (String deptId :idsList){
                for(int var3 = 0; var3 < var4; ++var3) {
                    Department map = var5.get(var3);
                    if(map.getId()!=null){
                        String code = map.getId();
                        if (code.equals(deptId)) {
                            if(stringBuilderDeptName.length()==0){
                                stringBuilderDeptName.append(map.getName());
                            }else{
                                stringBuilderDeptName.append(","+map.getName());
                            }
                        }
                    }
                }
            }
            return stringBuilderDeptName.toString();
        }
        return null;
    }

    /**
     *
     *
     * 公司签到记录详情
     * @param id 签到记录id
     * @return SigninRecord
     */
    @Override
    public SigninRecord signinRuleDetail(String id){
        if(StringUtils.isAllBlank(id)){
            throw new OaSigninlException(EnumSigninError.PARAMETER_IS_NULL_ID);
        }
        SigninRecord singRule = signinRecordMapper.selectById(id);
        if (singRule!=null){
            if(singRule.getStatus()!=null){
                singRule.setStatus(SigninTypeEnum.getMsgByValue(singRule.getStatus()));
            }
            return  singRule;
        }
        return null;
    }


    @Override
    public SigninRecord signinDetailByAttendanceId(long attendanceId) {
        QueryWrapper<SigninRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("attendance_id",attendanceId);
        wrapper.eq("deleted",0);
        SigninRecord signinRecord = signinRecordMapper.selectOne(wrapper);
        return signinRecord;
    }
    /**
     * 根据实例id查询数据
     * @param instanceId
     * @return
     */
    private List<SigninConfirm> signinConfirmListByInstanceId(String instanceId) {
        QueryWrapper<SigninConfirm> signinConfirmQueryWrapper = new QueryWrapper<>();
        signinConfirmQueryWrapper.lambda().eq(SigninConfirm::getTaskInstanceId,instanceId);
        signinConfirmQueryWrapper.lambda().eq(SigninConfirm::getDeleted,0);
        List<SigninConfirm> signinConfirms = signinConfirmMapper.selectList(signinConfirmQueryWrapper);
        return signinConfirms;
    }

    /**
     * 删除数据
     * @param signinConfirmList
     */
    private void signinConfirmDataDelete(List<SigninConfirm> signinConfirmList) {
        List<String> ids = new ArrayList<>();
        //删除记录
        signinConfirmList.stream().forEach(SigninConfirm -> {
            SigninConfirm.setDeleted(1);
            ids.add(SigninConfirm.getId());
            signinConfirmMapper.updateById(SigninConfirm);
        });
    }

    /**
     * 发送外出签到确认任务
     * 2 每天8点，读取前一天所有的签到数据
     * 2.1 根据签到数据的user_id和日期yyyy-MM-dd，保存一条‘外出签到确认任务’：用户id，用户姓名，部门id，主管id（部门id对应的主管，如果有多个主管，取第一个），审批日期 yyyy-MM-dd，待办id（空），状态（未完成）
     * 2.2 发一条link类型的通知（标题：员工张某2019-07-29签到确认）url参数里包括外出签到确认任务的id
     * 2.4 修改'签到记录'表的签到确认任务id为当前任务的id
     * @param companyId 公司id
     * @return boolean
     */
    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class, OaException.class})
    public boolean sendOutingSignin (String taskInstanceId,String companyId) {
        TaskInstance taskInstance = iTaskInstanceService.getById(taskInstanceId);
        if (taskInstance == null) {
            return false;
        }
        //如果根据实例id查到值,根据实例id删除之前的数据
        List<SigninConfirm> signinConfirmList = signinConfirmListByInstanceId(taskInstanceId);
        if(CollectionUtils.isNotEmpty(signinConfirmList)){
            //删除签到数据
            signinConfirmDataDelete(signinConfirmList);
        }

        TaskInstanceTime taskInstanceTime = taskInstanceService.resetStartAndEndTime(taskInstance);
        long start = taskInstanceTime.getStartTime();
        long end = taskInstanceTime.getEndTime();
        //时间跨度如果超过1天，需要拆分为多段
        List<Pair> intervalTimes = LocalDateTimeUtils.getIntervalTimes(start, end, 1);
        for (Pair intervalTime : intervalTimes) {
            long intervalTimeStart = (long) intervalTime.getKey();
            long intervalTimeEnd = (long) intervalTime.getValue();
            //导入日报记录数据
            sendOutingSigninTask(taskInstanceId,companyId,  intervalTimeStart, intervalTimeEnd );
        }
        return true;
    }

    /**
     * 发送外出签到确认任务
     * 2 每天8点，读取前一天所有的签到数据
     * 2.1 根据签到数据的user_id和日期yyyy-MM-dd，保存一条‘外出签到确认任务’：用户id，用户姓名，部门id，主管id（部门id对应的主管，如果有多个主管，取第一个），审批日期 yyyy-MM-dd，待办id（空），状态（未完成）
     * 2.2 发一条link类型的通知（标题：员工张某2019-07-29签到确认）url参数里包括外出签到确认任务的id
     * 2.4 修改'签到记录'表的签到确认任务id为当前任务的id
     * @param companyId 公司id
     * @return boolean
     */
    @Override
    public boolean sendOutingSigninTask (String taskInstanceId,String companyId ,long intervalTimeStart ,long intervalTimeEnd) {

        //每天8点，读取前一天所有的签到数据
        QueryWrapper<SigninRecord> signinRecordQueryWrapper = new QueryWrapper<>();
        signinRecordQueryWrapper.lambda().eq(SigninRecord::getDeleted, 0);
        signinRecordQueryWrapper.lambda().eq(SigninRecord::getCompanyId, companyId);
        signinRecordQueryWrapper.lambda().ge(SigninRecord::getCheckinTime,intervalTimeStart);
        signinRecordQueryWrapper.lambda().le(SigninRecord::getCheckinTime,intervalTimeEnd);
        signinRecordQueryWrapper.lambda().isNull(SigninRecord::getSigninConfirmId);
        List<SigninRecord> list = signinRecordMapper.selectList(signinRecordQueryWrapper);

        if(list == null || list.size() == 0){
            TaskMessage.addMessage(taskInstanceId,"发送外出签到确认任务，查询没有符合时间："+LocalDateTimeUtils.formatDateTime(intervalTimeStart)+"--"+LocalDateTimeUtils.formatDateTime(intervalTimeEnd)+"之间的签到，任务结束");
            return true;
        }
        TaskMessage.addMessage(taskInstanceId,"发送外出签到确认任务，查询到符合时间："+LocalDateTimeUtils.formatDateTime(intervalTimeStart)+"--"+LocalDateTimeUtils.formatDateTime(intervalTimeEnd)+"的签到共"+list.size()+"条");
        //根据签到记录中的user_id+日期yyyy-MM-dd,进行分组
        Map<String, List<SigninRecord>> stringListMap = list.stream() .collect(Collectors.groupingBy(d -> d.getUserId()+"&"+ LocalDateTimeUtils.formatDateTimeByYmd(d.getCheckinTime()) ));
        TaskMessage.addMessage(taskInstanceId,"根据签到数据中的user_id+日期yyyy-MM-dd,进行分组:"+stringListMap.size()+"条");

        for (String userIdAndTime : stringListMap.keySet()) {
            String userId = userIdAndTime.substring(0, userIdAndTime.indexOf("&"));
            String times = userIdAndTime.substring(userId.length()+1);

            List<SigninRecord> signinRecordList = stringListMap.get(userIdAndTime);
            TaskMessage.addMessage(taskInstanceId,"生成外出签到确认任务,用户:"+userId+",时间："+times+",关联签到"+signinRecordList.size()+"条");

            if(signinRecordList == null || signinRecordList.size() == 0){
                continue;
            }
            //根据外出申请单主键id，获取到任意一条外出申请实例数据
            SigninRecord signinRecord  = signinRecordList.get(0);
            if(signinRecord!=null){
                String managerUserId = null;
                Integer deptId = null;
                //主管id（部门id对应的主管，如果有多个主管，取第一个）
                UserDTO userDTO = iUserService.getUserByUserId(userId,companyId);

                if(userDTO == null || userDTO.getUserDepartments() == null){
                    continue;
                }
                List<String> deptList = userDTO.getUserDepartments().stream().map(UserDepartment -> String.valueOf(UserDepartment.getDeptId())).collect(Collectors.toList());
                if(deptList != null && deptList.size() > 0){
                    deptId = Integer.valueOf(deptList.get(0));
                    managerUserId = getManagerUserId(deptId,companyId);
                    if(StringUtils.isBlank(managerUserId)){
                        log.error("新增外出签到确认任务,主管为空");
                    }
                }
                //根据用户一组签到实例数据，保存一条‘外出签到确认任务
                SigninConfirm tSigninConfirm = new SigninConfirm();
                tSigninConfirm.setCreateTime(System.currentTimeMillis());
                tSigninConfirm.setDay(times);
                tSigninConfirm.setDeleted(0);
                tSigninConfirm.setDeptId(deptId);
                tSigninConfirm.setId(UUIDS.getID());
                tSigninConfirm.setUserId(userId);
                tSigninConfirm.setUserName(userDTO.getName());
                tSigninConfirm.setStatus(SigninTypeEnum.UNCOMPLETED.getKey());
                tSigninConfirm.setConfirmUserId(managerUserId);
                tSigninConfirm.setTaskInstanceId(taskInstanceId);
                tSigninConfirm.setCompanyId(companyId);
                int isNot = signinConfirmMapper.insert(tSigninConfirm);
                if(isNot == 0){
                    throw new OaSigninlException(EnumSigninError.THE_NEW_DATA_IS_ABNORMAL);
                }
                TaskMessage.addMessage(taskInstanceId,"新增外出签到确认任务成功"+tSigninConfirm.getId());
                int whetherSucceed = 0;
                for(SigninRecord signinRecords : signinRecordList){
                    signinRecords.setSigninConfirmId(tSigninConfirm.getId());
                    int whether = signinRecordMapper.updateById(signinRecords);
                    if(whether<=0){
                        whetherSucceed++;
                    }
                }
                if(whetherSucceed>0){
                    throw new OaSigninlException(EnumSigninError.MODIFY_SIGNIN_CONFIRM_ID_TASK_TABLE_TO_SHOW_AN_EXCEPTION);
                }
                TaskMessage.addMessage(taskInstanceId,"修改'签到记录'表的签到确认任务id为当前“外出签到确认任务”的id:"+tSigninConfirm.getId()+"成功");
            }
        }
        return true;
    }

    /**
     * 根据部门获取部门的一个主管用户id
     * @param deptId
     * @param companyId
     * @return
     */
    private String getManagerUserId(long deptId , String companyId){
        //主管id（部门id对应的主管，如果有多个主管，取第一个）
        //测试环境发送测试人员
        if(StringUtils.isNotBlank(signinConfirmTestDeptid)){
            String list= managerUserList(signinConfirmTestDeptid,companyId,deptId);
            if(list == null ){
                return null;
            }
            return list;
        }else{
            OapiDepartmentGetResponse deptInfo =  dingDingReportService.deptInfoById(String.valueOf(deptId),companyId);
            String managerUserIdList = deptInfo.getDeptManagerUseridList();
            if(StringUtils.isNotBlank(managerUserIdList)){
                return   managerUserIdList.split("\\|")[0];
            }
        }
        return null;
    }
    /**
     * 获取配置文件部门用户集合
     * @param signinConfirmTestDeptid
     * @param companyId
     * @return
     */
    private String managerUserList (String signinConfirmTestDeptid ,String companyId,long deptId){


        Object[] array= JSONArray.fromObject(signinConfirmTestDeptid).toArray();
        String userList = null;
        for(int i=0; i<array.length; i++) {
            JSONObject jsonObj = (JSONObject)JSONObject.toJSON(array[i]);
            if(jsonObj.get("companyId").toString().equals(companyId)){
                List<Integer> deptid = (List) jsonObj.get("depts");
                for (Integer dept : deptid){

                    if(dept == deptId){

                        OapiDepartmentGetResponse deptInfo =  dingDingReportService.deptInfoById(String.valueOf(dept),companyId);
                        String managerUserIdList = deptInfo.getDeptManagerUseridList();
                        if(StringUtils.isNotBlank(managerUserIdList)){
                            userList = managerUserIdList.split("\\|")[0];
                        }
                    }
                }
            }
        }
        return userList;
    }

    /**
     *钉钉消息撤回通知接口
     * @param taskInstanceId
     * @param companyId
     */
    @Override
    public void recallNotify(String taskInstanceId ,String companyId){
        String key = taskInstanceId + "recallNotify";
        String agentIdKey = taskInstanceId + "recallNotifyCompanyAgentId";
        String ids = (String) redisUtil.get(key);
        String agentId = (String) redisUtil.get(agentIdKey);
        if(StringUtils.isBlank(ids) || StringUtils.isBlank(agentId)){
            return;
        }
        List<Long> list= Arrays.asList(ids.split(",")).stream().map(s -> (Long.valueOf(s.trim()))).collect(Collectors.toList());
        for(Long msgTaskId : list){
            dingDingNotifyService.recallNotify(Long.valueOf(agentId), msgTaskId, companyId);
        }
        redisUtil.del(key);
        redisUtil.del(agentIdKey);
    }
    /**
     * 发一条link类型的通知和发一条待办，根据返回的待办id更新‘外出签到确认任务’的待办id
     * @param companyId 公司id
     * @param managerUserId 接收者id
     * @param tSigninConfirm 外出签到确认任务表实体类
     * @param times 时间
     * @return
     * @throws ApiException
     */
    @Override
    public boolean sendToNotifAndBacklog(String companyId, String managerUserId, SigninConfirm tSigninConfirm, String times ,String taskInstanceId){
        //2.2 发一条link类型的通知（标题：员工张某2019-07-29签到确认）url参数里包括外出签到确认任务的id
        String title = "员工"+tSigninConfirm.getUserName()+times+"签到确认";
        String url = MessageFormat.format(signinConfirmUrl,tSigninConfirm.getId());
        OANotifyDTO notify = new OANotifyDTO(companyId, managerUserId,url , title);
        notify.addParam("时间",times);

        Long whetherSuccess = dingDingNotifyService.sendOAMessage(notify);
        if(whetherSuccess != null){
            StringBuffer stringBuffer = new StringBuffer();
            String key = taskInstanceId + "recallNotify";

            String taskIds = (String) redisUtil.get(key);
            if(StringUtils.isNotBlank(taskIds)){
                stringBuffer.append(taskIds+","+String.valueOf(whetherSuccess));
            }else {
                stringBuffer.append(String.valueOf(whetherSuccess));
            }
            redisUtil.set(key , stringBuffer.toString());
        }
        return true;
        //2.3 发一条待办
       /* SendBacklogDTO sendBacklogDTO = new SendBacklogDTO();
        List<OapiWorkrecordAddRequest.FormItemVo> listFormItemVo = new ArrayList<>();
        OapiWorkrecordAddRequest.FormItemVo obj1 = new OapiWorkrecordAddRequest.FormItemVo();
        obj1.setTitle("姓名");
        obj1.setContent(tSigninConfirm.getUserName());
        OapiWorkrecordAddRequest.FormItemVo obj3 = new OapiWorkrecordAddRequest.FormItemVo();
        obj3.setTitle("时间");
        obj3.setContent(times);
        listFormItemVo.add(obj1);
        listFormItemVo.add(obj3);
        sendBacklogDTO.setCompanyId(companyId);
        sendBacklogDTO.setFormItemList(listFormItemVo);
        sendBacklogDTO.setManagerId(managerUserId);
        sendBacklogDTO.setTitle(tSigninConfirm.getUserName()+"签到确认");
        sendBacklogDTO.setUrl(MessageFormat.format(signinConfirmUrl,tSigninConfirm.getId()));
        String ddWorkrecordId = dingDingBacklogService.sendToBacklog(sendBacklogDTO);
        tSigninConfirm.setDdWorkrecordId(ddWorkrecordId);
        //根据返回的待办id更新‘外出签到确认任务’的待办id
        int whetherSuccessV2 = signinConfirmMapper.updateById(tSigninConfirm);
        if(whetherSuccessV2>0){
            return true;
        }*/
    }





}
