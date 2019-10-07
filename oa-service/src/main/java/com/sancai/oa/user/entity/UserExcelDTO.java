package com.sancai.oa.user.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

/**
 * 读取Excel中用户信息实体
 * @Author fans
 * @create 2019/8/16 14:01
 */
@Data
public class UserExcelDTO {



    @Excel(name = "员工UserID")
    private String userId;

    @Excel(name = "姓名")
    private String name;

    @Excel(name = "手机号")
    private String phone;
}
