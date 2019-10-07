package com.sancai.oa.company.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sancai.oa.company.entity.Company;
import com.taobao.api.ApiException;

import java.util.List;

/**
 * <p>
 * 分公司 服务类
 * </p>
 *
 * @author quanleilei
 * @since 2019-07-26
 */
public interface ICompanyService extends IService<Company> {
    /**
     * 分公司列表
     *
     * @return
     */

    List<Company> companyList();

    /**
     * 分公司详情
     *
     * @return
     */
    Company companyDetail(String id);

    /**
     * 新增分公司
     *
     * @param company
     * @return
     */

    boolean companyAdd(Company company) throws Exception;

    /**
     * 修改分公司
     *
     * @param company
     * @return
     */
    boolean companyModify(Company company) throws ApiException;


    /**
     * 根据任务id取钉钉企业id(cropId)
     *
     * @param type 类型，如：SIGNIN_CONFIRM、ATTENDANCE_RECORD
     * @param id   任务id
     * @return 返回企业corpId
     */
    String getCorpIdById(String type, String id);
}
