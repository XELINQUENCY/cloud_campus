package service.impl;

import dto.LoginRequest;
import service.auth.Authenticator; // 引入接口
import java.util.List;

import entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import service.AuthService;

import javax.crypto.SecretKey;
import java.util.Date;

import static io.jsonwebtoken.Jwts.builder;

/**
 * 认证服务实现类 (使用JWT)
 */
public class AuthServiceImpl implements AuthService {

    private final List<Authenticator> authenticators; // 持有策略列表
    // 使用一个安全的密钥，实际项目中应从配置文件加载
    private static final SecretKey JWT_SECRET_KEY = Jwts.SIG.HS256.key().build();
    private static final long EXPIRATION_TIME = 864_000_000; // 10 days

    public AuthServiceImpl(List<Authenticator> authenticators) {
        this.authenticators = authenticators;
    }

    @Override
    public User login(String username, String password, boolean isAdmin) throws Exception {
        LoginRequest request = new LoginRequest(username, password, isAdmin);

        // --- 核心修改 ---
        // 遍历所有注入的认证器
        for (Authenticator auth : authenticators) {
            // 检查当前认证器是否支持处理此类型的请求
            if (auth.supports(request)) {
                User user = auth.authenticate(request);
                if (user != null) {
                    return user; // 只要有一个认证器成功，就立即返回结果
                }
            }
        }
        // 遍历完所有支持的认证器都无法认证（例如，用户存在但密码错误是在 authenticator 内部抛出异常）
        // 如果循环正常结束，意味着没有找到对应的用户
        throw new Exception("用户名不存在或模块不匹配。");
    }

    @Override
    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

        return builder().subject(user.getId()).issuedAt(now).expiration(expiryDate)
                .signWith(JWT_SECRET_KEY)
                .compact();
    }

    @Override
    public String validateToken(String token) throws Exception {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(JWT_SECRET_KEY)
                    .build().parseSignedClaims(token).getPayload();

            return claims.getSubject(); // 返回用户ID
        } catch (Exception e) {
            throw new Exception("无效或已过期的令牌。", e);
        }
    }
}
