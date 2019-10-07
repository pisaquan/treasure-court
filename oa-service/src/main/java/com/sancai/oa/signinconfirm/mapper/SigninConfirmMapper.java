package com.sancai.oa.signinconfirm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sancai.oa.signinconfirm.entity.SigninConfirm;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author wangyl
 * @since 2019-08-03
 */
@Repository
public interface SigninConfirmMapper extends BaseMapper<SigninConfirm> {
    /**
     * 签到外出确认列表
     * @param map
     * @return
     */
    List<Map> signinConfirmListByCompany(Map<String,Object> map);

    /**
     * 签到外出确认详情
     * @param id
     * @return
     */
    Map signinConfirmDetailById(String id);

    /**
     * 根据签到记录查询到关联的签到确认id
     * @param id
     * @return
     * @throws Exception
     */
    String signConfirmIdBySignRecordId(String id) throws Exception;

    /**
     * 判断签到确认记录下所有的签到是否都已经确认
     * @param id
     * @return
     * @throws Exception
     */
    Integer signConfirmAllDone(String id) throws Exception;
}
