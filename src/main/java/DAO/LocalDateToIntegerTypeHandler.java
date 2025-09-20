package DAO; // 确保这个包名与您的项目结构一致

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.*;
import java.time.LocalDate;

@MappedTypes(LocalDate.class)
public class LocalDateToIntegerTypeHandler extends BaseTypeHandler<LocalDate> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, LocalDate parameter, JdbcType jdbcType) throws SQLException {
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
        return epochDay == 0 && rs.wasNull() ? null : LocalDate.ofEpochDay(epochDay);
    }

    @Override
    public LocalDate getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        // 从存储过程出参中读取长整型的 epoch day
        long epochDay = cs.getLong(columnIndex);
        return epochDay == 0 && cs.wasNull() ? null : LocalDate.ofEpochDay(epochDay);
    }
}