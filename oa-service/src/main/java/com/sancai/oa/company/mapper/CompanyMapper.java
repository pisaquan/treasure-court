package com.sancai.oa.company.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sancai.oa.company.entity.Company;
import org.springframework.stereotype.Repository;

/**
 * <p>
 * 分公司 Mapper 接口
 * </p>
 *
 * @author quanleilei
 * @since 2019-07-26
 */
@Repository
public interface CompanyMapper extends BaseMapper<Company> {

    /**
     * 在t_signin_confirm表中根据id查询companyId
     *
     * @param id
     * @return
     */
    String getCompanyIdBySId(String id);

    /**
     * 在t_attendance_record表中根据id查询companyId
     *
     * @param id
     * @return
     */
    String getCompanyIdByAId(String id);

    /**
     * 在t_examine_leave表中根据id查询companyId,再容t_company表中
     *
     * @param id
     * @return
     */
    String getCompanyIdByEId(String id);
}
