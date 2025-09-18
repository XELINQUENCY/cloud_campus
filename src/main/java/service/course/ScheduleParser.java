package service.course;

import java.util.HashSet;
import java.util.Set;

/**
 * 课程时间安排字符串的解析工具类。
 * 能够处理如 "1-16", "1,3,5-9", "3-4" 等格式。
 */
public class ScheduleParser {

    /**
     * 解析包含逗号和连字符的范围字符串，返回一个包含所有独立数字的集合。
     * 示例:
     * <ul>
     * <li>"1-8" -> {1, 2, 3, 4, 5, 6, 7, 8}</li>
     * <li>"1,3,5-7" -> {1, 3, 5, 6, 7}</li>
     * <li>"9" -> {9}</li>
     * </ul>
     * @param rangeString 代表范围的字符串 (如周次 "weeks" 或节次 "class_period")
     * @return 包含所有解析出的整数的 Set 集合。如果输入为空或格式错误，则返回空集合。
     */
    public static Set<Integer> parseRangeString(String rangeString) {
        Set<Integer> resultSet = new HashSet<>();
        if (rangeString == null || rangeString.trim().isEmpty()) {
            return resultSet;
        }

        // 1. 按逗号分割，以处理 "1,3,5-7" 这样的混合格式
        String[] parts = rangeString.split(",");

        for (String part : parts) {
            part = part.trim();
            // 2. 检查每个部分是否包含连字符，以确定是范围还是单个数字
            if (part.contains("-")) {
                String[] rangeEnds = part.split("-");
                if (rangeEnds.length == 2) {
                    try {
                        int start = Integer.parseInt(rangeEnds[0]);
                        int end = Integer.parseInt(rangeEnds[1]);
                        // 3. 将范围内的所有数字添加到集合中
                        for (int i = start; i <= end; i++) {
                            resultSet.add(i);
                        }
                    } catch (NumberFormatException e) {
                        // 忽略格式错误的部分，并在控制台打印错误信息
                        System.err.println("时间解析错误：无法解析范围 '" + part + "'");
                    }
                }
            } else {
                try {
                    // 4. 将单个数字添加到集合中
                    resultSet.add(Integer.parseInt(part));
                } catch (NumberFormatException e) {
                    System.err.println("时间解析错误：无法解析数字 '" + part + "'");
                }
            }
        }
        return resultSet;
    }
}