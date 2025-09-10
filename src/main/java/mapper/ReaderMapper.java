package mapper;

import entity.Reader;
import org.apache.ibatis.annotations.Param;

public interface ReaderMapper {
    /**
     * 根据读者ID查找读者（用于登录验证）
     * @param readerId 读者的唯一编号
     * @return 找到的读者对象，未找到则返回null
     */
    Reader findById(@Param("readerId") String readerId);

    /**
     * 新增一个读者（用于注册）
     * @param reader 要新增的读者对象
     * @return 受影响的行数
     */
    int insert(Reader reader);

    /**
     * 更新读者的逾期状态
     * @param reader 要更新的读者
     * @return 受影响的行数
     */
    int updateOverdueStatus(Reader reader);

}
