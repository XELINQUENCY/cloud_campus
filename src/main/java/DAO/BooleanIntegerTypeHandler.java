package DAO;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 自定义 MyBatis TypeHandler，用于在 Java 的 Boolean 类型和数据库的 Integer 类型之间转换。
 * - true  <=> 1
 * - false <=> 0
 */
@MappedTypes(Boolean.class)
public class BooleanIntegerTypeHandler extends BaseTypeHandler<Boolean> {

    /**
     * 将 Java 的 Boolean 类型转换为数据库的 Integer 类型，用于设置 PreparedStatement 的参数。
     * @param ps PreparedStatement 对象
     * @param i 参数索引
     * @param parameter 要设置的 boolean 值
     * @param jdbcType JDBC 类型
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Boolean parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter ? 1 : 0);
    }

    /**
     * 从 ResultSet 中根据列名获取 Integer 值，并转换为 Boolean。
     * @param rs ResultSet 对象
     * @param columnName 列名
     * @return 转换后的 boolean 值
     */
    @Override
    public Boolean getNullableResult(ResultSet rs, String columnName) throws SQLException {
        // 如果数据库中的值为 0，则为 false，否则为 true。
        return rs.getInt(columnName) != 0;
    }

    /**
     * 从 ResultSet 中根据列索引获取 Integer 值，并转换为 Boolean。
     * @param rs ResultSet 对象
     * @param columnIndex 列索引
     * @return 转换后的 boolean 值
     */
    @Override
    public Boolean getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return rs.getInt(columnIndex) != 0;
    }

    /**
     * 从 CallableStatement 中获取 Integer 值，并转换为 Boolean (用于存储过程)。
     * @param cs CallableStatement 对象
     * @param columnIndex 列索引
     * @return 转换后的 boolean 值
     */
    @Override
    public Boolean getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return cs.getInt(columnIndex) != 0;
    }
}

