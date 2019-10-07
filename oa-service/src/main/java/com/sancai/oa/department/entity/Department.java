package com.sancai.oa.department.entity;


import lombok.Data;

import java.util.List;

/**
 * @Author wangyl
 * @create 2019/7/25 09:14
 */
@Data
public class Department {
	/**
	 * 部门id
	 */
	private String id;
	/**
	 * 上级部门id
	 */
	private String parentid;
	/**
	 * 部门层级
	 */
	private Long level;
	/**
	 *  部门名称
	 */
	private String name;
}
