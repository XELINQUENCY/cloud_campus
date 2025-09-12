package DAO.library;

import DAO.MyBatisUtil;
import entity.library.LibraryProfile;
import mapper.LibraryProfileMapper;

import java.util.List;

/**
 * 图书馆用户档案数据访问对象 (LibraryProfileDAO)
 * 负责封装所有对 'library_profiles' 数据表的数据库操作。
 * 完全使用 MyBatis 实现，并通过 MyBatisUtil 执行。
 */
public class LibraryProfileDAO {

    /**
     * 根据主系统的用户ID查找图书馆用户档案。
     * @param mainUserId 主系统用户ID
     * @return LibraryProfile 对象或 null
     */
    public LibraryProfile findProfileByMainUserId(String mainUserId) {
        return MyBatisUtil.executeQuery(
                LibraryProfileMapper.class,
                mapper -> mapper.findByMainUserId(mainUserId)
        );
    }

    /**
     * 查找所有用户的图书馆档案。
     * @return LibraryProfile 对象的列表
     */
    public List<LibraryProfile> findAllProfiles() {
        return MyBatisUtil.executeQuery(
                LibraryProfileMapper.class,
                LibraryProfileMapper::findAll // 使用方法引用，更简洁
        );
    }

    /**
     * 创建一个新的图书馆用户档案。
     * @param profile 要创建的档案对象
     * @return 创建成功返回 true，失败返回 false
     */
    public boolean createProfile(LibraryProfile profile) {
        int result = MyBatisUtil.executeUpdate(
                LibraryProfileMapper.class,
                mapper -> mapper.insert(profile)
        );
        return result > 0;
    }

    /**
     * 更新图书馆用户档案 (通常是更新罚款和逾期状态)。
     * @param profile 包含更新信息的档案对象
     * @return 更新成功返回 true，失败返回 false
     */
    public boolean updateProfile(LibraryProfile profile) {
        int result = MyBatisUtil.executeUpdate(
                LibraryProfileMapper.class,
                mapper -> mapper.update(profile)
        );
        return result > 0;
    }

    /**
     * 根据主系统用户ID删除一个图书馆档案。
     * @param mainUserId 主系统用户ID
     * @return 删除成功返回 true，失败返回 false
     */
    public boolean deleteProfileByMainUserId(String mainUserId) {
        int result = MyBatisUtil.executeUpdate(
                LibraryProfileMapper.class,
                mapper -> mapper.deleteByMainUserId(mainUserId)
        );
        return result > 0;
    }
}
