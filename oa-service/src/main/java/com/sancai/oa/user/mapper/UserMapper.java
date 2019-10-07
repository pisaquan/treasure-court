package com.sancai.oa.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sancai.oa.user.entity.User;
import com.sancai.oa.user.entity.UserDTO;
import com.sancai.oa.user.entity.UserDepartment;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Author chenm
 * @create 2019/7/22 13:14
 */
@Repository
public interface UserMapper extends BaseMapper<User> {

    /**
     * 用户详情
     *
     * @param id
     * @return
     */
    User getUser(String id);

    /**
     * 查询用户
     *
     * @param userId
     * @param companyId
     * @return
     */
    UserDTO getUserByUserId(@Param("userId") String userId ,@Param("companyId") String companyId);


    /**
     * 新增用户
     *
     * @param user
     */
    void saveUser(User user);

    /**
     * 根据任务id从t_signin_confirm获取公司id
     *
     * @param id 任务id
     * @return 返回company_id
     */
    String getCompanyIdBySId(String id);

    /**
     * 根据任务id从t_attendance_record获取公司id
     *
     * @param id 任务id
     * @return 返回company_id
     */
    String getCompanyIdByAId(String id);

    /**
     * 批量保存用户数据
     *
     * @param userList 用户数据
     * @return 返回company_id
     */
    void batchSave(List<User> userList);

    /**
     * 批量更新离职用户
     *
     * @param userList 用户数据
     */
    void batchUpdateStatusOff(List<User> userList);

    /**
     * 查询一个部门下指定月的离职人员（用于考勤结果导出Excel中）
     *
     * @param deptId           部门id
     * @param firstDayLongTime 每月第一天的毫秒值
     * @param lastDayLongTime  每月最后一天的毫秒值
     * @return 返回一个部门查询月的离职人数
     */
    Integer queryDimission(@Param("deptId") String deptId,
                           @Param("firstDayLongTime") Long firstDayLongTime,
                           @Param("lastDayLongTime") Long lastDayLongTime);

    /**
     * 查询一个部门下的在职人员（用于考勤结果导出Excel中）
     *
     * @param deptId 部门id
     * @return 返回该部门下的在职人员数量
     */
    Integer queryInService(Integer deptId);

    /**
     * 分页获取公司下所有未删除用户信息
     * @param companyId  公司id
     * @return
     */
    List<UserDTO> listUserByCompany(@Param("companyId") String companyId);
    /**
     * 分页获取公司下所有未删除用户信息
     * @param companyId  公司id
     * @param status  状态
     *
     * @return
     */
    List<UserDTO> listUserByCompany(@Param("companyId") String companyId,@Param("status") Integer status);

    List<UserDTO> listUserByWorkDay(@Param("companyId") String companyId, @Param("status") int status,
                                    @Param("startTime") Long startTime, @Param("endTime") Long endTime);
    /**
     * 查询公司下名称匹配的用户信息
     * @param name  名称
     * @param type  类型
     * @param companyId  公司id
     * @return
     */
    List<UserDTO> listUserByCompanyAndName(@Param("name") String name,@Param("type") int type,@Param("companyId") String companyId);

    /**
     * 根据姓名 和 公司Id 获取获取在职或者不在职的员工列表（可能重名）
     * @param companyId
     * @param name
     * @param status
     * @return
     */
    List<UserDTO> getUserByUserName(@Param("companyId") String companyId,
                                    @Param("name") String name,
                                    @Param("status") Integer status);

    /**
     * 根据任务id从t_examine_leave获取公司id
     *
     * @param id 任务id
     * @return 返回company_id
     */
    String getCompanyIdByEId(String id);
}
