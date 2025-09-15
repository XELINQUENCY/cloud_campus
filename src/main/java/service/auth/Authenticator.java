// 创建新接口: service.auth.Authenticator.java
package service.auth;

import dto.LoginRequest;
import entity.User;

public interface Authenticator {
    /**
     * 判断此认证器是否支持处理给定的登录请求
     * @param request 登录请求
     * @return 如果支持则返回 true
     */
    boolean supports(LoginRequest request);

    /**
     * 执行认证
     * @param request 登录请求
     * @return 认证成功后的 User 对象
     * @throws Exception 认证失败时抛出异常
     */
    User authenticate(LoginRequest request) throws Exception;
}