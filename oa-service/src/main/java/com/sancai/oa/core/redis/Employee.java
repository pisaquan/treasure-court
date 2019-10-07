package com.sancai.oa.core.redis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author wangyl
 * @create 2019/7/24 09:39
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee implements Serializable {
	private static final long serialVersionUID = 1748441780826308444L;

	private Integer id;
	private String lastName;
	private String email;
	/**
	 * 性别 1男  0女
	 */
	private Integer gender;
	private Integer dId;
}
