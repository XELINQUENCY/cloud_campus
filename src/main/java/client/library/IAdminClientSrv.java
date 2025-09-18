package client.library;

import client.ApiException;
import entity.library.Book;

/**
 * 管理员客户端服务接口
 * 方法抛出ApiException。
 */
public interface IAdminClientSrv {

    boolean addBook(Book book) throws ApiException;

    boolean updateBook(Book book) throws ApiException;
}
