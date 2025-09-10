package DAO;

import enums.common.DisplayNameEnum;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.lang.reflect.Method;

// 使用泛型 E，并约束 E 必须是实现了 DisplayNameEnum 接口的枚举类型
public class EnumTypeHandler<E extends Enum<E> & DisplayNameEnum> extends BaseTypeHandler<E> {

    private final Class<E> type;

    // 构造函数，MyBatis在实例化TypeHandler时会传入具体的枚举类Class对象
    public EnumTypeHandler(Class<E> type) {
        if (type == null) {
            throw new IllegalArgumentException("Type argument cannot be null");
        }
        this.type = type;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType) throws SQLException {
        // 因为 E 实现了 DisplayNameEnum，所以可以直接调用 getDisplayName()
        ps.setString(i, parameter.getDisplayName());
    }

    @Override
    public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String dbValue = rs.getString(columnName);
        return dbValue == null ? null : fromDisplayName(dbValue);
    }

    @Override
    public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String dbValue = rs.getString(columnIndex);
        return dbValue == null ? null : fromDisplayName(dbValue);
    }

    @Override
    public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String dbValue = cs.getString(columnIndex);
        return dbValue == null ? null : fromDisplayName(dbValue);
    }

    /**
     * 使用反射调用枚举的静态 fromDisplayName 方法。
     * @param displayName 数据库中的值
     * @return 对应的枚举实例
     */
    private E fromDisplayName(String displayName) {
        try {
            // 通过反射获取静态方法 fromDisplayName(String)
            Method fromMethod = type.getMethod("fromDisplayName", String.class);
            // 调用静态方法 (第一个参数为 null)
            return (E) fromMethod.invoke(null, displayName);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot convert " + displayName + " to " + type.getSimpleName(), e);
        }
    }
}