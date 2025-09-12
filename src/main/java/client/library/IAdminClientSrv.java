package client.library;

import client.ApiException;
import entity.library.Book;

/**
 * 管理员客户端服务接口 (重构版)
 * 移除RMI相关的声明，方法改为抛出ApiException。
 */
public interface IAdminClientSrv {

    boolean addBook(Book book) throws ApiException;

    boolean updateBook(Book book) throws ApiException;
}
