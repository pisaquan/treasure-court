package com.sancai.oa.clockin.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 考勤结果导出Excel查询条件封装的实体类
 * @author fanjing
 * @date 2019/8/19
 */
@Data
public class DownloadQueryConditionDTO {
    /**
     * 下载文件记录id
     */
    @JsonProperty("id")
    private String id;
    /**
     * 公司id
     */
    @JsonProperty("company_id")
    private String companyId;
    /**
     * 姓名
     */
    @JsonProperty("user_name")
    private String userName;


    /**
     * 部门id
     */
    @JsonProperty("dept_id")
    private Long deptId;


    /**
     * 月份
     */
    private String month;

    /**
     * 下载部门id集合
     */
    private List<Long> deptList;
}
