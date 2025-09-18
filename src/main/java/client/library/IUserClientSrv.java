package client.library;

import client.ApiException;
import entity.User;
import entity.library.LibraryProfile;
import view.BorrowRecordView;
import view.ReservationView;

import java.util.List;

/**
 * 普通用户客户端服务接口
 * 抛出ApiException。
 */
public interface IUserClientSrv {

    User login(String username, String password, boolean isAdmin) throws ApiException;

    String borrowBook(String userId, int bookId) throws ApiException;

    String returnBook(int copyId) throws ApiException;

    String renewBook(int recordId) throws ApiException;

    String reserveBook(String userId, int bookId) throws ApiException;

    String payFine(String userId, double amount) throws ApiException;

    List<BorrowRecordView> getMyBorrowRecords(String userId) throws ApiException;

    List<ReservationView> getMyReservations(String userId) throws ApiException;

    String cancelReservation(int reservationId) throws ApiException;

    LibraryProfile refreshLibraryProfile(String userId) throws ApiException;
}
