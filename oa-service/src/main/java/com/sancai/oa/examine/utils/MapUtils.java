/**
 *
 */
package com.sancai.oa.examine.utils;


import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @authorfans
 * @description 判断map及其key是否存在，以及其key是否为空和null
 * @description 判断list及其value是否为空和null
 * @description 判断json及其key是否存在，以及其key是否为空和null
 * @description 判断多个string是否为数字
 * @see/boolean mapIsAnyBlank(Map<?, ?> map, String... keys)
 */
public class MapUtils extends HashMap<String, Object> {

    /**
     *
     */
    private static final long serialVersionUID = 6143192896290822562L;

    /**
     * @param/Map
     * @param/String
     * @return boolean
     * @see/if(map,"") == null true
     * @see/if(map,null) == null true
     * @see/if(map,"null") == null true
     * @see/if(map,"key not exist") == null true
     * @see/if(map,"key==""") == null true
     * @see/if(map,"key==null") == null true
     * @see/others false
     */

    public static boolean mapIsAnyBlank(Map<?, ?> map, String... keys) {

        boolean flag = true;

        if (map == null) {
            return flag;
        }
        if (map.isEmpty() || map.size() == 0) {
            return flag;
        }
        if (keys == null || keys.equals("")) {
            return flag;
        }
        for (String key : keys) {
            if (StringUtils.isAnyBlank(key) && !map.containsKey(key)) {
                return flag;
            }
        }
        for (String key : keys) {
            if (map.get(key) == null || map.get(key).equals("null") || map.get(key).equals("")) {
                return flag;
            }
        }
        return flag = false;

    }

    /**
     * @paramJSON
     * @paramString
     * @returnboolean
     * @seeif(jsonObject,"") == null true
     * @seef(jsonObject,null) == null true
     * @seeif(jsonObject,"null") == null true
     * @seeif(jsonObject,"key not exist") == null true
     * @seeif(jsonObject,"key==""") == null true
     * @seeif(jsonObject,"key==null") == null true
     * @seeothers false
     */

    public static boolean jsonIsAnyBlank(JSONObject jsonObject, String... keys) {

        boolean flag = true;

        if (jsonObject == null) {
            return flag;
        }
        if (jsonObject.isEmpty() || jsonObject.size() == 0) {
            return flag;
        }
        if (keys == null || keys.equals("")) {
            return flag;
        }
        for (String key : keys) {
            if (!jsonObject.containsKey(key)) {
                return flag;
            }
        }
        for (String key : keys) {
            if (jsonObject.get(key) == null || jsonObject.get(key).equals("null") || jsonObject.get(key).equals("")) {
                return flag;
            }
        }
        return flag = false;

    }

    /**
     * @param //List
     * @return boolean
     * @seeif(list,"") == null true
     * @seeif(list,null) == null true
     * @seeif(list,"null") == null true
     * @seeothers false
     */

    public static boolean listIsAnyBlank(List<?> list) {

        boolean flag = true;
        Object object = null;

        if (list == null) {
            return flag;
        }
        if (list.isEmpty() || list.size() == 0) {
            return flag;
        }
        if (list.contains(object)) {
            return flag;
        }
        return flag = false;

    }

    /**
     * @param //String []
     * @return boolean
     * @see/if(//string not a numeric) == null true
     * @see/if(//string,"") == null true
     * @see/if(//string,"null") == null true
     * @see///others false
     */

    public static boolean IsAnyNumeric(String... is) {

        boolean flag = true;

        if (is == null) {
            return flag;
        }
        if (is.length == 0) {
            return flag;
        }
        for (String i : is) {
            if (StringUtils.isAnyBlank(i)) {
                return flag;
            }
            if (!StringUtils.isNumeric(i)) {
                return flag;
            }

        }
        return flag = false;

    }

    public static boolean isStringNotNull(String... is) {
        boolean flag = false;
        if (is == null) {
            return flag;
        }
        if (is.length == 0) {
            return flag;
        }
        for (String i : is) {
            if (StringUtils.isAnyBlank(i)) {
                return flag;
            }
        }
        flag = true;
        return flag;
    }

    /**
     * @param string
     * @return String
     * @see///判断传来的字符串是否是小于1大于0的两位小数
     */
    public static String isTwoPlaces(String string) {

        // 判断小数点所在位置
        int i_a = 0;
        i_a = StringUtils.indexOf(string, ".");
        if (i_a != 1) {
            return null;
        }

        // 判断位数长度
        int i_b = StringUtils.length(string);
        if (i_b < 3 || i_b > 4) {
            return null;
        }

        // 判断是否大于1
        boolean b_a = StringUtils.startsWith(string, "0");
        if (!b_a) {
            return null;
        }

        // 判断是否小于0
        String[] s = StringUtils.split(string, ".");
        for (int i = 0; i < s.length; i++) {
            if (s[1].equals("0") || s[1].equals("00")) {
                return null;
            }
        }

        return "flag";
    }

    /**
     * @throws
     * @Title: stringToMap
     * @Description: String转map
     * @param: @param jsonString
     * @param: @return
     * @return: Map<String   ,   Object>
     * @author: fans
     * @date: 2017年12月21日 下午2:49:11
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> stringToMap(String jsonString) {

        Map<String, Object> map = new HashMap<String, Object>();
        try {
            JSONObject jb = JSONObject.fromObject(jsonString);
            map = (Map<String, Object>) jb;
        } catch (Exception e) {
            return null;
        }
        return map;
    }

    /**
     * @throws
     * @Title: stringToMap
     * @Description: String转map
     * @param: @param jsonString
     * @param: @return
     * @return: Map<String   ,   String>
     * @author: fss
     * @date: 2017年12月21日 下午2:49:11
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> stringToMapString(String jsonString) {

        Map<String, String> map = new HashMap<String, String>();
        try {
            JSONObject jb = JSONObject.fromObject(jsonString);
            map = (Map<String, String>) jb;
        } catch (Exception e) {
            return null;
        }
        return map;
    }

    public static Object mapToObject(Map<String, Object> map, Class<?> beanClass) throws Exception {
        if (map == null) {
            return null;
        }
        Object obj = beanClass.newInstance();

        BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor property : propertyDescriptors) {
            Method setter = property.getWriteMethod();
            if (setter != null) {
                setter.invoke(obj, map.get(property.getName()));
            }
        }

        return obj;
    }

    public static Map<String, Object> objectToMap(Object obj) throws Exception {
        if (obj == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<String, Object>();

        BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor property : propertyDescriptors) {
            String key = property.getName();
            if (key.compareToIgnoreCase("class") == 0) {
                continue;
            }
            Method getter = property.getReadMethod();
            Object value = getter != null ? getter.invoke(obj) : null;
            map.put(key, value);
        }

        return map;
    }

}
