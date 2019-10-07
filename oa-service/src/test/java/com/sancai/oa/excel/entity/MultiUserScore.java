package com.sancai.oa.excel.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.annotation.ExcelTarget;
import lombok.Data;

/**
 * @author fanjing
 * @date 2019/9/11
 */
@Data
@ExcelTarget("MultiUserScore")
public class MultiUserScore {

    /**
     * 部门全路径
     */
    @Excel(name = "部门",width = 40)
    private String deptName;
    /**
     * 员工id
     */
    @Excel(name = "员工id",width = 25)
    private String userId;

    /**
     * 员工姓名
     */
    @Excel(name = "员工姓名",width = 25)
    private String name;

    /**
     * 是否在职
     */
    @Excel(name = "是否在职",width = 25)
    private Integer status;

}
