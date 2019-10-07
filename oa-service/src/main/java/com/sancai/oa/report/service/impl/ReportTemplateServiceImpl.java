package com.sancai.oa.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dingtalk.api.response.OapiReportTemplateListbyuseridResponse;
import com.github.pagehelper.PageHelper;
import com.sancai.oa.dingding.report.DingDingReportService;
import com.sancai.oa.quartz.util.TaskMessage;
import com.sancai.oa.report.entity.ReportTemplate;
import com.sancai.oa.report.entity.enums.ReportTemplateTypeEnum;
import com.sancai.oa.report.exception.EnumReportError;
import com.sancai.oa.report.exception.OaReportlException;
import com.sancai.oa.report.mapper.ReportTemplateMapper;
import com.sancai.oa.report.service.IReportRuleService;
import com.sancai.oa.report.service.IReportTemplateService;
import com.sancai.oa.utils.LocalDateTimeUtils;
import com.sancai.oa.utils.OaMapUtils;
import com.sancai.oa.utils.UUIDS;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 日志模板 服务实现类
 * </p>
 *
 * @author  fans
 * @since 2019-07-19
 */
@Service
@Slf4j
public class ReportTemplateServiceImpl extends ServiceImpl<ReportTemplateMapper, ReportTemplate> implements IReportTemplateService {

    @Autowired
    private  ReportTemplateMapper treportTemplateMapper;
    @Autowired
    private DingDingReportService dingDingReportService;
    @Autowired
    private IReportRuleService iReportRuleService;
    /** 日报提交开始时间（时分秒字符）*/
    @Value("${reportsubmittime.begintime}")
    private String begintime;
    /** 日报提交结束时间（时分秒字符）*/
    @Value("${reportsubmittime.finishtime}")
    private String finishtime;



    /**
     * 导入日报模板数据
     *@param companyId 公司id
     * @return boolean
     */
    @Override
    public boolean importReportTemplateData(String companyId,String taskInstanceId){
        if(StringUtils.isBlank(companyId)){
            return false;
        }
        TaskMessage.addMessage(taskInstanceId,"日报模板数据抓取  companyId："+companyId);
        List<OapiReportTemplateListbyuseridResponse.ReportTemplateTopVo> list = dingDingReportService.reportTemplate(null,companyId);
        if(list==null || list.size()==0){
            return false;
        }
        TaskMessage.addMessage(taskInstanceId,"日报模板数据抓取模板：本次共抓取"+list.size()+"个模板");
        //查出数据库中子公司下所有的模板数据,待用
        QueryWrapper<ReportTemplate> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ReportTemplate::getCompanyId, companyId);
        queryWrapper.lambda().eq(ReportTemplate::getDeleted, 0);
        List<ReportTemplate> templateList = treportTemplateMapper.selectList(queryWrapper);
        //导入日报模板数据，初始化规则，并对模板code重复数据进行更新或过滤操作
        importReportTemplat(list , templateList, companyId,taskInstanceId);
        //根据钉钉最新模板数据集合更新数据库模板数据，将失效的模板标记已失效
        TaskMessage.addMessage(taskInstanceId,"日报模板数据抓取模板：更新模板开始");
        delInvalidReportTemplat(list , templateList,taskInstanceId);
        TaskMessage.addMessage(taskInstanceId,"日报模板数据抓取模板：导入与更新已完成，抓取任务结束");
        TaskMessage.finishMessage(taskInstanceId);
        return true;
    }

    /**
     * 导入日报模板数据
     * @param list
     * @param companyId
     */
    private void importReportTemplat(List<OapiReportTemplateListbyuseridResponse.ReportTemplateTopVo> list , List<ReportTemplate> templateList , String companyId,String taskInstanceId){
        int sumNew = 0;
        int sumExist = 0;
        for (OapiReportTemplateListbyuseridResponse.ReportTemplateTopVo reportMap :list) {
            //数据库中有模板数据，判断name,code是否重复
            boolean whetherExist = whetherExistTempCode(templateList,reportMap);
            if(whetherExist){
                TaskMessage.addMessage(taskInstanceId,"日报模板数据抓取模板："+reportMap.getName()+"已存在");
                sumExist++;
                continue;
            }
            TaskMessage.addMessage(taskInstanceId,"日报模板数据抓取模板："+reportMap.getName()+"新增");
            //新增日志模板,初始化日志模板规则(reportMap 钉钉数据,companyId 公司id)
            addTemplate(reportMap,companyId);
            sumNew++;
        }
        TaskMessage.addMessage(taskInstanceId,"日报模板数据抓取模板：导入完成,本次新增模板"+sumNew+"个，已存在"+sumExist+"个");
    }

    /**
     * 新增日志模板,初始化日志模板规则
     * @param reportMap 钉钉数据
     * @param companyId 公司id
     */
    @Override
    public void addTemplate( OapiReportTemplateListbyuseridResponse.ReportTemplateTopVo reportMap , String companyId){
        //数据库中没有模板数据或数据不重复进行新增模板逻辑
        String id  = UUIDS.getID();
        ReportTemplate tRt = new ReportTemplate();
        tRt.setId(id);
        tRt.setDeleted(0);
        tRt.setCode(reportMap.getReportCode());
        tRt.setCreateTime(System.currentTimeMillis());
        tRt.setName((reportMap.getName()));
        tRt.setCompanyId(companyId);
        tRt.setStatus(ReportTemplateTypeEnum.VALID.getKey());
        tRt.setBeginTime(begintime);
        tRt.setFinishTime(finishtime);
        //初始化日志模板提交开始结束时间
        treportTemplateMapper.insert(tRt);
        //调用新增默认日志模板规则方法，初始化日志模板规则
        iReportRuleService.initRule(id);
    }

    /**
     * 数据库中有模板数据，判断name,code是否重复
     * @param templateList 数据库模板集合
     * @param reportMap 钉钉模板数据
     * @return true 存在 ，否则不存在
     */
    private  boolean whetherExistTempCode(List<ReportTemplate> templateList , OapiReportTemplateListbyuseridResponse.ReportTemplateTopVo reportMap){
        ReportTemplate template;
        if(templateList!=null && templateList.size()>0){
            //根据模板code查询数据库集合中是否存在
            template = templateList.stream().filter(TReportTemplate -> TReportTemplate.getCode().equals(reportMap.getReportCode())).findAny().orElse(null);
            if(template != null){
                //code存在
                if(!template.getName().equals(reportMap.getName())){
                    //code重复name不同，更新name数据并跳过
                    template.setName(reportMap.getName());
                    template.setModifyTime(System.currentTimeMillis());
                    treportTemplateMapper.updateById(template);
                }
                return  true;
            }
            return false;
        }
        return false;
    }

    /**
     * 删除失效的模板，在数据库中标记模板status失效Deleted=1
     * @param list  钉钉最新模板数据集合
     * @param listTemplate  数据库模板数据集合
     */
    private void delInvalidReportTemplat( List<OapiReportTemplateListbyuseridResponse.ReportTemplateTopVo> list ,List<ReportTemplate> listTemplate,String taskInstanceId){
        if(listTemplate!=null&&listTemplate.size()>0){
            List<OapiReportTemplateListbyuseridResponse.ReportTemplateTopVo> reportTemplateList;
            for (ReportTemplate reportTemplate : listTemplate) {
                String code = reportTemplate.getCode();
                //判断数据库中的模板是否在钉钉最新模板集合中，
                reportTemplateList = list.stream().filter(ReportTemplateTopVo -> ReportTemplateTopVo.getReportCode().equals(code)).collect(Collectors.toList());
                //不存在钉钉最新模板数据集合中，修改该模板status失效Deleted=1
                if(reportTemplateList == null || reportTemplateList.size() == 0){
                    reportTemplate.setStatus(ReportTemplateTypeEnum.INVALID.getKey());
                    reportTemplate.setDeleted(1);
                    reportTemplate.setModifyTime(System.currentTimeMillis());
                    treportTemplateMapper.updateById(reportTemplate);
                    TaskMessage.addMessage(taskInstanceId,"日报模板数据抓取模板：更新"+reportTemplate.getName()+"已失效");
                }
            }
        }

    }

    /**
     * 日报模板列表(同时用于日报列表筛选条件，兼容不分页情况)
     *
     * @return List
     */
    @Override
    public List<ReportTemplate> reportTemplateList (Map<String,Object> map){
        if(OaMapUtils.mapIsAnyBlank(map,"company_id")){
            throw new OaReportlException(EnumReportError.PARAMETER_IS_NULL);
        }
        if(!OaMapUtils.mapIsAnyBlank(map,"page","capacity")){
            int pages = Integer.valueOf(map.get("page").toString());
            int capacity = Integer.valueOf(map.get("capacity").toString());
            PageHelper.startPage(pages, capacity);
        }
        //每页的大小为capacity，查询第page页的结果
        QueryWrapper<ReportTemplate> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ReportTemplate::getDeleted, 0);
        queryWrapper.lambda().eq(ReportTemplate::getCompanyId, String.valueOf(map.get("company_id")));
        queryWrapper.lambda().orderByDesc(ReportTemplate::getName);
        List<ReportTemplate> reportTemplates = treportTemplateMapper.selectList(queryWrapper);
        return reportTemplates;
    }
    /**
     * 日报模板详情
     *@param id 模板详情id
     * @return ReportTemplate
     */
    @Override
    public ReportTemplate reportTemplateDetail (String id){
        QueryWrapper<ReportTemplate> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ReportTemplate::getDeleted, 0);
        queryWrapper.lambda().eq(ReportTemplate::getId, id);
        ReportTemplate reportTemplate = treportTemplateMapper.selectOne(queryWrapper);
        if(reportTemplate !=null){
            return reportTemplate;
        }
        return null;
    }

    /**
     * 日报模板详情
     *@param code 模板详情code
     * @return ReportTemplate
     */
    @Override
    public ReportTemplate reportTemplateDetailByCode (String code){
        QueryWrapper<ReportTemplate> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ReportTemplate::getDeleted, 0);
        queryWrapper.lambda().eq(ReportTemplate::getCode, code);
        ReportTemplate reportTemplate = treportTemplateMapper.selectOne(queryWrapper);
        if(reportTemplate !=null){
            return reportTemplate;
        }
        return null;
    }


}
