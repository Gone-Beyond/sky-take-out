package com.sky.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 后端统一返回结果对象
 *
 * <p>
 * 作用：
 * 1. 统一后端接口返回给前端的数据格式；
 * 2. 前端可以根据 code 判断请求是否成功；
 * 3. 如果成功，可以从 data 中取业务数据；
 * 4. 如果失败，可以从 msg 中取错误提示信息。
 * </p>
 *
 * @param <T> 返回数据的类型，例如 EmployeeLoginVO、PageResult、List<EmployeeVO> 等
 */
@Data
public class Result<T> implements Serializable {

    /**
     * 响应状态码
     *
     * 约定：
     * 1 表示成功；
     * 0 或其他数字表示失败。
     */
    private Integer code;

    /**
     * 错误提示信息
     *
     * 成功时一般可以为空；
     * 失败时用于告诉前端具体失败原因，例如“用户名已存在”“账号不存在”等。
     */
    private String msg;

    /**
     * 返回给前端的业务数据
     *
     * 例如：
     * 登录成功时返回 token、用户信息；
     * 分页查询时返回 PageResult；
     * 普通新增、删除、修改成功时可以不返回数据。
     */
    private T data;

    /**
     * 返回成功结果，不携带业务数据
     *
     * <p>
     * 常用于新增、修改、删除这类接口。
     * 这些接口只需要告诉前端“操作成功”，不一定需要返回具体数据。
     * </p>
     *
     * @param <T> 返回数据类型
     * @return 成功结果对象
     */
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();

        // code = 1 表示本次请求处理成功
        result.code = 1;

        return result;
    }

    /**
     * 返回成功结果，并携带业务数据
     *
     * <p>
     * 常用于查询、登录这类接口。
     * 例如登录成功后，需要把 token、用户 id、用户名等数据返回给前端。
     * </p>
     *
     * @param object 需要返回给前端的数据
     * @param <T> 返回数据类型
     * @return 携带数据的成功结果对象
     */
    public static <T> Result<T> success(T object) {
        Result<T> result = new Result<>();

        // 保存真正要返回给前端的业务数据
        result.data = object;

        // code = 1 表示本次请求处理成功
        result.code = 1;

        return result;
    }

    /**
     * 返回失败结果，并携带错误提示信息
     *
     * <p>
     * 常用于业务异常、参数错误、登录失败、用户名重复等场景。
     * 前端可以根据 msg 展示错误提示。
     * </p>
     *
     * @param msg 错误提示信息
     * @param <T> 返回数据类型
     * @return 失败结果对象
     */
    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();

        // 保存错误提示信息，返回给前端展示
        result.msg = msg;

        // code = 0 表示本次请求处理失败
        result.code = 0;

        return result;
    }
}