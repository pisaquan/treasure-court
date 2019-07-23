package com.sancai.oasystem.controller;


import com.sancai.oasystem.bean.TExamineLeave;
import com.sancai.oasystem.service.TExamineLeaveService;
import org.springframework.beans.factory.annotation.Autowired;
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
    //    tExamineLeaveService.saveBatch(examineBaseList);
    //}
}
