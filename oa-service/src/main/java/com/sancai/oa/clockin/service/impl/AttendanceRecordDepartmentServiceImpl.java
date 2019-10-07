package com.sancai.oa.clockin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.sancai.oa.clockin.entity.AttendanceRecord;
import com.sancai.oa.clockin.entity.AttendanceRecordDepartment;
import com.sancai.oa.clockin.mapper.AttendanceRecordDepartmentMapper;
import com.sancai.oa.clockin.service.IAttendanceRecordDepartmentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sancai.oa.clockin.service.IAttendanceRecordService;
import com.sancai.oa.score.entity.ActionScoreDepartment;
import com.sancai.oa.score.entity.ActionScoreRecord;
import com.sancai.oa.score.service.IActionScoreDepartmentService;
import com.sancai.oa.score.service.IActionScoreRecordService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 考勤记录和部门的对应关系表 服务实现类
 * </p>
 *
 * @author quanleilei
 * @since 2019-08-03
 */
@Service
public class AttendanceRecordDepartmentServiceImpl extends ServiceImpl<AttendanceRecordDepartmentMapper, AttendanceRecordDepartment> implements IAttendanceRecordDepartmentService {

    @Autowired
    private IAttendanceRecordService attendanceRecordService;

    @Autowired
    private IActionScoreRecordService actionScoreRecordService;

    @Autowired
    private IActionScoreDepartmentService actionScoreDepartmentService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW,rollbackFor = {Exception.class, RuntimeException.class})
    public void attendanceStatisticDelete(List<AttendanceRecord> attendanceRecordList) {
        List<String> ids = new ArrayList<>();
        List<String> recordIds = new ArrayList<>();
        //删除统计数据
        attendanceRecordList.stream().forEach(attendanceRecord -> {
            attendanceRecord.setDeleted(1);
            ids.add(attendanceRecord.getId());
            attendanceRecordService.updateById(attendanceRecord);
        });
        AttendanceRecordDepartment attendanceRecordDepartment = new AttendanceRecordDepartment();
        attendanceRecordDepartment.setDeleted(1);
        UpdateWrapper<AttendanceRecordDepartment> attendanceDepartmentUpdateWrapper = new UpdateWrapper<>();
        attendanceDepartmentUpdateWrapper.lambda().in(AttendanceRecordDepartment::getAttendanceRecordId,ids);
        update(attendanceRecordDepartment,attendanceDepartmentUpdateWrapper);

        //删除积分记录数据
        QueryWrapper<ActionScoreRecord> actionScoreRecordQueryWrapper = new QueryWrapper<>();
        actionScoreRecordQueryWrapper.lambda().in(ActionScoreRecord::getTargetId,ids);
        actionScoreRecordQueryWrapper.lambda().eq(ActionScoreRecord::getDeleted,0);
        List<ActionScoreRecord> actionScoreRecordList = actionScoreRecordService.list(actionScoreRecordQueryWrapper);
        if(CollectionUtils.isEmpty(actionScoreRecordList)){
            return;
        }
        actionScoreRecordList.stream().forEach(actionScoreRecord -> {
            actionScoreRecord.setDeleted(1);
            recordIds.add(actionScoreRecord.getId());
            actionScoreRecordService.updateById(actionScoreRecord);
        });
        //删除积分记录对应的部门关系表
        ActionScoreDepartment actionScoreDepartment = new ActionScoreDepartment();
        actionScoreDepartment.setDeleted(1);
        UpdateWrapper<ActionScoreDepartment> actionScoreDepartmentUpdateWrapper = new UpdateWrapper<>();
        actionScoreDepartmentUpdateWrapper.lambda().in(ActionScoreDepartment::getScoreRecordId,recordIds);
        actionScoreDepartmentService.update(actionScoreDepartment,actionScoreDepartmentUpdateWrapper);
    }

    /**
     * 通过id删除考勤积分数据
     *
     * @param attendanceRecordId
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW,rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteAttendanceRecord(String attendanceRecordId) {
        List<String> recordIds = new ArrayList<>();
        //删除积分记录数据
        QueryWrapper<ActionScoreRecord> actionScoreRecordQueryWrapper = new QueryWrapper<>();
        actionScoreRecordQueryWrapper.lambda().eq(ActionScoreRecord::getTargetId, attendanceRecordId);
        actionScoreRecordQueryWrapper.lambda().eq(ActionScoreRecord::getDeleted, 0);
        List<ActionScoreRecord> actionScoreRecordList = actionScoreRecordService.list(actionScoreRecordQueryWrapper);
        if(CollectionUtils.isEmpty(actionScoreRecordList)){
            return;
        }
        actionScoreRecordList.stream().forEach(actionScoreRecord -> {
            actionScoreRecord.setDeleted(1);
            recordIds.add(actionScoreRecord.getId());
        });
        for (ActionScoreRecord actionScoreRecord : actionScoreRecordList) {
            actionScoreRecordService.updateById(actionScoreRecord);
        }
        //删除积分记录对应的部门关系表
        ActionScoreDepartment actionScoreDepartment = new ActionScoreDepartment();
        actionScoreDepartment.setDeleted(1);
        UpdateWrapper<ActionScoreDepartment> actionScoreDepartmentUpdateWrapper = new UpdateWrapper<>();
        actionScoreDepartmentUpdateWrapper.lambda().in(ActionScoreDepartment::getScoreRecordId, recordIds);
        actionScoreDepartmentService.update(actionScoreDepartment, actionScoreDepartmentUpdateWrapper);
    }
}
