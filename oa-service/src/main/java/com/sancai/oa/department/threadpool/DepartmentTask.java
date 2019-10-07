package com.sancai.oa.department.threadpool;

import com.dingtalk.api.response.OapiDepartmentListResponse;
import com.sancai.oa.core.threadpool.RollBack;
import com.sancai.oa.core.threadpool.ThreadResult;
import com.sancai.oa.core.threadpool.ThreadTaskCall;
import com.sancai.oa.dingding.user.DingDingUserService;
import com.sancai.oa.quartz.util.TaskMessage;
import com.sancai.oa.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CountDownLatch;

/**
 * 抓部门的任务
 * @Author chenm
 * @create 2019/8/27 14:43
 */
@Slf4j
public class DepartmentTask extends ThreadTaskCall<Map<String,List<String>>> {

    private String companyId;
    private String taskInstanceId;
    private DingDingUserService dingDingUserService;
    private List<OapiDepartmentListResponse.Department> departmentList;
    private int batch;
    private long startTime;

    public DepartmentTask(CountDownLatch childCountDown, CountDownLatch mainCountDown, BlockingDeque<ThreadResult> result, RollBack rollback, DataSourceTransactionManager transactionManager, Object obj, Map<String, Object> params) {
        super(childCountDown, mainCountDown, result, rollback, transactionManager, obj, params);
    }

    @Override
    public void initParam() {
        this.departmentList = (List<OapiDepartmentListResponse.Department>)  obj;

        this.batch = (int) getParam("batch");
        this.taskInstanceId = (String) getParam("taskInstanceId");
        this.companyId = (String) getParam("companyId");
        this.dingDingUserService = (DingDingUserService) getParam("dingDingUserService");

        this.startTime = (long) getParam("startTime");
    }


    /**
     * 执行任务,返回false表示任务执行错误，需要回滚 <?>
     * @return
     */
    @Override
    public ThreadResult processTask(){
        ThreadResult threadResult = new ThreadResult();
        Map<String,List<String>> res = new HashMap<String,List<String>>();
        List listAll = new ArrayList();
        try{

            long endTime = System.currentTimeMillis();
            for(OapiDepartmentListResponse.Department department : departmentList){
                // 钉钉部门下所有用户id集合
                List<String> list = dingDingUserService.allUserIdByDeptId(department.getId(),companyId);
                res.put(department.getId()+"",list);
            }
            int sec = (int) ((endTime-startTime)/1000);
            System.out.println("--第"+batch+"批部门用户抓取完成,第"+sec+"秒");
            TaskMessage.addMessage(taskInstanceId,"--第"+batch+"批部门用户抓取完成,第"+sec+"秒");
            listAll.add(res);
            threadResult.setData(listAll);
            threadResult.setFlag(true);
        }catch (Exception e){
            System.out.println(e.getMessage());
            TaskMessage.addMessage(taskInstanceId,"--第"+batch+"批部门抓取时异常："+e.getMessage());
            threadResult.setFlag(false);
            threadResult.setE(e);
        }

        return threadResult;
    }


}