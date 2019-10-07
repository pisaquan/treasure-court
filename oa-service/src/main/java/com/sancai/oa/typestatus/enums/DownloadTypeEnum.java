package com.sancai.oa.typestatus.enums;

import com.baomidou.mybatisplus.core.enums.IEnum;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 文件下载类型枚举
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum DownloadTypeEnum implements IEnum<String> {

    /**
     *   文件下载类型枚举
     */
    ATTENDANCE_STATISTICS("ATTENDANCE_STATISTICS","考勤统计"),
    HOLIDAYS_STATISTICS("HOLIDAYS_STATISTICS","公休统计")
    ;

    private String key;
    private String value;

    DownloadTypeEnum(final String key, final String value) {
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
        for (DownloadTypeEnum reportRecordTypeEnum : DownloadTypeEnum.values()) {
            if (key.equals(reportRecordTypeEnum.key)) {
                return reportRecordTypeEnum.getValue();
            }
        }
        return "";
    }

}
