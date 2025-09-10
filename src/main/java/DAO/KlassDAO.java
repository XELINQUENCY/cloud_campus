package DAO;

import entity.Klass;
import mapper.KlassMapper;

import java.util.List;

public class KlassDAO {
    public List<Klass> selectAll() {
        return MyBatisUtil.executeQuery(KlassMapper.class, KlassMapper::findAll);
    }

    public int insert(Klass klass) {
        return MyBatisUtil.executeUpdate(KlassMapper.class, mapper -> mapper.insert(klass));
    }

    public int update(Klass klass) {
        return MyBatisUtil.executeUpdate(KlassMapper.class, mapper -> mapper.update(klass));
    }

    public int delete(String classId) {
        return MyBatisUtil.executeUpdate(KlassMapper.class, mapper->mapper.delete(classId));
    }

}
