package client.library;

import client.ApiClient;
import client.ApiException;
import com.google.gson.reflect.TypeToken;
import dto.LoginRequest;
import dto.LoginResponse;
import entity.User;
import entity.library.Book;
import entity.library.Category;
import entity.library.LibraryProfile;
import view.BorrowRecordView;
import view.ReservationView;

import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 图书馆模块的专用客户端 (重构版)
 * 实现了所有与图书馆相关的客户端服务接口。
 * 内部使用核心ApiClient来发送HTTPS请求，替代了原有的RMI实现。
 */
public class LibraryClient implements IUserClientSrv, IAdminClientSrv, IBookClientSrv {

    private final ApiClient apiClient;

    public LibraryClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    //<editor-fold desc="IUserClientSrv 接口实现">
    @Override
    public User login(String username, String password, boolean isAdmin) throws ApiException {
        LoginRequest loginRequest = new LoginRequest(username, password, isAdmin);
        HttpRequest request = apiClient.newRequestBuilder("/auth/login")
                .POST(HttpRequest.BodyPublishers.ofString(apiClient.getGson().toJson(loginRequest)))
                .build();

        LoginResponse response = apiClient.sendRequest(request, LoginResponse.class);

        if (response != null && response.getToken() != null) {
            apiClient.setAuthToken(response.getToken()); // 登录成功后，在核心客户端中设置令牌
            return response.getUser();
        }
        throw new ApiException("登录失败，服务器未返回有效数据。");
    }

    @Override
    public String borrowBook(String userId, int bookId) throws ApiException {
        Map<String, Object> body = Map.of("bookId", bookId); // userId从token中获取，无需传递
        HttpRequest request = apiClient.newRequestBuilder("/library/user/borrows")
                .POST(HttpRequest.BodyPublishers.ofString(apiClient.getGson().toJson(body)))
                .build();
        Map<String, String> response = apiClient.sendRequest(request, new TypeToken<Map<String, String>>() {}.getType());
        return response.get("message");
    }

    @Override
    public String returnBook(int copyId) throws ApiException {
        Map<String, Object> body = Map.of("copyId", copyId);
        HttpRequest request = apiClient.newRequestBuilder("/library/user/returns")
                .POST(HttpRequest.BodyPublishers.ofString(apiClient.getGson().toJson(body)))
                .build();
        Map<String, String> response = apiClient.sendRequest(request, new TypeToken<Map<String, String>>() {}.getType());
        return response.get("message");
    }

    @Override
    public String renewBook(int recordId) throws ApiException {
        Map<String, Object> body = Map.of("recordId", recordId);
        HttpRequest request = apiClient.newRequestBuilder("/library/user/renews")
                .POST(HttpRequest.BodyPublishers.ofString(apiClient.getGson().toJson(body)))
                .build();
        Map<String, String> response = apiClient.sendRequest(request, new TypeToken<Map<String, String>>() {}.getType());
        return response.get("message");
    }

    @Override
    public String reserveBook(String userId, int bookId) throws ApiException {
        Map<String, Object> body = Map.of("bookId", bookId);
        HttpRequest request = apiClient.newRequestBuilder("/library/user/reservations")
                .POST(HttpRequest.BodyPublishers.ofString(apiClient.getGson().toJson(body)))
                .build();
        Map<String, String> response = apiClient.sendRequest(request, new TypeToken<Map<String, String>>() {}.getType());
        return response.get("message");
    }

    @Override
    public String payFine(String userId, double amount) throws ApiException {
        Map<String, Object> body = Map.of("amount", amount);
        HttpRequest request = apiClient.newRequestBuilder("/library/user/fines/pay")
                .POST(HttpRequest.BodyPublishers.ofString(apiClient.getGson().toJson(body)))
                .build();
        Map<String, String> response = apiClient.sendRequest(request, new TypeToken<Map<String, String>>() {}.getType());
        return response.get("message");
    }

    @Override
    public List<BorrowRecordView> getMyBorrowRecords(String userId) throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/library/user/" + userId + "/borrows").GET().build();
        Type listType = new TypeToken<List<BorrowRecordView>>() {}.getType();
        return apiClient.sendRequest(request, listType);
    }

    @Override
    public List<ReservationView> getMyReservations(String userId) throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/library/user/" + userId + "/reservations").GET().build();
        Type listType = new TypeToken<List<ReservationView>>() {}.getType();
        return apiClient.sendRequest(request, listType);
    }

    @Override
    public String cancelReservation(int reservationId) throws ApiException {
        Map<String, Object> body = Map.of("reservationId", reservationId);
        HttpRequest request = apiClient.newRequestBuilder("/library/user/reservations/cancel")
                .POST(HttpRequest.BodyPublishers.ofString(apiClient.getGson().toJson(body)))
                .build();
        Map<String, String> response = apiClient.sendRequest(request, new TypeToken<Map<String, String>>() {}.getType());
        return response.get("message");
    }

    @Override
    public LibraryProfile refreshLibraryProfile(String userId) throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/library/user/" + userId + "/profile").GET().build();
        return apiClient.sendRequest(request, LibraryProfile.class);
    }
    //</editor-fold>

    //<editor-fold desc="IBookClientSrv 接口实现">
    @Override
    public List<Book> searchBooks(String title, String author, String publisher, Integer categoryId) throws ApiException {
        Map<String, String> params = new HashMap<>();
        if (title != null && !title.isEmpty()) params.put("title", title);
        if (author != null && !author.isEmpty()) params.put("author", author);
        if (publisher != null && !publisher.isEmpty()) params.put("publisher", publisher);
        if (categoryId != null) params.put("categoryId", String.valueOf(categoryId));

        String query = params.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        String path = "/library/books" + (query.isEmpty() ? "" : "?" + query);
        HttpRequest request = apiClient.newRequestBuilder(path).GET().build();
        Type listType = new TypeToken<List<Book>>() {}.getType();
        return apiClient.sendRequest(request, listType);
    }

    @Override
    public List<Category> getAllCategories() throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/library/categories").GET().build();
        Type listType = new TypeToken<List<Category>>() {}.getType();
        return apiClient.sendRequest(request, listType);
    }
    //</editor-fold>

    //<editor-fold desc="IAdminClientSrv 接口实现">
    @Override
    public boolean addBook(Book book) throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/library/admin/books")
                .POST(HttpRequest.BodyPublishers.ofString(apiClient.getGson().toJson(book)))
                .build();
        apiClient.sendRequest(request, Void.class); // 201 Created or other success codes
        return true;
    }

    @Override
    public boolean updateBook(Book book) throws ApiException {
        HttpRequest request = apiClient.newRequestBuilder("/library/admin/books/" + book.getBookId())
                .PUT(HttpRequest.BodyPublishers.ofString(apiClient.getGson().toJson(book)))
                .build();
        apiClient.sendRequest(request, Void.class); // 200 OK or other success codes
        return true;
    }
    //</editor-fold>
}
