package com.qingying.springboot.service.impl;

import com.qingying.springboot.entity.ClockinRecord;
import com.qingying.springboot.mapper.ClockinRecordMapper;
import com.qingying.springboot.service.IClockinRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 考勤打卡记录 服务实现类
 * </p>
 *
 * @author pisaquan
 * @since 2019-08-01
 */
@Service
public class ClockinRecordServiceImpl extends ServiceImpl<ClockinRecordMapper, ClockinRecord> implements IClockinRecordService {

}
