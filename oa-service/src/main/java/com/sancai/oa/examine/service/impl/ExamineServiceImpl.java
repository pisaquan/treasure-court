package com.sancai.oa.examine.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.dingtalk.api.response.OapiAttendanceGetusergroupResponse;
import com.sancai.oa.clockin.enums.EnumDingDingAttendanceCheckType;
import com.sancai.oa.core.redis.RedisUtil;
import com.sancai.oa.dingding.user.DingDingUserService;
import com.sancai.oa.examine.entity.Examine;
import com.sancai.oa.examine.entity.ExamineDTO;
import com.sancai.oa.examine.entity.ExamineTimeDTO;
import com.sancai.oa.examine.mapper.ExamineMapper;
import com.sancai.oa.examine.service.IExamineService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sancai.oa.user.entity.UserDTO;
import com.sancai.oa.user.service.IUserService;
import com.sancai.oa.utils.LocalDateTimeUtils;
import com.sancai.oa.utils.UUIDS;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * <p>
 * 审批表单 服务实现类
 * </p>
 *
 * @author quanleilei
 * @since 2019-07-24
 */
@Slf4j
@Service
public class ExamineServiceImpl extends ServiceImpl<ExamineMapper, Examine> implements IExamineService {

    @Autowired
    private DingDingUserService dingDingUserService;
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    private IUserService userService;

    /**
     * 新增审批模板
     * @param examineDTO
     */
    @Override
    public boolean examineCreate(ExamineDTO examineDTO) {
        Examine examine = new Examine();
        examine.setId(UUIDS.getID());
        examine.setCode(examineDTO.getCode());
        examine.setCompanyId(examineDTO.getCompanyId());
        examine.setExamineGroup(examineDTO.getExamineGroup());
        examine.setName(examineDTO.getName());
        examine.setCreateTime(LocalDateTimeUtils.getMilliByTime(LocalDateTime.now()));
        examine.setDeleted(0);
        return save(examine);
    }

    @Override
    public boolean repetitionExamineAdd(ExamineDTO examineDTO) {
        QueryWrapper<Examine> examineQueryWrapper = new QueryWrapper<>();
        examineQueryWrapper.lambda().eq(Examine::getCompanyId,examineDTO.getCompanyId());
        examineQueryWrapper.lambda().and(i -> i.eq(Examine::getName,examineDTO.getName())
                .or().eq(Examine::getExamineGroup,examineDTO.getExamineGroup()));
        examineQueryWrapper.lambda().eq(Examine::getDeleted,0);
        int examineSize = count(examineQueryWrapper);
        return examineSize>0?true:false;
    }

    @Override
    public boolean repetitionExamineModify(ExamineDTO examineDTO) {
        Examine examine = getById(examineDTO.getId());
        String companyId = examine.getCompanyId();
        QueryWrapper<Examine> examineQueryWrapper = new QueryWrapper<>();
        examineQueryWrapper.lambda().eq(Examine::getCompanyId,companyId);
        examineQueryWrapper.lambda().and(i -> i.eq(Examine::getName,examineDTO.getName())
                .or().eq(Examine::getExamineGroup,examineDTO.getExamineGroup()));
        examineQueryWrapper.lambda().ne(Examine::getId,examineDTO.getId());
        examineQueryWrapper.lambda().eq(Examine::getDeleted,0);
        int examineSize = count(examineQueryWrapper);
        return examineSize>0?true:false;
    }

    /**
     * 修改审批模板
     * @param examineDTO
     * @return
     */
    @Override
    public boolean examineModify(ExamineDTO examineDTO) {
        Examine examine = new Examine();
        examine.setCode(examineDTO.getCode());
        examine.setName(examineDTO.getName());
        examine.setExamineGroup(examineDTO.getExamineGroup());
        examine.setModifyTime(LocalDateTimeUtils.getMilliByTime(LocalDateTime.now()));
        UpdateWrapper<Examine> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id",examineDTO.getId());
        boolean isSuccess = update(examine,updateWrapper);
        return isSuccess;
    }

    /**
     * 删除审批模板
     * @param examineDTO
     * @return
     */
    @Override
    public boolean examineDelete(ExamineDTO examineDTO) {
        Examine examine = new Examine();
        examine.setDeleted(1);
        UpdateWrapper<Examine> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id",examineDTO.getId());
        boolean isSuccess = update(examine,updateWrapper);
        return isSuccess;
    }

    /**
     * 根据公司id获取审批模板列表
     * @param companyId
     * @return
     */
    @Override
    public List<ExamineDTO> getExamineList(String companyId) {
        List<ExamineDTO> examineDTOList = new ArrayList<>();
        QueryWrapper<Examine> examineQueryWrapper = new QueryWrapper<>();
        examineQueryWrapper.lambda().eq(Examine::getCompanyId,companyId);
        examineQueryWrapper.lambda().eq(Examine::getDeleted,0);
        examineQueryWrapper.orderByDesc("create_time");
        List<Examine> examineList = list(examineQueryWrapper);
        examineList.stream().forEach(examine -> {
            ExamineDTO examineDTO = new ExamineDTO();
            examineDTO.setId(examine.getId());
            examineDTO.setCode(examine.getCode());
            examineDTO.setName(examine.getName());
            examineDTO.setExamineGroup(examine.getExamineGroup());
            examineDTO.setCreateTime(examine.getCreateTime());
            examineDTO.setDeleted(examine.getDeleted());
            examineDTO.setCompanyId(examine.getCompanyId());
            examineDTO.setModifyTime(examine.getModifyTime());
            examineDTOList.add(examineDTO);
        });
        return examineDTOList;
    }

    /**
     * 根据id取审批模板详情
     * @param id
     * @return
     */
    @Override
    public ExamineDTO getExamineDetailById(String id) {
        ExamineDTO examineDTO = new ExamineDTO();
        QueryWrapper<Examine> examineQueryWrapper = new QueryWrapper<>();
        examineQueryWrapper.lambda().eq(Examine::getId,id);
        examineQueryWrapper.lambda().eq(Examine::getDeleted,0);
        Examine examine = getOne(examineQueryWrapper,true);
        if(examine == null){
            return null;
        }
        examineDTO.setId(examine.getId());
        examineDTO.setCode(examine.getCode());
        examineDTO.setName(examine.getName());
        examineDTO.setExamineGroup(examine.getExamineGroup());
        examineDTO.setCreateTime(examine.getCreateTime());
        examineDTO.setDeleted(examine.getDeleted());
        examineDTO.setCompanyId(examine.getCompanyId());
        examineDTO.setModifyTime(examine.getModifyTime());
        return examineDTO;
    }

    /**
     * 根据模板组和公司id取审批模板详情
     * @param group
     * @param companyId
     * @return
     */
    @Override
    public ExamineDTO getExamineDetail(String group, String companyId) {
        ExamineDTO examineDTO = new ExamineDTO();
        QueryWrapper<Examine> examineQueryWrapper = new QueryWrapper<>();
        examineQueryWrapper.lambda().eq(Examine::getExamineGroup,group);
        examineQueryWrapper.lambda().eq(Examine::getCompanyId,companyId);
        examineQueryWrapper.lambda().eq(Examine::getDeleted,0);
        Examine examine = getOne(examineQueryWrapper,true);
        if(examine == null){
            return null;
        }
        examineDTO.setId(examine.getId());
        examineDTO.setCode(examine.getCode());
        examineDTO.setName(examine.getName());
        examineDTO.setExamineGroup(examine.getExamineGroup());
        examineDTO.setCreateTime(examine.getCreateTime());
        examineDTO.setDeleted(examine.getDeleted());
        examineDTO.setCompanyId(examine.getCompanyId());
        examineDTO.setModifyTime(examine.getModifyTime());
        return examineDTO;
    }

    /**
     * 查询，并存储用户考勤组
     * @param companyId
     * @param userId
     * @return
     */
    @Override
    public  List<ExamineTimeDTO> examineTime (String companyId ,String userId ){
        String key = companyId + "userAttendanceGroup";
        String dataMap = (String) redisUtil.get(key);
        Map<String, List<ExamineTimeDTO>> examineTimeMap = new ConcurrentHashMap<>();
        if(StringUtils.isNotBlank(dataMap)){
            examineTimeMap = JSONObject.parseObject(dataMap).entrySet().stream() .collect(Collectors.toMap(Map.Entry::getKey, entry -> JSONObject.parseArray(String.valueOf(entry.getValue()), ExamineTimeDTO.class)));
        }
        List<ExamineTimeDTO> examineTimeDTOList = new ArrayList<>();

        List<ExamineTimeDTO> timeDTOList =  examineTimeMap.get(userId);
        //缓存没有考勤组，获取考勤组返回并存储
        if(timeDTOList == null || timeDTOList.size() == 0){
            List<OapiAttendanceGetusergroupResponse.AtClassVo> atClassVoList = dingDingUserService.getUserGroup(companyId,userId);
            if(atClassVoList != null && atClassVoList.size() > 0){
                for(OapiAttendanceGetusergroupResponse.AtClassVo ado :atClassVoList){
                    List<OapiAttendanceGetusergroupResponse.AtSectionVo> dsd =  ado.getSections();
                    if(dsd != null && dsd.size() > 0){
                        for(OapiAttendanceGetusergroupResponse.AtSectionVo at : dsd){
                            List<OapiAttendanceGetusergroupResponse.AtTimeVo> atvo = at.getTimes();
                            if(atvo != null && atvo.size()>0){
                                for(OapiAttendanceGetusergroupResponse.AtTimeVo AtTimeVo : atvo){
                                    Date date = AtTimeVo.getCheckTime();
                                    String time = new SimpleDateFormat("HH:mm:ss").format(date);
                                    ExamineTimeDTO examineTimeDTO = new ExamineTimeDTO();
                                    examineTimeDTO.setCheckTimeString(time);
                                    examineTimeDTO.setCheckType(AtTimeVo.getCheckType());
                                    examineTimeDTOList.add(examineTimeDTO);
                                }
                            }
                            if(examineTimeDTOList != null && examineTimeDTOList.size() > 0){
                                examineTimeMap.put(userId, examineTimeDTOList);
                            }
                        }
                    }
                }
            }
            if(examineTimeDTOList != null && examineTimeDTOList.size() > 0){
                examineTimeMap.put(userId, examineTimeDTOList);
                JSONObject jo = (JSONObject) JSON.toJSON(examineTimeMap);
                String  stringMap = String.format(JSONObject.toJSONString(jo));
                redisUtil.set(key , stringMap, 24 * 60 * 60);
            } else {
                //获取不到考勤组的默认补4次时间
                ExamineTimeDTO examineTimeDTO = new ExamineTimeDTO();
                examineTimeDTO.setCheckTimeString("09:00:00");
                examineTimeDTO.setCheckType(EnumDingDingAttendanceCheckType.OnDuty.getKey());
                ExamineTimeDTO examineTime = new ExamineTimeDTO();
                examineTime.setCheckTimeString("18:00:00");
                examineTime.setCheckType(EnumDingDingAttendanceCheckType.OffDuty.getKey());
                examineTimeDTOList.add(examineTimeDTO);
                examineTimeDTOList.add(examineTime);
            }
        } else {
            //有考勤组返回
            examineTimeDTOList = timeDTOList;
        }
        //两次考勤的默认补中间两次
        if(examineTimeDTOList!=null && examineTimeDTOList.size() == 2){
            ExamineTimeDTO examineTimeDTO = new ExamineTimeDTO();
            examineTimeDTO.setCheckTimeString("12:00:00");
            examineTimeDTO.setCheckType(EnumDingDingAttendanceCheckType.OffDuty.getKey());
            ExamineTimeDTO examineTime = new ExamineTimeDTO();
            examineTime.setCheckTimeString("13:00:00");
            examineTime.setCheckType(EnumDingDingAttendanceCheckType.OnDuty.getKey());
            examineTimeDTOList.add(examineTimeDTO);
            examineTimeDTOList.add(examineTime);
        }
        //排序
        examineTimeDTOList.sort(Comparator.comparing(ExamineTimeDTO::getCheckTimeString));
        return  examineTimeDTOList;
    }
    /**
     * 查询，并存储用户考勤组
     * @param companyId
     * @param userId
     * @return
     */

    @Override
    public void saveUserAttendance (String companyId ,String userId ){
        String key = companyId + "userAttendanceGroup";
        String dataMap = (String) redisUtil.get(key);
        Map<String, List<ExamineTimeDTO>> examineTimeMap = new ConcurrentHashMap<>();
        if(StringUtils.isNotBlank(dataMap)){
            examineTimeMap = JSONObject.parseObject(dataMap).entrySet().stream() .collect(Collectors.toMap(Map.Entry::getKey, entry -> JSONObject.parseArray(String.valueOf(entry.getValue()), ExamineTimeDTO.class)));
        }

        List<ExamineTimeDTO> examineTimeDTOList = new ArrayList<>();
        List<OapiAttendanceGetusergroupResponse.AtClassVo> atClassVoList = dingDingUserService.getUserGroup(companyId,userId);
            if(atClassVoList != null && atClassVoList.size() > 0){
                for(OapiAttendanceGetusergroupResponse.AtClassVo ado :atClassVoList){
                    List<OapiAttendanceGetusergroupResponse.AtSectionVo> dsd =  ado.getSections();
                    if(dsd != null && dsd.size() > 0){
                        for(OapiAttendanceGetusergroupResponse.AtSectionVo at : dsd){
                            List<OapiAttendanceGetusergroupResponse.AtTimeVo> atvo = at.getTimes();
                            if(atvo != null && atvo.size()>0){
                                for(OapiAttendanceGetusergroupResponse.AtTimeVo AtTimeVo : atvo){
                                    Date date = AtTimeVo.getCheckTime();
                                    String time = new SimpleDateFormat("HH:mm:ss").format(date);
                                    ExamineTimeDTO examineTimeDTO = new ExamineTimeDTO();
                                    examineTimeDTO.setCheckTimeString(time);
                                    examineTimeDTO.setCheckType(AtTimeVo.getCheckType());
                                    examineTimeDTOList.add(examineTimeDTO);
                                }
                            }
                            if(examineTimeDTOList != null && examineTimeDTOList.size() > 0){
                                examineTimeMap.put(userId, examineTimeDTOList);
                            }
                        }
                    }
                }
            }

            JSONObject jo = (JSONObject) JSON.toJSON(examineTimeMap);
            String  stringMap = String.format(JSONObject.toJSONString(jo));
            redisUtil.set(key , stringMap, 24 * 60 * 60);
    }

    /**
     * 查询，并存储用户考勤组
     * @param companyId
     * @return
     */

    @Override
    public void saveUserAttendanceByCompany(String companyId){
        String key = companyId + "userAttendanceGroup";
        String dataMap = (String) redisUtil.get(key);
        Map<String, List<ExamineTimeDTO>> examineTimeMap = new ConcurrentHashMap<>();
        if(StringUtils.isNotBlank(dataMap)){
            examineTimeMap = JSONObject.parseObject(dataMap).entrySet().stream() .collect(Collectors.toMap(Map.Entry::getKey, entry -> JSONObject.parseArray(String.valueOf(entry.getValue()), ExamineTimeDTO.class)));
        }

        // 获取公司下所有用户信息
        List<UserDTO> userDTOList = userService.listUserByCompany(companyId,0);
        if(null==userDTOList||userDTOList.size()==0){
            return;
        }
        for(UserDTO userDTO:userDTOList){
            //遍历集合查询每个用户考勤信息并缓存到redis中
            List<ExamineTimeDTO> examineTimeDTOList = new ArrayList<>();
            List<OapiAttendanceGetusergroupResponse.AtClassVo> atClassVoList = dingDingUserService.getUserGroup(companyId,userDTO.getUserId());
            if(atClassVoList == null || atClassVoList.size() == 0) {
                continue;
            }
            for(OapiAttendanceGetusergroupResponse.AtClassVo ado :atClassVoList){
                List<OapiAttendanceGetusergroupResponse.AtSectionVo> dsd =  ado.getSections();
                if(dsd == null && dsd.size() == 0){
                    continue;
                }
                for(OapiAttendanceGetusergroupResponse.AtSectionVo at : dsd){
                    List<OapiAttendanceGetusergroupResponse.AtTimeVo> atvo = at.getTimes();
                    if(atvo == null && atvo.size()==0){
                        continue;
                    }
                    for(OapiAttendanceGetusergroupResponse.AtTimeVo AtTimeVo : atvo){
                        Date date = AtTimeVo.getCheckTime();
                        String time = new SimpleDateFormat("HH:mm:ss").format(date);
                        ExamineTimeDTO examineTimeDTO = new ExamineTimeDTO();
                        examineTimeDTO.setCheckType(AtTimeVo.getCheckType());
                        examineTimeDTO.setCheckTimeString(time);
                        examineTimeDTOList.add(examineTimeDTO);
                    }
                    if(examineTimeDTOList != null && examineTimeDTOList.size() > 0){
                        examineTimeMap.put(userDTO.getUserId(), examineTimeDTOList);
                    }
                }
            }
        }

        JSONObject jo = (JSONObject) JSON.toJSON(examineTimeMap);
        String  stringMap = String.format(JSONObject.toJSONString(jo));
        redisUtil.set(key , stringMap, 24 * 60 * 60);
    }
}
