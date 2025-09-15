package DAO; // 确保包名正确

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset; // 使用UTC时区进行转换

@MappedTypes(LocalDateTime.class)
public class LocalDateTimeToLongTypeHandler extends BaseTypeHandler<LocalDateTime> {

    // 假定存储和读取都基于UTC时区，避免时区问题
    private static final ZoneOffset ZONE_OFFSET = ZoneOffset.UTC;

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, LocalDateTime parameter, JdbcType jdbcType) throws SQLException {
        // 将 LocalDateTime 转换为长整型的秒级时间戳并存入数据库
        ps.setLong(i, parameter.toEpochSecond(ZONE_OFFSET));
    }

    @Override
    public LocalDateTime getNullableResult(ResultSet rs, String columnName) throws SQLException {
        // 从数据库读取长整型时间戳
        long epochSecond = rs.getLong(columnName);
        // 如果 rs.getLong() 返回0且数据库中实际为NULL，rs.wasNull()会是true
        return epochSecond == 0 && rs.wasNull() ? null : LocalDateTime.ofEpochSecond(epochSecond, 0, ZONE_OFFSET);
    }

    @Override
    public LocalDateTime getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        long epochSecond = rs.getLong(columnIndex);
        return epochSecond == 0 && rs.wasNull() ? null : LocalDateTime.ofEpochSecond(epochSecond, 0, ZONE_OFFSET);
    }

    @Override
    public LocalDateTime getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        long epochSecond = cs.getLong(columnIndex);
        return epochSecond == 0 && cs.wasNull() ? null : LocalDateTime.ofEpochSecond(epochSecond, 0, ZONE_OFFSET);
    }
}