package com.sancai.oa.downloadfile.controller;


import com.github.pagehelper.PageInfo;
import com.sancai.oa.core.ApiResponse;
import com.sancai.oa.core.version.ApiVersion;
import com.sancai.oa.downloadfile.entity.DownloadRecord;
import com.sancai.oa.downloadfile.entity.DownloadRecordDTO;
import com.sancai.oa.downloadfile.exception.EnumDownloadError;
import com.sancai.oa.downloadfile.service.IDownloadRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 文件下载记录 前端控制器
 * </p>
 *
 * @author fans
 * @since 2019-08-22
 */
@ApiVersion(1)
@RestController
@RequestMapping("{version}/download")
public class DownloadRecordController {

    @Autowired
    IDownloadRecordService iDownloadRecordService;

    /**
     * 文件下载申请
     *
     */
    @PostMapping("/req_download")
    public ApiResponse reqDownload(@RequestBody DownloadRecord downloadRecord) {
        boolean succcess = iDownloadRecordService.reqDownload(downloadRecord);
        if(!succcess){
            return ApiResponse.fail(EnumDownloadError.NO_OPERATION_OK);
        }
        return ApiResponse.success();
    }


    /**
     * 文件下载列表
     *
     */
    @PostMapping("/download_list")
    public ApiResponse downloadList(@RequestBody DownloadRecordDTO downloadRecordDTO) {
        List<DownloadRecord>  list = iDownloadRecordService.downloadList(downloadRecordDTO);
        return ApiResponse.success(new PageInfo<>(list));
    }
    /**
     * 文件下载
     *
     */
    @GetMapping("/download_file/{user_id}/{download_id}")
    public void downloadFile(HttpServletResponse response , @PathVariable String user_id ,@PathVariable String download_id) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
        response.setHeader("Access-Control-Allow-Headers", ":x-requested-with,content-type");
        iDownloadRecordService.downloadallfiles(response,user_id,download_id);
    }

}

