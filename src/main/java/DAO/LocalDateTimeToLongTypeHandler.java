package DAO; // 确保包名正确

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset; // 使用UTC时区进行转换

@MappedTypes(LocalDateTime.class)
public class LocalDateTimeToLongTypeHandler extends BaseTypeHandler<LocalDateTime> {

    private static final ZoneOffset ZONE_OFFSET = ZoneOffset.UTC;

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, LocalDateTime parameter, JdbcType jdbcType) throws SQLException {
        ps.setLong(i, parameter.toEpochSecond(ZONE_OFFSET));
    }

    @Override
    public LocalDateTime getNullableResult(ResultSet rs, String columnName) throws SQLException {
        long epochSecond = rs.getLong(columnName);
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