package com.sancai.oa.report.entity.modify;



import java.util.HashMap;

/**
 * ClassName: DataMap
 *
 * @Description: 封装Map，
 * @date 2019-07-29
 */
public class DataMap extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;

    public static DataMap getInstance() {
        return new DataMap();
    }
   @Override
    public DataMap put(String key, Object value) {
        super.put(key, value);
        return this;
    }
    @Override
    public Object get(Object key) {
        Object value = super.get(key);
        if(value == null){
            return  super.put((String) key,null);
        }
        return value;
    }
}
