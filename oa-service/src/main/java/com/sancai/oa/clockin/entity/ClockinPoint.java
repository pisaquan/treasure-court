package com.sancai.oa.clockin.entity;

import com.sancai.oa.clockin.enums.EnumClockinPointStatus;
import com.sancai.oa.clockin.enums.EnumDingDingAttendanceCheckType;
import com.sancai.oa.clockin.enums.EnumDingDingAttendanceLocationResult;
import com.sancai.oa.clockin.enums.EnumDingDingAttendanceTimeResult;
import lombok.Data;

import java.util.Date;

/**
 * 具体的一个打卡点
 * @Author chenm
 * @create 2019/8/1 10:55
 */
@Data
public class ClockinPoint {

    private Long id;

    private Date baseCheckTime;

    private Date userCheckTime;

    private EnumClockinPointStatus status;

    private EnumDingDingAttendanceCheckType checkType;

    private EnumDingDingAttendanceLocationResult locationResult;

    private EnumDingDingAttendanceTimeResult timeResult;

    public ClockinPoint(){}

    public ClockinPoint(Long id,Date baseCheckTime, Date userCheckTime, EnumDingDingAttendanceCheckType checkType, EnumDingDingAttendanceLocationResult locationResult, EnumDingDingAttendanceTimeResult timeResult) {
        this.id = id;
        this.baseCheckTime = baseCheckTime;
        this.userCheckTime = userCheckTime;
        this.status =  getClockinPointStatus(timeResult,locationResult);
        this.checkType = checkType;
        this.locationResult = locationResult;
        this.timeResult = timeResult;
    }

    /**
     * 根据打卡的时间结果和位置结果判断这个打卡点的状态
     * @param timeResult
     * @param locationResult
     * @return
     */
    private EnumClockinPointStatus getClockinPointStatus(EnumDingDingAttendanceTimeResult timeResult,EnumDingDingAttendanceLocationResult locationResult){

        if(EnumDingDingAttendanceLocationResult.Outside.equals(locationResult) || EnumDingDingAttendanceLocationResult.NotSigned.equals(locationResult)){
            return EnumClockinPointStatus.NOTSIGNED;
        }
        if(EnumDingDingAttendanceTimeResult.Early.equals(timeResult)){
            return EnumClockinPointStatus.EARLY;
        }
        if(EnumDingDingAttendanceTimeResult.Late.equals(timeResult) || EnumDingDingAttendanceTimeResult.SeriousLate.equals(timeResult)){
            return EnumClockinPointStatus.LATE;
        }
        if(EnumDingDingAttendanceTimeResult.Absenteeism.equals(timeResult)){
            return EnumClockinPointStatus.ABSENTEEISM;
        }
        if(EnumDingDingAttendanceTimeResult.Normal.equals(timeResult)){
            return EnumClockinPointStatus.NORMAL;
        }
        return EnumClockinPointStatus.NOTSIGNED;
    }
}
