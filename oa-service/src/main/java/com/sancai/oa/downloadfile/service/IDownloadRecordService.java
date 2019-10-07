package com.sancai.oa.downloadfile.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sancai.oa.downloadfile.entity.DownloadRecord;
import com.sancai.oa.downloadfile.entity.DownloadRecordDTO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 文件下载记录 服务类
 * </p>
 *
 * @author fans
 * @since 2019-08-22
 */
public interface IDownloadRecordService extends IService<DownloadRecord> {

    /**
     * 文件下载申请
     * @param downloadRecord
     * @return
     */
    boolean reqDownload(DownloadRecord downloadRecord) ;

    /**
     * 下载文件列表
     * @param downloadRecordDTO
     * @return
     */
    List<DownloadRecord> downloadList(DownloadRecordDTO downloadRecordDTO);

    /**
     * 下载文件
     *
     * @param userId
     * @param companyId
     */
    void downloadallfiles(HttpServletResponse response, String userId, String companyId);

    /**
     * 修改文件下载记录状态
     * @param downloadId 下载记录id
     * @param filePath 文件路径
     * @param successOrFailure 是否成功 true 成功， false 失败
     */
    void updateDownloadRecordStatus(String downloadId ,String filePath, boolean successOrFailure );
}
