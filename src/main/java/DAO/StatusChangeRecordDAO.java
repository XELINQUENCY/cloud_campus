package DAO;

import entity.StatusChangeRecord;
import mapper.StatusChangeRecordMapper;

import java.util.List;

public class StatusChangeRecordDAO {
    public int insert(StatusChangeRecord record) {
        return MyBatisUtil.executeUpdate(StatusChangeRecordMapper.class, mapper->mapper.insert(record));
    }

    public List<StatusChangeRecord> fintByStudentId(String studentId) {
        return MyBatisUtil.executeQuery(StatusChangeRecordMapper.class, mapper->mapper.fintByStudentId(studentId));
    }
}
