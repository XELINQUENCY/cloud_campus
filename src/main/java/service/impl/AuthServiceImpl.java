package service.impl;

import DAO.UserDAO;
import entity.User;
import enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import service.AuthService;

import java.security.Key;
import java.util.Date;

/**
 * 认证服务实现类 (使用JWT)
 */
public class AuthServiceImpl implements AuthService {

    private final UserDAO userDAO;
    // 使用一个安全的密钥，实际项目中应从配置文件加载
    private static final Key JWT_SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private static final long EXPIRATION_TIME = 864_000_000; // 10 days

    public AuthServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public User login(String username, String password, boolean isAdmin) throws Exception {
        User user = userDAO.findByNameForAuth(username);

        if (user == null) {
            throw new Exception("用户名不存在。");
        }

        if (!password.equals(user.getPassword())) {
            throw new Exception("密码错误。");
        }

        boolean hasRequiredRole = isAdmin ? user.hasRole(UserRole.LIBRARIAN) : user.hasRole(UserRole.READER);
        if (!hasRequiredRole) {
            throw new Exception("权限不足。");
        }

        return user;
    }

    @Override
    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .setSubject(user.getId()) // 将用户ID作为主题
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(JWT_SECRET_KEY)
                .compact();
    }

    @Override
    public String validateToken(String token) throws Exception {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(JWT_SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getSubject(); // 返回用户ID
        } catch (Exception e) {
            throw new Exception("无效或已过期的令牌。", e);
        }
    }
}
