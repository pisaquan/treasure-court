package com.sancai.oasystem.controller;


import com.sancai.oasystem.bean.ExamineImplFactory;
import com.sancai.oasystem.bean.enums.ExamineTypeEnum;
import com.sancai.oasystem.service.IExamineDataService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.stereotype.Controller;

/**
 * <p>
 * 审批表单 前端控制器
 * </p>
 *
 * @author pisaquan
 * @since 2019-07-18
 */
@Controller
@RequestMapping("/tExamine")
public class TExamineController {

    //@Autowired
    //private IUserService userService;

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/pullDingTalkExamineData")
    public void pullDingTalkExamineData(String group) {
        IExamineDataService examineCommonService = ExamineImplFactory.get(group);
        examineCommonService.dealExamineData(group);
        System.out.println(" 这里手动抛出异常，自动回滚数据");
        throw new RuntimeException();
    }

}
