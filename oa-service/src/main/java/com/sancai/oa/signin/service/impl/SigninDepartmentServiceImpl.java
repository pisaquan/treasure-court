package com.sancai.oa.signin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sancai.oa.signin.entity.SigninDepartment;
import com.sancai.oa.signin.mapper.SigninDepartmentMapper;
import com.sancai.oa.signin.service.ISigninDepartmentService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 签到实例和部门的对应关系表 服务实现类
 * </p>
 *
 * @author fans
 * @since 2019-08-27
 */
@Service
public class SigninDepartmentServiceImpl extends ServiceImpl<SigninDepartmentMapper, SigninDepartment> implements ISigninDepartmentService {

}
