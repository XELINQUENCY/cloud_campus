package DAO;

import entity.Reader;
import mapper.ReaderMapper;

public class ReaderDAO {
    public Reader findById(String readerId){
        return MyBatisUtil.executeQuery(ReaderMapper.class, mapper->mapper.findById(readerId));
    }

    public int createReader(Reader reader){
        return MyBatisUtil.executeUpdate(ReaderMapper.class, mapper->mapper.insert(reader));
    }

    public int updateOverdueStatus(Reader reader){
        return MyBatisUtil.executeUpdate(ReaderMapper.class, mapper->mapper.updateOverdueStatus(reader));
    }

}
