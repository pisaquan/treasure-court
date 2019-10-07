package com.sancai.oa.signin.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.dingtalk.api.response.OapiCheckinRecordResponse;
import com.sancai.oa.core.exception.OaException;
import com.sancai.oa.department.entity.Department;
import com.sancai.oa.report.entity.modify.DataMap;
import com.sancai.oa.signin.entity.SigninRecord;
import com.sancai.oa.signin.entity.SigninRecordDTO;
import com.sancai.oa.signinconfirm.entity.SigninConfirm;
import com.taobao.api.ApiException;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;

/**
 * <p>
 * 签到记录 接口类
 * </p>
 *
 * @author fans
 * @since 2019-07-19
 */
public interface ISigninRecordService extends IService<SigninRecord> {



    /**
     * 获取子公司每天所有用户签到记录(根据部门获取签到记录)
     * @return
     */
    LinkedHashSet<OapiCheckinRecordResponse.Data> checkinListByDeptList(String companyId,long intervalTimeStart , long intervalTimeEnd ,String taskInstanceId)throws  ApiException;

    /**
     * 导入签到数据
     *
     * @return
     */
     boolean importEveryDayCheckinData(String companyId,long intervalTimeStart , long intervalTimeEnd,String taskInstanceId)throws  ApiException;

    /**
     * 导入签到数据
     */
    boolean importEveryDayCheckinData(String taskInstanceId,String companyId) throws ApiException;


    /**
     * 公司签到记录列表
     *
     * @param signinRecordDTO
     * @return
     */

    List<DataMap> signinRecordListByCompany(SigninRecordDTO signinRecordDTO) throws ApiException;


    /**
     *
     *
     * 公司签到记录详情
     * @param id
     * @return
     */

    SigninRecord signinRuleDetail(String id);

    /**
     *
     *
     * 根据打卡点取签到记录详情
     * @param attendanceId
     * @return
     */
    SigninRecord signinDetailByAttendanceId(long attendanceId);

    /**
     *  从集合中获取部门名称
     * @param list 部门集合
     * @param deptId 部门id
     * @return boolean
     */
    String getDeptName(List<Department> list, String deptId);


    /**
     * 钉钉消息撤回通知接口
     *
     * @param taskInstanceId
     * @param companyId
     */
    void recallNotify(String taskInstanceId, String companyId);

    /**
     * 发一条link类型的通知和发一条待办，根据返回的待办id更新‘外出签到确认任务’的待办id
     *
     * @param companyId      公司id
     * @param managerUserId  接收者id
     * @param tSigninConfirm 外出签到确认任务表实体类
     * @param times          时间
     * @return
     * @throws ApiException
     */
    boolean sendToNotifAndBacklog(String companyId, String managerUserId, SigninConfirm tSigninConfirm, String times, String taskInstanceId);

    /**
     * 根据companyId + UserId + checkinTime + taskInstanceId 把记录对应的部门集合存入缓存
     *
     * @param recordLists 记录集合
     */
    void recordOriginDeptIdsSaveRedis(List<DataMap> recordLists);

    /**
     * 发送外出签到确认任务
     *
     * @param companyId 公司id
     * @return boolean
     */
    boolean sendOutingSigninTask(String taskInstanceId, String companyId,long intervalTimeStart ,long intervalTimeEnd);

    /**
     * 发送外出签到确认任务
     * 2 每天8点，读取前一天所有的签到数据
     * 2.1 根据签到数据的user_id和日期yyyy-MM-dd，保存一条‘外出签到确认任务’：用户id，用户姓名，部门id，主管id（部门id对应的主管，如果有多个主管，取第一个），审批日期 yyyy-MM-dd，待办id（空），状态（未完成）
     * 2.2 发一条link类型的通知（标题：员工张某2019-07-29签到确认）url参数里包括外出签到确认任务的id
     * 2.4 修改'签到记录'表的签到确认任务id为当前任务的id
     *
     * @param companyId 公司id
     * @return boolean
     */
    boolean sendOutingSignin(String taskInstanceId, String companyId);
}
