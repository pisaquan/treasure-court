package com.sancai.oa.examine.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.sancai.oa.examine.entity.ExamineLeave;
import com.sancai.oa.examine.entity.ExamineLeaveDTO;
import com.sancai.oa.examine.entity.ExamineLeaveDetailDTO;
import com.sancai.oa.examine.entity.RequestEntity;
import com.taobao.api.ApiException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.util.List;

/**
 * <p>
 * 请假 服务类
 * </p>
 *
 * @author quanleilei
 * @since 2019-07-24
 */
public interface IExamineLeaveService extends IService<ExamineLeave> {

    /**
     * 处理审批数据
     *
     * @param taskInstanceId
     * @param companyId
     * @param processCode
     * @param intervalTimeStart
     * @param intervalTimeEnd
     * @throws ApiException
     */
    void dealExamineData(String taskInstanceId, String companyId, String processCode, long intervalTimeStart, long intervalTimeEnd);

    /**
     * 更新审批数据
     * 必须晚上更新
     */
    void updateExamineData(String taskInstanceId);

    /**
     * 请假列表查询
     *
     * @param requestEntity 封装请求体的实体类
     * @return 返回集合
     */

    List<ExamineLeaveDTO> getExamineLeaveList(RequestEntity requestEntity);

    /**
     * 根据请假记录id获取详情记录
     *
     * @param id 请假记录id
     * @return 返回一条请假记录详情
     */
    ExamineLeaveDetailDTO getExamineLeaveDetail(String id);

    /**
     * 根据任务实例id查询请假数据
     * @param instanceId
     * @return
     */
    List<ExamineLeave> examineLeaveListByInstanceId(String instanceId);

    /**
     * 删除请假审批数据
     * @param examineLeaveList
     */
    void examineLeaveDataDelete(List<ExamineLeave> examineLeaveList);

    /**
     * 抓取请假数据
     * @param companyId
     * @param taskInstanceId
     */
    void pullLeaveExamineData(String companyId,String taskInstanceId);

    /**
     * 员工上传病例证明信息
     * @param multipartFile
     * @param leaveId
     * @param userId
     */
    void uploadsMedicalCertificate(List<MultipartFile> multipartFile, String leaveId, String userId)  throws Exception ;

    /**
     * 人事审核病例证明
     *
     * @param leaveId
     */
    void checkMedicalCertificate( String leaveId , String status) ;


    /**
     * 根据请假记录id获取详情记录病例图片
     *
     * @param id 请假记录id
     * @return 返回一条请假记录详情
     */
    ExamineLeaveDetailDTO getExamineLeaveDetailImg(String id);


}
