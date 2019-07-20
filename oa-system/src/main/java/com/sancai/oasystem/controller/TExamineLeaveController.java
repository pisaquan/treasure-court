package com.sancai.oasystem.controller;


import com.baomidou.mybatisplus.extension.api.R;
import com.sancai.oasystem.bean.ExamineBaseVO;
import com.sancai.oasystem.bean.TExamineLeave;
import com.sancai.oasystem.service.IExamineCommonService;
import com.sancai.oasystem.service.TExamineLeaveService;
import com.sancai.oasystem.service.impl.ExamineCommonServiceImpl;
import com.taobao.api.internal.toplink.embedded.websocket.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * <p>
 * 请假 前端控制器
 * </p>
 *
 * @author pisaquan
 * @since 2019-07-18
 */
@Controller
@RequestMapping("/tExamineLeave")
public class TExamineLeaveController {

    //@Autowired
    //private TExamineLeaveService tExamineLeaveService;
    //
    //@GetMapping("/api")
    //public void testError() {
    //    List<TExamineLeave> examineBaseList = tExamineLeaveService.pullDingTalkLeaveData();
    //    //tExamineLeaveService.saveBatch(examineBaseList);
    //}
}
