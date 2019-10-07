package com.sancai.oa.examine.utils;

import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * @author fanjing
 * @date 2019/8/5
 * 此工具类是为了解决PageHelper插件对查询出来的原始list转换携带分页信息，但对原始数据的list进行转换后，则分页信息就获取不到的问题
 * @param <T>
 */
public class MyPageHelper<T> {

    //结果集
    private List<T> list;

    public MyPageHelper(List<T> list){
        this.list = list;
    }
    public PageInfo newPage(PageInfo old ){
        PageInfo<T> pageInfo = new PageInfo<>();
        pageInfo.setList(list);
        pageInfo.setPageNum(old.getPageNum());
        pageInfo.setTotal(old.getTotal());
        pageInfo.setPageSize(old.getPageSize());
        pageInfo.setSize(old.getSize());
        pageInfo.setEndRow(old.getEndRow());
        pageInfo.setStartRow(old.getStartRow());
        pageInfo.setPages(old.getPages());
        pageInfo.setPrePage(old.getPrePage());
        pageInfo.setNextPage(old.getNextPage());
        pageInfo.setIsFirstPage(old.isIsFirstPage());
        pageInfo.setIsLastPage(old.isIsLastPage());
        pageInfo.setHasPreviousPage(old.isHasPreviousPage());
        pageInfo.setHasNextPage(old.isHasNextPage());
        pageInfo.setNavigatePages(old.getNavigatePages());
        pageInfo.setNavigatepageNums(old.getNavigatepageNums());
        pageInfo.setNavigateFirstPage(old.getNavigateFirstPage());
        pageInfo.setNavigateLastPage(old.getNavigateLastPage());
        return pageInfo;
    }
    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}
