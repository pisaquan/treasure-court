package com.sancai.oa.downloadfile.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.sancai.oa.clockin.entity.AttendanceComplexResultDTO;
import com.sancai.oa.clockin.entity.DownloadQueryConditionDTO;
import com.sancai.oa.company.entity.Company;
import com.sancai.oa.company.mapper.CompanyMapper;
import com.sancai.oa.company.service.ICompanyService;
import com.sancai.oa.department.entity.Department;
import com.sancai.oa.department.service.IDepartmentService;
import com.sancai.oa.dingding.report.DingDingReportService;
import com.sancai.oa.downloadfile.entity.DownloadRecord;
import com.sancai.oa.downloadfile.entity.DownloadRecordDTO;
import com.sancai.oa.downloadfile.entity.enums.DownloadMethodPathEnum;
import com.sancai.oa.downloadfile.entity.enums.DownloadStatusEnum;
import com.sancai.oa.downloadfile.entity.enums.SelectMethodPathEnum;
import com.sancai.oa.downloadfile.exception.EnumDownloadError;
import com.sancai.oa.downloadfile.exception.OaDownloadlException;
import com.sancai.oa.downloadfile.mapper.DownloadRecordMapper;
import com.sancai.oa.downloadfile.service.IDownloadRecordService;
import com.sancai.oa.downloadfile.util.MethodReflectUtils;
import com.sancai.oa.examine.utils.UUIDS;
import com.sancai.oa.quartz.util.StringUtil;
import com.sancai.oa.typestatus.enums.DownloadTypeEnum;
import com.sancai.oa.user.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * <p>
 * 文件下载记录 服务实现类
 * </p>
 *
 * @author fans
 * @since 2019-08-22
 */
@Service
@Slf4j
public class DownloadRecordServiceImpl extends ServiceImpl<DownloadRecordMapper, DownloadRecord> implements IDownloadRecordService {


    @Autowired
    DownloadRecordMapper downloadRecordMapper;
    @Autowired
    DingDingReportService dingDingReportService;
    @Autowired
    IUserService iUserService;
    @Autowired
    private IDownloadRecordService downloadRecordService;
    @Autowired
    private CompanyMapper companyMapper;
    @Autowired
    private IDepartmentService departmentService;
    @Autowired
    ICompanyService companyService;

    /**
     * 下载文件路径
     */
    @Value("${filePath.zipStorePath}")
    private String zipStorePath;
    /**
     * 文件下载申请
     * @param downloadRecord
     * @return
     */

    @Override
    public boolean reqDownload(DownloadRecord downloadRecord) {

        if(StringUtils.isAnyBlank(downloadRecord.getAdminId(),downloadRecord.getUserName(),downloadRecord.getCompanyId(),downloadRecord.getType(),downloadRecord.getParam())){
            throw new OaDownloadlException(EnumDownloadError.PARAMETER_IS_NULL_REQ_DOWNLOADED);
        }
        //对必要参数进行校验
        DownloadQueryConditionDTO downloadQueryDTO = JSON.parseObject(downloadRecord.getParam(), DownloadQueryConditionDTO.class);
        if(downloadQueryDTO == null){
            throw new OaDownloadlException(EnumDownloadError.PARAMETER_IS_NULL_COMPANY_MONTH);
        }
        if(StringUtils.isBlank(downloadQueryDTO.getCompanyId())||StringUtils.isBlank(downloadQueryDTO.getMonth())){
            throw new OaDownloadlException(EnumDownloadError.PARAMETER_IS_NULL_COMPANY_MONTH);
        }
        //校验下载类型
        String type = DownloadTypeEnum.getValueByKey(downloadRecord.getType());
        String path = DownloadMethodPathEnum.getValueByKey(downloadRecord.getType());
        if(StringUtils.isBlank(path) || StringUtils.isBlank(type)){
            throw new OaDownloadlException(EnumDownloadError.DOWNLOADED_TYPE_NOT_DATA);
        }

        //获取查询下载数据方法，进行数据为空判断
        String selectPath = SelectMethodPathEnum.getValueByKey(downloadRecord.getType());
        if(StringUtils.isBlank(selectPath)){
            throw new OaDownloadlException(EnumDownloadError.DOWNLOADED_TYPE_NOT_DATA);
        }
        if(!selectPath.contains(".")){
            throw new OaDownloadlException(EnumDownloadError.DOWNLOADED_METHOD_NOT_DATA);
        }
        String classNameSelect = selectPath.substring(0,selectPath.lastIndexOf("."));
        String methodNameSelect = selectPath.substring(selectPath.lastIndexOf(".")+1);
        List<Object> data = (List<Object>) MethodReflectUtils.methodReflect(classNameSelect,methodNameSelect,downloadQueryDTO);
        if(data == null || data.size() == 0){
            throw new OaDownloadlException(EnumDownloadError.DOWNLOADED_NOT_DATA);
        }

        //校验一个人一个公司下载同一个的类型文件若存在有下载中的不能再次申请
        QueryWrapper <DownloadRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DownloadRecord::getDeleted, 0);
        queryWrapper.lambda().eq(DownloadRecord::getAdminId, downloadRecord.getAdminId());
        queryWrapper.lambda().eq(DownloadRecord::getStatus, DownloadStatusEnum.PROCESSING.getKey());
        queryWrapper.lambda().eq(DownloadRecord::getUserName, downloadRecord.getUserName());
        queryWrapper.lambda().eq(DownloadRecord::getCompanyId, downloadRecord.getCompanyId());
        queryWrapper.lambda().eq(DownloadRecord::getType, downloadRecord.getType());
        DownloadRecord downloadRecord1 = downloadRecordMapper.selectOne(queryWrapper);
        if(downloadRecord1 != null){
            throw new OaDownloadlException(EnumDownloadError.BEING_DOWNLOADED);
        }
        //添加下载记录
        String id = UUIDS.getID();
        downloadRecord.setCreateTime(System.currentTimeMillis());
        downloadRecord.setDeleted(0);
        downloadRecord.setId(id);
        downloadRecord.setParam(downloadRecord.getParam());
        downloadRecord.setStatus(DownloadStatusEnum.PROCESSING.getKey());
        int success = downloadRecordMapper.insert(downloadRecord);
        //根据路径取出类名，方法名
        if(!path.contains(".")){
            throw new OaDownloadlException(EnumDownloadError.DOWNLOADED_METHOD_NOT_DATA);
        }
        String className = path.substring(0,path.lastIndexOf("."));
        String methodName = path.substring(path.lastIndexOf(".")+1);

        //异步调用下载方法
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            DownloadQueryConditionDTO downloadQueryConditionDTO = JSON.parseObject(downloadRecord.getParam(), DownloadQueryConditionDTO.class);
            downloadQueryConditionDTO.setId(id);
            MethodReflectUtils.methodReflect(className,methodName,downloadQueryConditionDTO);
        }) .exceptionally(e -> {
            downloadRecordService.updateDownloadRecordStatus(id,e.getMessage(),false);
            throw new OaDownloadlException(EnumDownloadError.DOWNLOADED_DOWNLOAD_EXCEPTION);
        });
        CompletableFuture.runAsync(() -> {
            try {
                future.get(600, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                downloadRecordService.updateDownloadRecordStatus(id,e.getMessage(),false);
            } catch (ExecutionException e) {
                downloadRecordService.updateDownloadRecordStatus(id,e.getMessage(),false);
            } catch (TimeoutException e) {
                downloadRecordService.updateDownloadRecordStatus(id,"超时停止",false);

            }
        });
        if(success>0){
            return true;
        }
        return false;
    }

    /**
     * 下载文件列表
     * @param downloadRecordDTO
     * @return
     */
    @Override
    public List<DownloadRecord> downloadList(DownloadRecordDTO downloadRecordDTO){

        if(downloadRecordDTO.getCapacity()==null ||downloadRecordDTO.getPage() == null ||StringUtils.isBlank(downloadRecordDTO.getAdminId())){
            throw new OaDownloadlException(EnumDownloadError.PARAMETER_IS_NULL);
        }
        int pages = downloadRecordDTO.getPage();
        int capacity = downloadRecordDTO.getCapacity();
        //每页的大小为capacity，查询第page页的结果
        PageHelper.startPage(pages, capacity);
        QueryWrapper<DownloadRecord> queryWrapper = new QueryWrapper<>();
        if(StringUtils.isNotBlank(downloadRecordDTO.getCompanyId())){
            queryWrapper.lambda().eq(DownloadRecord::getCompanyId,downloadRecordDTO.getCompanyId());
        }
        if(StringUtils.isNotBlank(downloadRecordDTO.getType())){
            queryWrapper.lambda().eq(DownloadRecord::getType,downloadRecordDTO.getType());
        }
        queryWrapper.lambda().eq(DownloadRecord::getDeleted,0);
        queryWrapper.lambda().eq(DownloadRecord::getAdminId,downloadRecordDTO.getAdminId());
        queryWrapper.lambda().orderByDesc(DownloadRecord::getCreateTime,DownloadRecord ::getCompanyId);

        List<DownloadRecord> list = downloadRecordMapper.selectList(queryWrapper);

        QueryWrapper<Company> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.lambda().eq(Company::getDeleted, 0);
        List<Company> companyList  = companyMapper.selectList(queryWrapper1);

        for(DownloadRecord downloadRecord : list){
            downloadRecord.setType(DownloadTypeEnum.getValueByKey(downloadRecord.getType()));
            downloadRecord.setStatus(DownloadStatusEnum.getValueByKey(downloadRecord.getStatus()));
            String param = paramString(companyList, downloadRecord.getParam());
            downloadRecord.setParam(param);
        }

        return list;
    }

    /**
     * 对下载参数进行对应名称组合
     * @param companyList 公司数据集合
     * @param param 下载条件jsonString
     * @return
     */
    private String paramString ( List<Company> companyList,String param) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        DownloadQueryConditionDTO downloadQueryConditionDTO = JSON.parseObject(param, DownloadQueryConditionDTO.class);

        Company company  = companyList.stream().filter(c -> c.getId().equals(downloadQueryConditionDTO.getCompanyId())).findAny().orElse(null);

        if(StringUtils.isNotBlank(downloadQueryConditionDTO.getMonth())){
            map.put("month",downloadQueryConditionDTO.getMonth());
        }
        if(company!=null){
            map.put("companyName",company.getName());
        }
        if(downloadQueryConditionDTO.getDeptId()!=null){
            Department  result = departmentService.getDepartment(String.valueOf(downloadQueryConditionDTO.getDeptId()));
            if(result!=null&&result.getName()!=null){
                String deptName = result.getName();
                map.put("deptName",deptName);
            }
        }
        if(StringUtils.isNotBlank(downloadQueryConditionDTO.getUserName())){
            map.put("userName",downloadQueryConditionDTO.getUserName());
        }
        String jsonObject = JSONObject.fromObject(map).toString();
        return jsonObject;
    }
    /**
     * 下载文件
     * @param userId 用户id
     * @param downloadId 下载文件主键id
     */
    @Override
    public void downloadallfiles(HttpServletResponse responses ,String userId , String downloadId) {

       QueryWrapper<DownloadRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DownloadRecord::getDeleted, 0);
        queryWrapper.lambda().eq(DownloadRecord::getAdminId, userId);
        queryWrapper.lambda().eq(DownloadRecord::getId,downloadId);
        DownloadRecord downloadRecord = downloadRecordMapper.selectOne(queryWrapper);
        if(downloadRecord == null){
            throw new OaDownloadlException(EnumDownloadError.DOWNLOADED_FILE_NOT_DATA);
        }
        if(downloadRecord.getStatus().equals(DownloadStatusEnum.PROCESSING.getKey())){
            throw new OaDownloadlException(EnumDownloadError.BEING_DOWNLOADED);
        }
        if(downloadRecord.getStatus().equals(DownloadStatusEnum.FAILURE.getKey())){
            throw new OaDownloadlException(EnumDownloadError.DOWNLOADED_FAILURE);
        }
        download(downloadRecord.getFilePath(),responses,DownloadTypeEnum.getValueByKey(downloadRecord.getType()),downloadRecord.getCompanyId());
    }


    /**
     * 下载文件
     * @param path 文件的相对路径
     * @param response
     * @param fileTypeName 下载文件数据类型
     * @return
     */
    private   HttpServletResponse download(String path, HttpServletResponse response,String fileTypeName ,String companyId) {
        try {
            Company company =companyService.companyDetail(companyId);
            String paths = zipStorePath+path;
            // path是指欲下载的文件的路径。
            File file = new File(paths);
            // 取得文件类型
            String fileType = file.getName().substring(file.getName().lastIndexOf("."));
            // 文件重命名
            String filename = company.getName() + fileTypeName + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + fileType;
            // 以流的形式下载文件。
            InputStream fis = new BufferedInputStream(new FileInputStream(paths));
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();
            // 清空response
            response.reset();
            // 设置response的Header
            filename = new String(filename.getBytes("UTF-8"), "ISO-8859-1");
            response.addHeader("Content-Disposition", "attachment;filename=" + filename);
            response.addHeader("Content-Length", "" + file.length());
            OutputStream toClient = new BufferedOutputStream(response.getOutputStream());
            response.setContentType("application/octet-stream");
            toClient.write(buffer);
            toClient.flush();
            toClient.close();
        } catch (IOException ex) {
            throw new OaDownloadlException(EnumDownloadError.DOWNLOADED_DOWNLOAD_EXCEPTION);
        }
        return response;
    }




    /**
     * 修改文件下载记录状态
     * @param downloadId 下载记录id
     * @param filePath 文件路径
     * @param successOrFailure 是否成功 true 成功， false 失败
     */
    @Override
    public void updateDownloadRecordStatus(String downloadId ,String filePath , boolean successOrFailure ){
        QueryWrapper<DownloadRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DownloadRecord::getDeleted, 0);
        queryWrapper.lambda().eq(DownloadRecord::getId,downloadId);
        DownloadRecord downloadRecord = downloadRecordMapper.selectOne(queryWrapper);
        if(downloadRecord == null){
            throw new OaDownloadlException(EnumDownloadError.DOWNLOADED_FILE_NOT_DATA);
        }
        String status;
        if(successOrFailure){
            if(StringUtils.isBlank(filePath)){
                throw new OaDownloadlException(EnumDownloadError.DOWNLOADED_FILE_NOT_PATH);
            }
            status = DownloadStatusEnum.COMPLETE.getKey();
            filePath = getLocation(filePath,3);

        }else{
            status = DownloadStatusEnum.FAILURE.getKey();
        }
        downloadRecord.setFilePath(filePath);
        downloadRecord.setStatus(status);
        downloadRecord.setModifyTime(System.currentTimeMillis());
        downloadRecordMapper.updateById(downloadRecord);
    }
    /**
     *@Description: 从倒数第n个"/"开始截取到最后
     *@Param [str, n]
     *@return java.lang.String
     */
    private static String getLocation(String str,int n){
        int index=str.lastIndexOf(File.separator);
        for (int i = 0; i < n-1; i++) {
            index=str.lastIndexOf(File.separator,index-1);
        }
        String location=str.substring(index+1);
        return location;
    }
}
