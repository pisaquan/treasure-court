package com.sancai.oa.examine.service;

import com.sancai.oa.examine.entity.Examine;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sancai.oa.examine.entity.ExamineDTO;
import com.sancai.oa.examine.entity.ExamineTimeDTO;
import com.sancai.oa.examine.entity.ExamineTypeDTO;

import java.util.List;

/**
 * <p>
 * 审批表单 服务类
 * </p>
 *
 * @author quanleilei
 * @since 2019-07-24
 */
public interface IExamineService extends IService<Examine> {

    /**
     * 新增审批模板
     * @param examineDTO
     */
    boolean examineCreate(ExamineDTO examineDTO);

    /**
     * 新增判断审批模板的名称和组是否重名
     * @param examineDTO
     * @return
     */
    boolean repetitionExamineAdd(ExamineDTO examineDTO);

    /**
     * 修改判断审批模板的名称和组是否重名
     * @param examineDTO
     * @return
     */
    boolean repetitionExamineModify(ExamineDTO examineDTO);

    /**
     * 修改审批模板
     * @param examineDTO
     */
    boolean examineModify(ExamineDTO examineDTO);

    /**
     * 删除审批模板
     * @param examineDTO
     */
    boolean examineDelete(ExamineDTO examineDTO);


    /**
     * 根据公司id获取审批模板列表
     * @param companyId
     * @return
     */
    List<ExamineDTO> getExamineList(String companyId);

    /**
     * 根据id取审批模板详情
     * @param id
     * @return
     */
    ExamineDTO getExamineDetailById(String id);

    /**
     * 根据模板组和公司取审批模板详情
     * @param group
     * @param companyId
     * @return
     */
    ExamineDTO getExamineDetail(String group,String companyId);

    /**
     * 查询，并存储用户考勤组
     *
     * @param companyId
     * @param userId
     * @return
     */
    List<ExamineTimeDTO> examineTime(String companyId, String userId);

    /**
     * 查询，并存储用户考勤组
     * @param companyId
     * @param userId
     */
    void saveUserAttendance(String companyId, String userId);

    /**
     * 查询，并存储公司下所有用户考勤组
     * @param companyId
     */
    void saveUserAttendanceByCompany(String companyId);
}
