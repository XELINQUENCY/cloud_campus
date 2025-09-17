package DAO; // 确保这个包名与您的项目结构一致

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.*;
import java.time.LocalDate;

/**
 * MyBatis TypeHandler for mapping between {@link java.time.LocalDate} and {@code INTEGER}.
 * <p>
 * This handler converts a {@link LocalDate} to its epoch day (the number of days since 1970-01-01)
 * for storage in an INTEGER column, and vice versa.
 * </p>
 */
@MappedTypes(LocalDate.class)
public class LocalDateToIntegerTypeHandler extends BaseTypeHandler<LocalDate> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, LocalDate parameter, JdbcType jdbcType) throws SQLException {
        // 将 LocalDate 转换为自 1970-01-01 以来的天数（epoch day），并存入数据库
        // toEpochDay() 返回的是 long 类型，但对于实际日期，其值远在 Integer 范围内，
        // 使用 setLong 可以确保安全，因为 SQLite 的 INTEGER 类型可以存储 8 字节长整型。
        ps.setLong(i, parameter.toEpochDay());
    }

    @Override
    public LocalDate getNullableResult(ResultSet rs, String columnName) throws SQLException {
        // 从数据库读取长整型的 epoch day
        long epochDay = rs.getLong(columnName);
        // 如果 rs.getLong() 返回0且数据库中实际为NULL，rs.wasNull()会返回true，这是处理NULL值的关键
        return epochDay == 0 && rs.wasNull() ? null : LocalDate.ofEpochDay(epochDay);
    }

    @Override
    public LocalDate getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        // 从数据库读取长整型的 epoch day
        long epochDay = rs.getLong(columnIndex);
        // 处理NULL值的关键检查
        return epochDay == 0 && rs.wasNull() ? null : LocalDate.ofEpochDay(epochDay);
    }

    @Override
    public LocalDate getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        // 从存储过程出参中读取长整型的 epoch day
        long epochDay = cs.getLong(columnIndex);
        // 处理NULL值的关键检查
        return epochDay == 0 && cs.wasNull() ? null : LocalDate.ofEpochDay(epochDay);
    }
}