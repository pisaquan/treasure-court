package com.sancai.oa.downloadfile.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 文件下载记录
 * </p>
 *
 * @author fans
 * @since 2019-08-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class DownloadRecordDTO extends Model<DownloadRecordDTO> {

    private static final long serialVersionUID=1L;

    /**
     * 第几页
     */
    private Integer page;

    /**
     * 每页显示条数
     */
    private Integer capacity;

    /**
     * 员工id
     */
    @JsonProperty("admin_id")
    private String adminId;
    /**
     * 公司id
     */
    @JsonProperty("company_id")
    private String companyId;

    /**
     * 下载类型：考勤统计、公休统计等
     */
    private String type;

    @Override
    protected Serializable pkVal() {
        return null;
    }

}
