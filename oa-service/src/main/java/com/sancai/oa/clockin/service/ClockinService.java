package com.sancai.oa.clockin.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sancai.oa.clockin.entity.ClockinPoint;
import com.sancai.oa.clockin.entity.ClockinRecord;
import com.sancai.oa.user.entity.UserDTO;
import com.taobao.api.ApiException;

import java.util.List;
import java.util.Map;

/**
 * @Author chenm
 * @create 2019/8/1 14:03
 */
public interface ClockinService extends IService<ClockinRecord> {


    /**
     *  取一个人一个月的考勤数据
     * @param companyId
     * @param userId
     * @param month yyyy-MM
     * @return
     */
    public ClockinRecord getUserMonthClockinRecord(String companyId,String userId,String month);

    /**
     * 抓取公司下的考勤记录
     * @param taskInstanceId
     * @param companyId
     * @throws ApiException
     */
    public void graspClockinRecord(String taskInstanceId, String companyId) throws Exception;

    /**
     * 抓取公司下的考勤记录
     * @param companyId
     * @param start
     * @param end
     * @throws ApiException
     */
    public void graspClockinRecord(String taskInstanceId, String companyId, List<List<UserDTO>> userIdBatch, long start, long end, boolean isFinish) throws Exception;


    /**
     * 抓取公司下的一批用户id1的考勤记录
     * @param taskInstanceId
     * @param companyId
     * @param start
     * @param end
     * @param userIds
     */
    public Map<String, Map<Long,List<ClockinPoint>>> graspClockin(String taskInstanceId, String companyId, long start, long end,Map<String,UserDTO> users);
    /**
     * 取一个人一段时间内的缺卡的打卡点
     * @param companyId
     * @param userId
     * @param start
     * @param end
     * @return
     */
    public List<JSONObject> getNotSignedCheckPoints(String companyId,String userId, long start, long end);
}
