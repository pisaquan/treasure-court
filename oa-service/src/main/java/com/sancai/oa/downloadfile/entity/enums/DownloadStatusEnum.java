package com.sancai.oa.downloadfile.entity.enums;

import com.baomidou.mybatisplus.core.enums.IEnum;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 文件下载状态枚举
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum DownloadStatusEnum implements IEnum<String> {

    /**
     *   文件下载状态枚举
     */
    PROCESSING("PROCESSING","处理中"),
    COMPLETE("COMPLETE","处理完成"),
    FAILURE("FAILURE","处理失败")
    ;

    private String key;
    private String value;

    DownloadStatusEnum(final String key, final String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    public String getKey(){
        return this.key;
    }

    /**
     *根据key取Value
     * @param key 枚举的key
     * @return String 枚举的value
     */
    public static String getValueByKey(String key) {
        for (DownloadStatusEnum reportRecordTypeEnum : DownloadStatusEnum.values()) {
            if (key.equals(reportRecordTypeEnum.key)) {
                return reportRecordTypeEnum.getValue();
            }
        }
        return "";
    }

}
