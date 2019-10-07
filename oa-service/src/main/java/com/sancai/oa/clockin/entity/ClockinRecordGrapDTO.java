package com.sancai.oa.clockin.entity;

import com.sancai.oa.user.entity.UserDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 抓取的打卡记录
 * </p>
 *
 * @author fans
 * @since 2019-08-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ClockinRecordGrapDTO {

    private static final long serialVersionUID=1L;

    /**
     * 用户
     */
    private Map<String, UserDTO> userMap;

    /**
     * 用户的打卡抓取数据
     */
    private Map<String, Map<Long, List<ClockinPoint>>> clockinMap;


}
