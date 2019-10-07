package com.sancai.oa.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sancai.oa.department.entity.Department;
import com.sancai.oa.department.entity.TDepartment;
import com.sancai.oa.user.entity.User;
import com.sancai.oa.user.entity.UserDTO;
import com.sancai.oa.user.entity.UserExcelDTO;
import com.taobao.api.ApiException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 用户
 * @author fanjing
 * @date 2019/8/5
 */
public interface IUserService extends IService<User> {
    /**
     * 根据钉钉登录授权码取用户id
     * @param type  SIGNIN_CONFIRM、ATTENDANCE_RECORD两种
     * @param code 钉钉登录授权码
     * @param id 任务id
     * @return 返回userid
     */
    String getUserIdByCode(String type,String code,String id) throws ApiException;

    /**
     * 分页获取公司下所有在职用户信息
     * @param page
     * @param capacity
     * @param companyId
     * @return
     */
    List<User> listUser(int page,int capacity,String companyId);


    /**
     * 分页获取公司下所有未删除用户信息
     * @param companyId 公司id
     * @param status 状态
     * @return
     */
    List<UserDTO> listUserByCompany(String companyId,Integer status);
    /**
     * 分页获取公司下所有在职用户信息
     * @param page
     * @param companyId
     * @return
     */
    List<User> listUser(int page,String companyId);
    /**
     * 分页获取公司下所有用户信息，默认capacity 100
     * @param companyId  公司id
     * @param status 0：在职，1：离职
     *  @param startTime  开始时间
     *  @param endTime    截止时间
     * @return
     */
    List<UserDTO> listUser(String companyId,int status,Long startTime,Long endTime);

    UserDTO getUserByUserId(String userId, String companyId);


    /**
     * 更新离职用户状态
     * @param companyId
     * @param taskInstanceId
     */
    void updateOffUserinfoStatus(String companyId,String taskInstanceId) throws Exception;
    /**
     * 导入离职员工信息
     * @param file excel文件
     * @param companyId 公司id
     */
    void initUserInfoByExcel(MultipartFile file , String companyId);

    /**
     * 根据用户名称获取用户list
     * @param name
     * @param type
     * @return
     */
    List<Map> getUserInfo(String companyId,String name, int type);


    /**
     * 导入离职员工信息
     *
     * @param companyId 公司id
     */
     void  initUserInfo(List<UserExcelDTO> userExcelDTOList ,String companyId,String taskInstanceId  ,List<Department> departmentRedis);

    /**
     * 离职用户部门信息持久化
     *
     * @param companyId
     */
    void insertDeptId( String companyId);
}
