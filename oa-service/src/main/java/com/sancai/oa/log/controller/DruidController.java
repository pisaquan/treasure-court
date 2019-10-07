package com.sancai.oa.log.controller;


import com.sancai.oa.clockin.service.IClockinRecordService;
import com.sancai.oa.company.service.ICompanyService;
import com.sancai.oa.core.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * 连接池监控
 */

@RestController
@RequestMapping("druid")
public class DruidController {

    @Autowired
    private DataSource dataSource;
    @Autowired
    ICompanyService icompanyService;
    @Autowired
    IClockinRecordService itClockinRecordService;
    @RequestMapping("/index")
    @ResponseBody
    public String index() {

        return dataSource.toString();
    }


    @GetMapping("/test")
    @ResponseBody
    public ApiResponse testConnct(@RequestParam("count") int count) {
        List<String> result = new ArrayList<>();


        for(int i=1;i<=count;i++){
            try {

                Connection connection = dataSource.getConnection();
                PreparedStatement prepareStatement = connection
                        .prepareStatement("select * from t_company limit 1");
                ResultSet resultSet = prepareStatement.executeQuery();
                resultSet.next();
                result.add("ok:"+resultSet.getString("name"));
//                connection.close();
            }catch (Exception e) {
                System.out.println(e.getMessage());
                result.add("Exception:"+e.getMessage());
            }
        }
        return ApiResponse.success(result);
    }

}
