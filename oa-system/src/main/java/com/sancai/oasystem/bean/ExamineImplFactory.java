package com.sancai.oasystem.bean;

import com.sancai.oasystem.bean.enums.ExamineTypeEnum;
import com.sancai.oasystem.service.IExamineDataService;
import com.sancai.oasystem.service.impl.TExamineBusinessTravelServiceImpl;
import com.sancai.oasystem.service.impl.TExamineHolidayServiceImpl;
import com.sancai.oasystem.service.impl.TExamineLeaveServiceImpl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author pisaquan
 * @since 2019/7/22 14:18
 */
public class ExamineImplFactory {

    private static Map<String,IExamineDataService> examineInstances = new ConcurrentHashMap<>();

    static {
        put(ExamineTypeEnum.LEAVE.getValue(),new TExamineLeaveServiceImpl());
        put(ExamineTypeEnum.HOLIDAY.getValue(),new TExamineHolidayServiceImpl());
        put(ExamineTypeEnum.BUSINESSTRAVEL.getValue(),new TExamineBusinessTravelServiceImpl());
    }

    public static void put(String key,IExamineDataService iExamineDataService){
        examineInstances.put(key, iExamineDataService);
    }

    public static IExamineDataService get(String key){
        return examineInstances.get(key);
    }
}
