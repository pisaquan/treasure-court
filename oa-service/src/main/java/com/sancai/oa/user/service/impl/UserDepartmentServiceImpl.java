package com.sancai.oa.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sancai.oa.user.entity.UserDepartment;
import com.sancai.oa.user.mapper.UserDepartmentMapper;
import com.sancai.oa.user.service.ITUserDepartmentService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户部门关系表 服务实现类
 * </p>
 *
 * @author wangyl
 * @since 2019-09-17
 */
@Service
public class UserDepartmentServiceImpl extends ServiceImpl<UserDepartmentMapper, UserDepartment> implements ITUserDepartmentService {

}
