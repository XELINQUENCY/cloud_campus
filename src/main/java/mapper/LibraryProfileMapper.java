package mapper;

import entity.library.LibraryProfile;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * MyBatis Mapper 接口，用于操作 library_profiles 表。
 * 它取代了旧的 UserDAO 中与图书馆用户业务相关的部分。
 */
public interface LibraryProfileMapper {

    /**
     * 根据主系统的用户ID查找图书馆用户档案。
     * @param mainUserId 主系统中的用户唯一ID (通常是UUID或字符串)
     * @return 找到则返回 LibraryProfile 对象，否则返回 null。
     */
    LibraryProfile findByMainUserId(@Param("mainUserId") String mainUserId);

    /**
     * 根据图书馆档案的自增ID查找。
     * @param profileId library_profiles 表的主键ID
     * @return 找到则返回 LibraryProfile 对象，否则返回 null。
     */
    LibraryProfile findById(@Param("profileId") int profileId);

    /**
     * 查找所有用户的图书馆档案。
     * @return 包含所有 LibraryProfile 对象的列表。
     */
    List<LibraryProfile> findAll();

    /**
     * 为一个主系统用户创建一个新的图书馆档案。
     * @param profile 要添加的 LibraryProfile 对象，需要设置好 mainUserId。
     * @return 成功插入的行数 (通常是1)。
     */
    int insert(LibraryProfile profile);

    /**
     * 更新一个用户的图书馆档案信息 (例如：逾期状态和罚款金额)。
     * @param profile 包含需要更新信息的 LibraryProfile 对象。
     * @return 成功更新的行数 (通常是1)。
     */
    int update(LibraryProfile profile);

    /**
     * 根据主系统的用户ID删除一个图书馆档案。
     * @param mainUserId 要删除档案的主系统用户ID。
     * @return 成功删除的行数 (通常是1)。
     */
    int deleteByMainUserId(@Param("mainUserId") String mainUserId);
}
