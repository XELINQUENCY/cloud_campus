package DAO;

import entity.ElectiveTime;
import mapper.ElectiveTimeMapper;

public class ElectiveTimeDAO {
    public int insertOrUpdate(ElectiveTime electiveTime) {
        ElectiveTime res = MyBatisUtil.executeQuery(ElectiveTimeMapper.class, ElectiveTimeMapper::findCurrent);
        if (res != null) {
            return MyBatisUtil.executeUpdate(ElectiveTimeMapper.class, mapper->mapper.update(electiveTime));
        }else{
            return MyBatisUtil.executeUpdate(ElectiveTimeMapper.class, mapper->mapper.insert(electiveTime));
        }
    }

    public ElectiveTime getCurrentElectiveTime(){
        return MyBatisUtil.executeQuery(ElectiveTimeMapper.class, ElectiveTimeMapper::findCurrent);
    }
}
