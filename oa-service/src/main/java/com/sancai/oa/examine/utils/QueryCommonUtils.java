package com.sancai.oa.examine.utils;

import com.sancai.oa.department.service.IDepartmentService;
import com.sancai.oa.examine.entity.QueryCommonDTO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 此工具类为请假接口，出差接口，公休接口共有方法的抽取
 *
 * @author fanjing
 * @date 2019/8/5
 */
@Service
public class QueryCommonUtils {

    @Autowired
    private IDepartmentService departmentService;

    /**
     * 将部门id转为部门name（可能多个id，逗号隔开）
     *
     * @param list 查询出来的请假列表集合
     * @param set  根据公司Id查询出来的部门id的集合
     */
    public static void setDeptNameAll(List<? extends QueryCommonDTO> list, Set set) {
        for (QueryCommonDTO commonDTO : list) {
            if (!StringUtils.isNotBlank(commonDTO.getDeptId())) {
                continue;
            }
            String deptName = getDeptName(set, commonDTO.getDeptId());
            commonDTO.setDeptName(deptName);
        }
    }

    /**
     * 将一条记录的deptId转换为deptName
     *
     * @param set     根据companyId从redis中获取的部门集合set(Map<String,String>)
     * @param deptIds 查询出列表的中一条记录的部门id，可能为多个
     * @return 返回部门名称，如多个，中间用空格隔开
     */
    public static String getDeptName(Set set, String deptIds) {

        StringBuilder builder = new StringBuilder();
        if (deptIds.contains(",")) {
            for (String s : deptIds.split(",")) {
                for (Object o : set) {
                    Map<String, String> map = (Map<String, String>) o;
                    if (map.get("id").equals(s)) {
                        if (builder.length() == 0) {
                            builder.append(map.get("name"));
                        } else {
                            builder.append("," + map.get("name"));
                        }
                    }
                }
            }
        } else {
            for (Object o : set) {
                Map<String, String> map = (Map<String, String>) o;
                if (map.get("id").equals(deptIds)) {
                    builder.append(map.get("name"));
                }
            }
        }
        return builder + "";
    }

    /**
     * 获取该部门以上的三层级部门名称   例：西安第二分公司-未央区域-海荣名城店
     *
     * @param companyId 公司id
     * @param deptId    该部门的id
     * @return
     */
    public String getThreeLevelDeptName(String companyId, String deptId) {
        String grandParentName = null;
        String superiorsDepartmentName = departmentService.getSuperiorsDepartmentName(companyId, deptId);
        if (StringUtils.isNotBlank(superiorsDepartmentName)) {
            String[] split = superiorsDepartmentName.split("-");
            if (split.length > 3) {
                grandParentName = split[split.length - 3] +"-"+ split[split.length - 2] +"-"+ split[split.length - 1];
            } else {
                grandParentName = superiorsDepartmentName;
            }

        }
        return grandParentName;
    }

    /**
     * 将多个部门的id排序
     *
     * @param deptIds 部门id的字符串
     * @return
     */
    public static String sortDeptIds(String deptIds) {
        if (!deptIds.contains(",")) {
            return deptIds;
        }
        String[] split = deptIds.split(",");
        int[] arr = new int[split.length];
        for (int i = 0; i < split.length; i++) {
            arr[i] = Integer.parseInt(split[i]);
        }
        //将int数组进行排序
        Arrays.sort(arr);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            if (i != arr.length - 1) {
                builder.append(arr[i] + ",");
            } else {
                builder.append(arr[i]);
            }
        }
        return builder.toString();
    }

}
