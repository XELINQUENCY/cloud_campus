package DAO;

import entity.BorrowRecord;
import mapper.BorrowRecordMapper;

import java.time.LocalDateTime;
import java.util.List;

public class BorrowRecordDAO {
    public int insert(BorrowRecord record){
        return MyBatisUtil.executeUpdate(BorrowRecordMapper.class, mapper->mapper.insert(record));
    }

    public int updateReturnInfo(String recordId, LocalDateTime overdueDate){
        return MyBatisUtil.executeUpdate(BorrowRecordMapper.class, mapper->mapper.updateReturnInfo(recordId, overdueDate));
    }

    public List<BorrowRecord> selectBorrowRecordByReaderId(String readerId){
        return MyBatisUtil.executeQuery(BorrowRecordMapper.class, mapper->mapper.findActiveRecordByReaderId(readerId));
    }

    public List<BorrowRecord> selectByReaderId(String readerId){
        return MyBatisUtil.executeQuery(BorrowRecordMapper.class, mapper->mapper.findActiveRecordByReaderId(readerId));
    }
}
