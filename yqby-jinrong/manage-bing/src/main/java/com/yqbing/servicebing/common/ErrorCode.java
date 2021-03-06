/**
 * Copyright 2018 tties Inc.
 */
package com.yqbing.servicebing.common;

import java.util.HashMap;

/**
 * 编号规则 ：Action编号+0001.
 * Staff 编号101.
 * @author wangxu
 *
 */
public class ErrorCode {

    /**
     * 错误代码名称.
     */
    public static final String ERROR_CODE = "errorCode";
    /**
     * 错误信息名称.
     */
    public static final String ERROR_MESSAGE = "errorMessage";

    /**************系统错误编码***************/
    /** 成功. */
    public static final int SUCCESS = 0;
    /** 认证失败. */
    public static final int AUTH_FAIL = 1000;
    /** 请求参数错误. */
    public static final int PARAM_ERROR = 1001;
    /** 业务错误(DB). */
    public static final int BUSINESS_ERROR = 1002;
    /** 参数不能为空. */
    public static final int PARAM_NULL = 1003;
    /** 其他错误. */
    public static final int OTHERS = 9999;

    /*****************************************/
    /** account E101 start
     * [101-199]为空判断 . [201-299]字典值判断。 [301-399]字段规则判断 。
     * [401-499]对象不存在判断。[501-599]业务判断。[601-699]其他。
     */
    /*****************************************/
    /** 请重新登录. */
    public static final int E1010101 = 1010101;
    /*****************************************/
    /**
     *  account E101 end
     */
    /*****************************************/

    /********************系统管理账户模块start*********************/
    public static final int E1020101 = 1020101;
    public static final int E1020102 = 1020102;
    /********************系统管理账户模块end***********************/
    public static final int E1020301 = 1020302;
    public static final int E1020303 = 1020303;
    /********************报价start***********************/
    public static final int E1020401 = 1020401;
    /*****************************************/
    public static final int E1020501 = 1020501;
    public static final int E1020601 = 1020601;
    public static final int E1020701 = 1020701;
    public static final int E1020702 = 1020702;
    /** Staff E101 start
     * [101-199]为空判断 . [201-299]字典值判断。 [301-399]字段规则判断 。
     * [401-499]对象不存在判断。[501-599]业务判断。[601-699]其他。
     */
    /*****************************************/
    /*****************************************/
    /**
     *  Staff E101 end
     */
    /*****************************************/

    /********************数据查询 E104 start*********************/
    /**
     * [101-199]为空判断 . [201-299]字典值判断。 [301-399]字段规则判断 。
     * [401-499]对象不存在判断。[501-599]业务判断。[601-699]其他。
     */
    /** 请选择表计 */
    public static final int E1040401 = 1040401;
    /********************数据查询 E104 end***********************/
    
    
    private static HashMap<Integer, String > errorInfo = new HashMap<Integer, String >();

    static {
        /*********** System start ***************/
        errorInfo.put(SUCCESS, "成功");
        errorInfo.put(AUTH_FAIL, "认证失败");
        errorInfo.put(PARAM_ERROR, "请求参数错误");
        errorInfo.put(BUSINESS_ERROR, "业务错误(DB)");
        errorInfo.put(PARAM_NULL, "参数不能为空");
        errorInfo.put(OTHERS, "其他错误");
        /***********System end ***************/

        /*********** account start end ***************/
        errorInfo.put(E1010101, "请重新登录");

        /*********** account E101 end ***************/

        
        /********************系统管理账户模块start*********************/
        errorInfo.put(E1020101, "添加用户异常");
        errorInfo.put(E1020102, "删除用户异常");
        /********************系统管理账户模块end***********************/
        
        errorInfo.put(E1020301, "数据格式不正确");
        errorInfo.put(E1020303, "没有可以发放财富的用户");
        

        /*********** Staff E404 end ***************/
        /*********** 数据查询 start  ***************/
        errorInfo.put(E1020401, "有些数据不能为空,请仔细检查");
        /*********** 数据查询 E104 end ***************/
        errorInfo.put(E1020501, "热门城市以及存在请从新输入");
        
        errorInfo.put(E1020601, "花呗已经发送过财富 ");
        errorInfo.put(E1020701, "费率不能为空");
        errorInfo.put(E1020702, "财富值太低,发送不了");
    }

    /**
     * 获取错误信息.
     * @param errCode String
     * @return String
     */
    public static String getErrInfo(Integer errCode) {
        return errorInfo.get(errCode);
    }

    /**
     * 设置错误信息.
     * @param errCode String
     * @param errMsg String
     */
    public static void setErrInfo(Integer errCode, String errMsg) {
        errorInfo.put(errCode, errMsg);
    }
}
