package com.sancai.oa.company.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sancai.oa.company.entity.Company;
import com.sancai.oa.company.exception.EnumCompanyError;
import com.sancai.oa.company.exception.OaCompanylException;
import com.sancai.oa.company.mapper.CompanyMapper;
import com.sancai.oa.company.service.ICompanyService;
import com.sancai.oa.dingding.report.DingDingReportService;
import com.sancai.oa.quartz.init.QuartzInit;
import com.sancai.oa.utils.UUIDS;
import com.taobao.api.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 分公司 服务实现类
 * </p>
 *
 * @author quanleilei
 * @since 2019-07-26
 */
@Service
@Slf4j
public class CompanyServiceImpl extends ServiceImpl<CompanyMapper, Company> implements ICompanyService {


    @Autowired
    CompanyMapper companyMapper;
    @Autowired
    DingDingReportService publicMethods;
    @Autowired
    QuartzInit quartzInit;
    /**
     * 分公司列表
     *
     * @return
     */
    @Override
    public List<Company> companyList() {
        QueryWrapper<Company> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Company::getDeleted, 0);
        queryWrapper.lambda().orderByDesc(Company::getCreateTime);
        List<Company> company = companyMapper.selectList(queryWrapper);
        if (company != null && company.size() > 0) {
            return company;
        }
        return null;
    }

    /**
     * 分公司详情
     *
     * @return
     */
    @Override
    public Company companyDetail(String id) {
        QueryWrapper<Company> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Company::getDeleted, 0);
        queryWrapper.lambda().eq(Company::getId, id);
        Company company = companyMapper.selectOne(queryWrapper);
        if (company != null) {
            return company;
        }
        return null;
    }

    /**
     * 新增分公司
     *
     * @param company
     * @return
     */
    @Override
    public boolean companyAdd(Company company) throws Exception {
        if (StringUtils.isAnyBlank(company.getName(), company.getAppKey(), company.getAgentId(), company.getAppSecret(), company.getCorpId())) {
            throw new OaCompanylException(EnumCompanyError.PARAMETER_IS_NULL);
        }
        QueryWrapper<Company> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Company::getDeleted, 0);
        queryWrapper.lambda().eq(Company::getName, company.getName());
        Company company1 =  companyMapper.selectOne(queryWrapper);
        if(company1!=null){
            throw new OaCompanylException(EnumCompanyError.COMPANY_NAME_EXIST);
        }
        //需校验应用key和应用密钥是否与应用id匹配
        String appKey = company.getAppKey();
        String appSecret = company.getAppSecret();
        Long agentId = Long.valueOf(company.getAgentId());
        boolean isNotCheck = publicMethods.checkAppKeySecretAndAgentId(appKey, appSecret, agentId);
        if (!isNotCheck) {
            throw new OaCompanylException(EnumCompanyError.DATA_DOES_NOT_MATCH);
        }
        company.setId(UUIDS.getID());
        company.setCreateTime(System.currentTimeMillis());
        company.setDeleted(0);
        int sRof = companyMapper.insert(company);
        if (sRof <= 0) {
            throw new OaCompanylException(EnumCompanyError.NO_OPERATION_OK);
        }

        //新增公司增加定时任务
        quartzInit.initOneCompany(company.getId());
        return true;
    }

    /**
     * 修改分公司
     *
     * @param company
     * @return
     */
    @Override
    public boolean companyModify(Company company) throws ApiException {
        if (StringUtils.isAnyBlank(company.getId(), company.getName(), company.getAppKey(), company.getAgentId(), company.getAppSecret(), company.getCorpId())) {
            throw new OaCompanylException(EnumCompanyError.PARAMETER_IS_NULL);
        }
        //查询数据是否存在
        String id = company.getId();
        Company tcompany = companyMapper.selectById(id);
        if (tcompany == null) {
            throw new OaCompanylException(EnumCompanyError.COMPANY_ID_EMPTY);
        }
        //需校验应用key和应用密钥是否与应用id匹配
        String appKey = company.getAppKey();
        String appSecret = company.getAppSecret();
        Long agentId = Long.valueOf(company.getAgentId());
        boolean isNotCheck = publicMethods.checkAppKeySecretAndAgentId(appKey, appSecret, agentId);
        if (!isNotCheck) {
            throw new OaCompanylException(EnumCompanyError.DATA_DOES_NOT_MATCH);
        }
        tcompany.setModifyTime(System.currentTimeMillis());
        tcompany.setAgentId(company.getAgentId());
        tcompany.setAppKey(appKey);
        tcompany.setAppSecret(appSecret);
        tcompany.setName(company.getName());
        tcompany.setCorpId(company.getCorpId());
        int whetherSuccess = companyMapper.updateById(tcompany);
        if (whetherSuccess <= 0) {
            throw new OaCompanylException(EnumCompanyError.NO_OPERATION_OK);
        }
        return true;
    }

    /**
     * 根据任务Id获取钉钉企业id
     *
     * @param type 类型，如：SIGNIN_CONFIRM、ATTENDANCE_RECORD,EXAMINE_LEAVE 三种
     * @param id   任务id
     * @return 返回corpId
     */
    @Override
    public String getCorpIdById(String type, String id) {
        String corpId = null;
        if ("SIGNIN_CONFIRM".equals(type)) {
            corpId = companyMapper.getCompanyIdBySId(id);
        }
        if ("ATTENDANCE_RECORD".equals(type)) {
            corpId = companyMapper.getCompanyIdByAId(id);
        }
        if("EXAMINE_LEAVE".equals(type)){
            corpId = companyMapper.getCompanyIdByEId(id);
        }
        return corpId;
    }


}
