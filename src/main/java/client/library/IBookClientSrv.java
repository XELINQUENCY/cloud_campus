package client.library;

import client.ApiException;
import entity.library.Book;
import entity.library.Category;

import java.util.List;

/**
 * 公共图书服务客户端接口 (重构版)
 * 移除RMI相关的声明，方法改为抛出ApiException。
 */
public interface IBookClientSrv {

    List<Book> searchBooks(String title, String author, String publisher, Integer categoryId) throws ApiException;

    List<Category> getAllCategories() throws ApiException;
}
