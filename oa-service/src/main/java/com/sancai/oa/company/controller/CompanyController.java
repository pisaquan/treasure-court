package com.sancai.oa.company.controller;


import com.sancai.oa.company.entity.Company;
import com.sancai.oa.company.exception.EnumCompanyError;
import com.sancai.oa.company.exception.OaCompanylException;
import com.sancai.oa.company.service.ICompanyService;
import com.sancai.oa.core.ApiResponse;
import com.sancai.oa.core.version.ApiVersion;
import com.sancai.oa.log.config.Log;
import com.sancai.oa.log.config.LogModelEnum;
import com.sancai.oa.log.config.LogOperationTypeEnum;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 分公司 前端控制器
 * </p>
 *
 * @author quanleilei
 * @since 2019-07-26
 */
@ApiVersion(1)
@RestController
@RequestMapping("{version}/company")
public class CompanyController {

    @Autowired
    ICompanyService icompanyService;

    /**
     * 分公司列表
     *
     * @param
     * @return
     */
    @GetMapping("/company_list")
    public ApiResponse companyList() {
        try {
            List<Company> companyList = icompanyService.companyList();
                return ApiResponse.success(companyList);
        } catch (Exception e) {
            throw new OaCompanylException(EnumCompanyError.NO_OPERATION_OK);
        }
    }

    /**
     * 分公司详情
     *
     * @param
     * @return
     */
    @GetMapping("/company_detail/{id}")
    public ApiResponse companyDetail(@PathVariable String id) {
        try {
            Company company = icompanyService.companyDetail(id);
            if (company != null) {
                return ApiResponse.success(company);
            }
            return ApiResponse.fail(EnumCompanyError.COMPANY_NOT_DATA);
        } catch (Exception e) {
            throw new OaCompanylException(EnumCompanyError.NO_OPERATION_OK);
        }
    }

    /**
     * 新增分公司
     *
     * @param company
     * @return
     */
    @PostMapping("/company_add")
    @Log( type =  LogOperationTypeEnum.SAVE,model = LogModelEnum.COMPANY)
    public ApiResponse companyAdd(@RequestBody Company company) throws Exception {
        boolean whetherSuccess = icompanyService.companyAdd(company);
        if (whetherSuccess) {
            return ApiResponse.success();
        }
        return ApiResponse.fail(EnumCompanyError.NO_OPERATION_OK);
    }

    /**
     * 修改分公司
     *
     * @param company
     * @return
     */
    @PostMapping("/company_modify")
    @Log( type =  LogOperationTypeEnum.UPDATE,model = LogModelEnum.COMPANY)
    public ApiResponse companyModify(@RequestBody Company company) throws Exception {
        boolean whetherSuccess = icompanyService.companyModify(company);
        if (whetherSuccess) {
            return ApiResponse.success();
        }
        return ApiResponse.fail(EnumCompanyError.NO_OPERATION_OK);
    }


    /**
     * 根据任务id取钉钉企业id(cropId)
     *
     * @param type 类型，如：SIGNIN_CONFIRM、ATTENDANCE_RECORD,EXAMINE_LEAVE 三种
     * @param id   任务id
     * @return 返回企业corpId
     */
    @GetMapping("/get_corp_id/{type}-{id}")
    public ApiResponse getCorpidById(@PathVariable String type, @PathVariable String id) {
        String corpId = null;
        try {
            corpId = icompanyService.getCorpIdById(type, id);
        } catch (Exception e) {
            throw new OaCompanylException(EnumCompanyError.FAIL_TO_GET_CORPID);
        }
        if (StringUtils.isBlank(corpId)) {
            return ApiResponse.fail(EnumCompanyError.QUERY_IS_EMPTY);
        }
        return ApiResponse.success(corpId);
    }


}
