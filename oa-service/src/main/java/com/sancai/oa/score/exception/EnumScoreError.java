package com.sancai.oa.score.exception;

import com.sancai.oa.core.exception.OaError;

/**
 * 积分错误枚举
 * @author fanjing
 * @date 2019/8/7
 */
public enum EnumScoreError implements OaError {

    /**
     * 积分类异常 2007
     */
    INSERT_USERSCORE_FAILURE_PARAM_ISNULL(20111001,"用户积分变动参数不能为空"),
    PARAM_SCORE_IS_BELOWSTANDARD(20111002,"分数值不符合规定"),
    INSERT_RECORD_FAILURE(20111003,"对员工积分变动存储异常"),
    UPDATE_SCORERECORD_FAILURE(20111004,"对员工积分变动存储失败"),
    QUERY_PARAM_COMPANYID_IS_EMPTY(20111005,"请求参数公司id不能为空"),
    QUERY_SCORElIST_FAILURE(20111006,"查询积分变动列表失败"),
    QUERY_USERSCORE_FAILURE(20111007,"员工累计总分查询失败"),
    QUERY_DEPT_USERSCORE_FAILURE(20111008,"部门下员工积分列表查询失败"),
    QUERY_USERSCORE_IS_EMPTY(20111009,"员工积分记录为空"),
    QUERY_PARAM_IS_EMPTY(20111010,"请求参数不能为空"),
    QUERY_SCORERULE_FAILURE(20111011,"查询积分规则列表失败"),
    UPDATE_SCORERULE_PARAM_IS_EMPTY(20111012,"积分规则修改参数id为空"),
    UPDATE_SCORERULE_FAILURE(20111013,"修改积分规则失败"),
    QUERY_RULE_FAILURE(20111014,"根据key获取规则失败"),
    QUERY_IS_EMPTY(20111015,"该key对应的规则不存在"),
    INSERT_SCORERECORD_FAILURE(20111016,"日报不合规则扣除积分记录插入失败"),
    INSERT_USERSCORE_FAILURE(20111017,"对员工积分变动失败")
    ;

    private Integer code;
    private String message;
    EnumScoreError(Integer code,String message){
        this.code=code;
        this.message=message;
    }
    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Integer getCode() {
        return code;
    }
}
