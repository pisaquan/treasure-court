package com.sancai.oa.downloadfile.entity.enums;

import com.baomidou.mybatisplus.core.enums.IEnum;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 文件下载对应方法枚举
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum DownloadMethodPathEnum implements IEnum<String> {

    /**
     *   文件下载类型枚举（KEY为DownloadTypeEnum文件下载类型枚举对应的KEY , VALUE为需要调用的方法路径格式：类名(首写字母小写).方法名）
     */
    ATTENDANCE_STATISTICS("ATTENDANCE_STATISTICS","attendanceRecordServiceImpl.generateAttendanceExcel")
    ;

    private String key;
    private String value;

    DownloadMethodPathEnum(final String key, final String value) {
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
        for (DownloadMethodPathEnum reportRecordTypeEnum : DownloadMethodPathEnum.values()) {
            if (key.equals(reportRecordTypeEnum.key)) {
                return reportRecordTypeEnum.getValue();
            }
        }
        return "";
    }

}
