package com.sancai.oa.excel.entity;


import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.annotation.ExcelTarget;
import lombok.Data;

@Data
@ExcelTarget("UserScore")
public class UserScore {

    @Excel(name = "姓名",width = 30)
    private String name;

    @Excel(name = "本月积分",width = 30)
    private Float score;
}
