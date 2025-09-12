package service;

import entity.User;

/**
 * 认证服务接口
 * 负责处理用户登录、登出和令牌管理。
 */
public interface AuthService {

    /**
     * 用户登录验证。
     * @param username 用户名
     * @param password 密码
     * @param isAdmin  是否尝试作为管理员登录
     * @return 登录成功返回完整的User对象
     * @throws Exception 如果认证失败（用户名不存在、密码错误、权限不足）
     */
    User login(String username, String password, boolean isAdmin) throws Exception;

    /**
     * 为指定用户生成一个认证令牌。
     * @param user 已认证的用户对象
     * @return 代表该用户会话的令牌字符串
     */
    String generateToken(User user);

    /**
     * 验证一个令牌的有效性。
     * @param token 客户端传来的令牌
     * @return 如果令牌有效，返回令牌中包含的用户ID
     * @throws Exception 如果令牌无效、过期或被篡改
     */
    String validateToken(String token) throws Exception;
}
