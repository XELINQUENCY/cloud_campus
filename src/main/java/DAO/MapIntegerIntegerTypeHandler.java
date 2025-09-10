package DAO;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.lang.reflect.Type;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

@MappedTypes(Map.class)
@MappedJdbcTypes(JdbcType.CHAR)
public class MapIntegerIntegerTypeHandler extends BaseTypeHandler<Map<Integer, Integer>> {

    // Gson 实例是线程安全的，可以作为静态实例共享
    private static final Gson gson = new Gson();

    // 定义需要转换的 Map 的具体类型，以便 Gson 反序列化时使用
    private static final Type MAP_TYPE = new TypeToken<Map<Integer, Integer>>() {}.getType();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Map<Integer, Integer> parameter, JdbcType jdbcType) throws SQLException {
        // 使用 gson.toJson 将 Map 转换为 JSON 字符串
        ps.setString(i, gson.toJson(parameter));
    }

    @Override
    public Map<Integer, Integer> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
        return parseJson(json);
    }

    @Override
    public Map<Integer, Integer> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String json = rs.getString(columnIndex);
        return parseJson(json);
    }

    @Override
    public Map<Integer, Integer> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String json = cs.getString(columnIndex);
        return parseJson(json);
    }

    private Map<Integer, Integer> parseJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return Collections.emptyMap(); // 或者返回 null
        }
        // 使用 gson.fromJson 将 JSON 字符串转换回 Map 对象
        // 必须传入我们预先定义的 MAP_TYPE 来保留泛型信息
        return gson.fromJson(json, MAP_TYPE);
    }
}