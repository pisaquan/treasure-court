package com.sancai.oasystem.service;


import com.sancai.oasystem.bean.enums.ExamineTypeEnum;

/**
 * 抓取审批数据接口
 * @author pisaquan
 * @since 2019-07-18
 */
public interface IExamineDataService {


    /**
     * 处理审批数据
     * @param group
     * @return
     */
    void dealExamineData(String group,String accessToken,String companyId,String processCode);

}
