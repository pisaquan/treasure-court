package com.sancai.oa.signinconfirm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sancai.oa.signinconfirm.entity.SigninConfirm;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author wangyl
 * @since 2019-08-02
 */
public interface ISigninConfirmService extends IService<SigninConfirm> {
    /**
     * 公司签到确认列表
     *
     * @param map
     * @return
     */

    List<Map> signinConfirmListByCompany(Map<String, Object> map) throws Exception;
    /**
     * 公司签到确认详情
     *
     * @param id
     * @return
     */

    Map signinConfirmDetailById(String id) throws Exception;

    /**
     * 签到确认接口
     * @param id
     * @param status
     * @param confirm_user_id
     * @param attendance_id
     * @throws Exception
     */
    void signinConfirm(String id,String status,String confirm_user_id,String admin_id,String attendance_id) throws Exception;

}
