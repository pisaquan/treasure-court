package com.sancai.oa.clockin.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.sancai.oa.clockin.entity.ClockinDepartment;
import com.sancai.oa.clockin.entity.ClockinRecord;
import com.sancai.oa.clockin.entity.ClockinRecordDTO;
import com.sancai.oa.clockin.exception.EnumClockinError;
import com.sancai.oa.clockin.exception.OaClockinlException;
import com.sancai.oa.clockin.mapper.ClockinDepartmentMapper;
import com.sancai.oa.clockin.mapper.ClockinRecordMapper;
import com.sancai.oa.clockin.service.IAttendanceRecordService;
import com.sancai.oa.clockin.service.IClockinRecordService;
import com.sancai.oa.department.entity.Department;
import com.sancai.oa.department.service.IDepartmentService;
import com.sancai.oa.report.entity.modify.DataMap;
import com.sancai.oa.signin.service.ISigninRecordService;
import com.sancai.oa.utils.OaMapUtils;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * <p>
 * 考勤打卡记录 服务实现类
 * </p>
 *
 * @author fans
 * @since 2019-07-26
 */
@Service
@Slf4j
public class ClockinRecordServiceImpl extends ServiceImpl<ClockinRecordMapper, ClockinRecord> implements IClockinRecordService {

    @Autowired
    ClockinRecordMapper tclockinRecordMapper;
    @Autowired
    IDepartmentService departmentService;
    @Autowired
    ClockinDepartmentMapper clockinDepartmentMapper;
    @Autowired
    ISigninRecordService signinRecordService;
    @Autowired
    ISigninRecordService iSigninRecordService;
    @Autowired
    IAttendanceRecordService attendanceRecordService;
    /**
     *
     *
     * 公司打卡记录列表
     * @param clockinRecordDTO 入参clockinRecordDTO
     * @return ApiResponse
     */
    @Override
    public List<DataMap> clockinRecordList(ClockinRecordDTO clockinRecordDTO){
        if(StringUtils.isBlank(clockinRecordDTO.getCompanyId())){
            throw new OaClockinlException(EnumClockinError.PARAMETER_IS_NULL_COMPANYID_PAGE);
        }
        int pages = clockinRecordDTO.getPage();
        int capacity = clockinRecordDTO.getCapacity();
        List<Department>  result = departmentService.listDepartment(clockinRecordDTO.getCompanyId());
        if(StringUtils.isNotBlank(clockinRecordDTO.getDeptId())){
            List<Long> longList = departmentService.listSubDepartment(clockinRecordDTO.getCompanyId(),clockinRecordDTO.getDeptId());
            clockinRecordDTO.setDeptList(longList);
        }
        //每页的大小为capacity，查询第page页的结果
        PageHelper.startPage(pages, capacity);
        List<DataMap>  clockinRecord = tclockinRecordMapper.clockinRecordList(clockinRecordDTO);
        if(clockinRecord!=null&&clockinRecord.size()>0){
            for (DataMap dataMap : clockinRecord){
                String deptId = dataMap.get("dept_ids").toString();
                String deptName = iSigninRecordService.getDeptName(result,deptId);
                dataMap.put("dept_name", deptName);
            }
        }
        return  clockinRecord;
    }



    /**
     *
     *
     * 公司打卡记录详情
     * @param id 记录详情id
     * @return ApiResponse
     */
    @Override
    public ClockinRecordDTO clockinRecordDetail(String id){
        ClockinRecordDTO clockinRecord  = tclockinRecordMapper.clockinRecordDetailById(id);
        if (clockinRecord!=null){
            String content = clockinRecord.getContent();
            //数据按时间排序
            String contents =  attendanceRecordService.sortContentByTime(content);
            clockinRecord.setContent(contents);
            return  clockinRecord;
        }
        return null;
    }

    /**
     * 按任务实例id删除
     * @param taskIntanceId
     */
    @Override
    public int deleteByTaskInstanceId(String taskIntanceId){
        QueryWrapper<ClockinRecord> query = new QueryWrapper<>();
        query.eq("task_instance_id",taskIntanceId);
        query.eq("deleted",0);
        List <ClockinRecord> cds = tclockinRecordMapper.selectList(query);
       if(cds == null || cds.size() == 0){
           return 0;
       }
        List<String> ids = new ArrayList<>();
        //删除记录
        cds.stream().forEach(ClockinRecord -> { ids.add(ClockinRecord.getId());});

        ClockinDepartment examineDepartment = new ClockinDepartment();
        examineDepartment.setDeleted(1);
        UpdateWrapper<ClockinDepartment> examineDepartmentUpdateWrapper = new UpdateWrapper<>();
        examineDepartmentUpdateWrapper.lambda().in(ClockinDepartment::getClockinRecordId, ids);
        int cdRow = clockinDepartmentMapper.update(examineDepartment, examineDepartmentUpdateWrapper);

        int row = tclockinRecordMapper.deleteByTaskInstanceId(taskIntanceId);
        return row;
    }


}
