package mapper;

import entity.ElectiveTime;

public interface ElectiveTimeMapper {
    /**
     * 获取当前生效的选课时间配置 [cite: 477]
     * @return 选课时间配置对象
     */
    ElectiveTime findCurrent();

    /**
     * (管理员) 更新选课时间配置
     * @param electiveTime 新的配置对象
     * @return 受影响的行数
     */
    int update(ElectiveTime electiveTime);

    /**
     * 新增一个选课时间配置
     * @param electiveTime 要插入的配置对象
     * @return 受影响的行数
     */
    int insert(ElectiveTime electiveTime);
}
