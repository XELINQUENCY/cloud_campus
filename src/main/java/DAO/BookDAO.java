package DAO;

import entity.Book;
import entity.BookQueryCriteria;
import enums.BookStatus;
import mapper.BookMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public class BookDAO {
    public Book findById(@Param("bookId")String bookId){
        return MyBatisUtil.executeQuery(BookMapper.class, mapper->mapper.findById(bookId));
    }

    public List<Book> searchBooks(BookQueryCriteria criteria){
        return MyBatisUtil.executeQuery(BookMapper.class, mapper->mapper.searchBooks(criteria));
    }

    public int updateStatus(@Param("bookId")String bookId, @Param("status") BookStatus status){
        return MyBatisUtil.executeUpdate(BookMapper.class, mapper->mapper.updateStatus(bookId,status));
    }

    public int insert(Book book){
        return MyBatisUtil.executeUpdate(BookMapper.class, mapper->mapper.insert(book));
    }

    public int update(Book book){
        return MyBatisUtil.executeUpdate(BookMapper.class, mapper->mapper.update(book));
    }
}
