package com.sancai.oa.downloadfile.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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
@TableName("t_download_record")
public class DownloadRecord extends Model<DownloadRecord> {

    private static final long serialVersionUID=1L;

    /**
     * 编号
     */
    private String id;

    /**
     * 员工id
     */
    @JsonProperty("admin_id")
    private String adminId;
    /**
     * 员工姓名
     */
    @JsonProperty("user_name")
    private String userName;

    /**
     * 公司id
     */
    @JsonProperty("company_id")
    private String companyId;

    /**
     * 下载类型：考勤统计、公休统计等
     */
    private String type;

    /**
     * 参数
     */
    private String param;

    /**
     * 状态：处理中，处理完成，失败
     */
    private String status;

    /**
     * 文件相对路径
     */
    private String filePath;

    /**
     * 创建时间
     */
    @JsonProperty("create_time")
    private Long createTime;

    /**
     * 最后修改时间
     */
    private Long modifyTime;

    /**
     * 是否删除(0:未删,1:已删)
     */
    private Integer deleted;


    @Override
    protected Serializable pkVal() {
        return null;
    }

}
