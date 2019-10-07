package com.sancai.oa.excel.service;


import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.afterturn.easypoi.excel.entity.enmus.ExcelType;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sancai.oa.Application;
import com.sancai.oa.department.service.IDepartmentService;
import com.sancai.oa.examine.utils.UUIDS;
import com.sancai.oa.excel.entity.MultiUserScore;
import com.sancai.oa.excel.entity.UserScore;
import com.sancai.oa.score.entity.ActionScoreDepartment;
import com.sancai.oa.score.entity.ActionScoreRecord;
import com.sancai.oa.score.mapper.ActionScoreRecordMapper;
import com.sancai.oa.score.service.IActionScoreDepartmentService;
import com.sancai.oa.typestatus.enums.ScoreRecordSourceEnum;
import com.sancai.oa.typestatus.enums.ScoreRecordTypeEnum;
import com.sancai.oa.user.entity.User;
import com.sancai.oa.user.entity.UserDTO;
import com.sancai.oa.user.entity.UserDepartment;
import com.sancai.oa.user.mapper.UserMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author fanjing
 * @create 2019/9/11
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class ReadExcelMessage {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ActionScoreRecordMapper actionScoreRecordMapper;

    @Autowired
    private IActionScoreDepartmentService actionScoreDepartmentService;

    @Autowired
    private IDepartmentService departmentServiceImpl;


    /**
     * 往积分记录表中导入员工初始积分值 同时生成Excel记录姓名重名或姓名未找到的员工
     */
    @Test
    public void exportUserExcel() {

        //公司id
        String companyId = "727052b0a78c430e90a510111658ff2b";
        //读取的Excel文件位置
        String importExcelPath = "D:/file/template.xls";
        //导出Excel的位置 exportUserInfo为Excel文件名
        String exportExcelPath = "D:/file/temp/exportUserInfo";

        Map<String, List> map = importUserData(companyId, importExcelPath);
        ExportParams multiParams = new ExportParams();
        // 设置sheet得名称
        multiParams.setSheetName("姓名重名");
        // 创建sheet1使用得map
        Map<String, Object> multiUserMap = new HashMap<>();
        // title的参数为ExportParams类型，目前仅仅在ExportParams中设置了sheetName
        multiUserMap.put("title", multiParams);
        // 模版导出对应得实体类型
        multiUserMap.put("entity", MultiUserScore.class);
        // sheet中要填充得数据
        multiUserMap.put("data", map.get("multiUserList"));

        ExportParams notFoundParams = new ExportParams();
        notFoundParams.setSheetName("姓名未找到");
        Map<String, Object> notFoundMap = new HashMap<>();
        notFoundMap.put("title", notFoundParams);
        notFoundMap.put("entity", UserScore.class);
        notFoundMap.put("data", map.get("notFoundList"));

        List<Map<String, Object>> sheetsList = new ArrayList<>();
        sheetsList.add(multiUserMap);
        sheetsList.add(notFoundMap);
        // 执行方法
        Workbook workBook = ExcelExportUtil.exportExcel(sheetsList, ExcelType.HSSF);
        try {
            long timeStamp = System.currentTimeMillis();
            FileOutputStream os = new FileOutputStream(exportExcelPath + timeStamp + ".xls");
            workBook.write(os);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map importUserData(String companyId, String importExcelPath) {
        ImportParams params = new ImportParams();
        //表格标题行数
        params.setTitleRows(1);
        //表头行数（默认1，各公司表头实际为2）
        params.setHeadRows(2);
        //读取第三个sheet（第一个从0开始）
        params.setStartSheetIndex(2);
        List<UserScore> list1 = ExcelImportUtil.importExcel(new File(importExcelPath), UserScore.class, params);
        //创建存储未找到员工的集合
        List<UserScore> notFoundList = new ArrayList<>();
        //创建存储重名员工信息的集合
        List<MultiUserScore> multiUserList = new ArrayList<>();
        for (UserScore userScore : list1) {
            //查询员工信息
            List<UserDTO> userDTOS = userMapper.getUserByUserName(companyId, userScore.getName(), 0);
            //如果分值为0,跳过
            if (userScore.getScore() == 0) {
                continue;
            }
            //1.判断根据姓名和公司id查询出的员工个数
            //1.1该员工未找到
            if (userDTOS.size() == 0) {
                List<UserDTO> userDTOS1 = userMapper.getUserByUserName(companyId, userScore.getName(), 1);
                //查询到的离职人数为1，插入该员工的记录
                if (userDTOS1.size() == 1) {
                    insertUserScore(userScore, companyId, userDTOS1);
                    continue;
                    //查询到的离职人数大于1，说明可能重名，导出到Excel的姓名重复中
                } else if (userDTOS1.size() > 1) {
                    for (UserDTO userDTO : userDTOS1) {
                        MultiUserScore multiUserScore = new MultiUserScore();
                        multiUserScore.setName(userDTO.getName());
                        multiUserScore.setStatus(userDTO.getStatus());
                        multiUserScore.setUserId(userDTO.getUserId());
                        List<UserDepartment> userDepartments = userDTO.getUserDepartments();
                        //查询全路径部门
                        String superDepartmentName = getSuperDepartmentName(userDepartments, companyId);
                        multiUserScore.setDeptName(superDepartmentName);
                        multiUserList.add(multiUserScore);
                    }
                    continue;
                    //未查询到数据，导出在Excel的姓名未找到中
                } else {
                    notFoundList.add(userScore);
                    continue;
                }
            }
            //1.2该员工存在重名
            if (userDTOS.size() >= 2) {
                for (UserDTO userDTO : userDTOS) {
                    MultiUserScore multiUserScore = new MultiUserScore();
                    multiUserScore.setName(userDTO.getName());
                    multiUserScore.setStatus(userDTO.getStatus());
                    multiUserScore.setUserId(userDTO.getUserId());
                    List<UserDepartment> userDepartments = userDTO.getUserDepartments();
                    //查询全路径部门
                    String superDepartmentName = getSuperDepartmentName(userDepartments, companyId);
                    multiUserScore.setDeptName(superDepartmentName);
                    multiUserList.add(multiUserScore);
                }
                continue;
            }
            insertUserScore(userScore, companyId, userDTOS);
        }
        //导出姓名重复和姓名未找到的
        HashMap<String, List> map = new HashMap<>();
        map.put("notFoundList", notFoundList);
        map.put("multiUserList", multiUserList);
        return map;
    }

    /**
     * 测试是否可以读取到Excel中的内容
     */
    @Test
    public void test2() {

        String importExcelPath = "D:/file/template.xls";
        ImportParams params = new ImportParams();
        //表格标题行数
        params.setTitleRows(1);
        //表头行数（默认1，各公司表头实际为2）
        params.setHeadRows(2);
        //读取第三个sheet（第一个从0开始）
        params.setStartSheetIndex(2);
        List<UserScore> list1 = ExcelImportUtil.importExcel(new File(importExcelPath), UserScore.class, params);
        System.out.println(list1.size());
        for (UserScore userScore : list1) {
            System.out.println(userScore.getName() + "=" + userScore.getScore());
        }
    }

    /**
     * 查询部门全路径
     *
     * @param userDepartments
     * @param companyId
     * @return
     */
    private String getSuperDepartmentName(List<UserDepartment> userDepartments, String companyId) {
        StringBuilder builder = new StringBuilder();
            for (UserDepartment userDepartment : userDepartments) {
                String departmentName = departmentServiceImpl.getSuperiorsDepartmentName(companyId, userDepartment.getDeptId());
                if (builder.length() == 0) {
                    builder.append(departmentName);
                } else {
                    builder.append(" , " + departmentName);
                }
            }
            return builder + "";
    }

    private void insertUserScore(UserScore userScore, String companyId, List<UserDTO> userDTOS) {
        //1.3产生该员工的积分变动记录
        //判断分值类型
        String type = null;
        Float score = userScore.getScore();

        if (userScore.getScore() > 0) {
            type = ScoreRecordTypeEnum.SCOREADD.getKey();

        }
        if (userScore.getScore() < 0) {
            type = ScoreRecordTypeEnum.SCORESUBTRACT.getKey();
            score = Math.abs(score);
        }
        ActionScoreRecord actionScoreRecord = new ActionScoreRecord();
        actionScoreRecord.setId(UUIDS.getID());
        actionScoreRecord.setCompanyId(companyId);
        actionScoreRecord.setSource(ScoreRecordSourceEnum.MANUALCORRECTION.getKey());
        actionScoreRecord.setCreateTime(System.currentTimeMillis());
        actionScoreRecord.setScoreRecordTime(System.currentTimeMillis());
        actionScoreRecord.setUserName(userScore.getName());
        actionScoreRecord.setType(type);
        actionScoreRecord.setScore(score);
        actionScoreRecord.setUserId(userDTOS.get(0).getUserId());
        actionScoreRecord.setDeleted(0);
        int result = actionScoreRecordMapper.insert(actionScoreRecord);
        if (result == 1) {
            //积分变动列表插入记录的同时往部门关系表插入记录（多个部门则为多条记录）
            List<UserDepartment> userDepartments = userDTOS.get(0).getUserDepartments();
            if (CollectionUtils.isEmpty(userDepartments)) {
                return;
            }
            List<Long> department = new ArrayList<>();
            for (UserDepartment userDepartment : userDepartments) {
                department.add(Long.parseLong(userDepartment.getDeptId()));
            }
            //该员工的部门id列表（一个或多个）
            List<ActionScoreDepartment> actionScoreDepartments = new ArrayList<>();
            if (!CollectionUtils.isEmpty(department)) {
                department.stream().forEach(departId -> {
                    ActionScoreDepartment actionScoreDepartment = new ActionScoreDepartment();
                    actionScoreDepartment.setId(UUIDS.getID());
                    actionScoreDepartment.setScoreRecordId(actionScoreRecord.getId());
                    actionScoreDepartment.setDeptId(new Long(departId).intValue());
                    actionScoreDepartment.setCreateTime(System.currentTimeMillis());
                    actionScoreDepartment.setDeleted(0);
                    actionScoreDepartments.add(actionScoreDepartment);
                });
            }
            if (!CollectionUtils.isEmpty(actionScoreDepartments)) {
                for (ActionScoreDepartment actionScoreDepartment : actionScoreDepartments) {
                    actionScoreDepartmentService.save(actionScoreDepartment);
                }
            }
        }
    }
}
