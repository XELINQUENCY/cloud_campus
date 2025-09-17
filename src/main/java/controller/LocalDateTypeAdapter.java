package controller; // 保持与您项目结构一致的包名

import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.LocalDate; // 引入 LocalDate
import java.time.format.DateTimeFormatter;

public class LocalDateTypeAdapter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {

    // 1. 关键改动：使用 ISO_LOCAL_DATE 格式化器，它专门用于处理没有时间的日期。
    // 例如："2025-09-17"
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * 序列化方法：将 LocalDate 对象转换为 JSON 字符串
     */
    @Override
    public JsonElement serialize(LocalDate localDate, Type srcType,
                                 JsonSerializationContext context) {
        // 使用新的格式化器进行格式化
        return new JsonPrimitive(formatter.format(localDate));
    }

    /**
     * 反序列化方法：将 JSON 字符串转换为 LocalDate 对象
     */
    @Override
    public LocalDate deserialize(JsonElement json, Type typeOfT,
                                 JsonDeserializationContext context) throws JsonParseException {
        // 使用新的格式化器进行解析
        return LocalDate.parse(json.getAsString(), formatter);
    }
}