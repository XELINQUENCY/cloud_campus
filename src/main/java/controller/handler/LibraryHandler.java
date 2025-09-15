package controller.handler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import entity.library.Book;
import service.library.LibraryService;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 负责处理所有图书馆相关的API请求 (/api/library/**)
 */
public class LibraryHandler extends BaseHandler {
    private final LibraryService libraryService;

    public LibraryHandler(LibraryService libraryService, Gson gson, ServerLogger logger) {
        super(gson, logger);
        this.libraryService = libraryService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        String authenticatedUserId = (String) exchange.getAttribute("userId");

        try {
            // --- 公共接口 (无需认证) ---
            if (path.equals("/api/library/books") && "GET".equalsIgnoreCase(method)) {
                Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
                Integer categoryId = params.containsKey("categoryId") ? Integer.parseInt(params.get("categoryId")) : null;
                sendJsonResponse(exchange, 200, libraryService.searchBooks(params.get("title"), params.get("author"), params.get("publisher"), categoryId));
            } else if (path.equals("/api/library/categories") && "GET".equalsIgnoreCase(method)) {
                sendJsonResponse(exchange, 200, libraryService.getAllCategories());
            }
            // --- 用户接口 (需要认证) ---
            else if (path.matches("/api/library/user/[^/]+/profile") && "GET".equalsIgnoreCase(method)) {
                sendJsonResponse(exchange, 200, libraryService.refreshLibraryProfile(authenticatedUserId));
            } else if (path.matches("/api/library/user/[^/]+/borrows") && "GET".equalsIgnoreCase(method)) {
                sendJsonResponse(exchange, 200, libraryService.getMyBorrowRecords(authenticatedUserId));
            } else if (path.matches("/api/library/user/[^/]+/reservations") && "GET".equalsIgnoreCase(method)) {
                sendJsonResponse(exchange, 200, libraryService.getMyReservations(authenticatedUserId));
            } else if (path.equals("/api/library/user/borrows") && "POST".equalsIgnoreCase(method)) {
                Map<String, Object> body = gson.fromJson(new InputStreamReader(exchange.getRequestBody()), new TypeToken<Map<String, Object>>(){}.getType());
                int bookId = ((Double) body.get("bookId")).intValue();
                String message = libraryService.borrowBook(authenticatedUserId, bookId);
                sendJsonResponse(exchange, 200, Map.of("message", message));
            } else if (path.equals("/api/library/user/returns") && "POST".equalsIgnoreCase(method)) {
                Map<String, Object> body = gson.fromJson(new InputStreamReader(exchange.getRequestBody()), new TypeToken<Map<String, Object>>(){}.getType());
                int copyId = ((Double) body.get("copyId")).intValue();
                String message = libraryService.returnBook(copyId);
                sendJsonResponse(exchange, 200, Map.of("message", message));
            } else if (path.equals("/api/library/user/renews") && "POST".equalsIgnoreCase(method)) {
                Map<String, Object> body = gson.fromJson(new InputStreamReader(exchange.getRequestBody()), new TypeToken<Map<String, Object>>(){}.getType());
                int recordId = ((Double) body.get("recordId")).intValue();
                String message = libraryService.renewBook(recordId);
                sendJsonResponse(exchange, 200, Map.of("message", message));
            } else if (path.equals("/api/library/user/reservations") && "POST".equalsIgnoreCase(method)) {
                Map<String, Object> body = gson.fromJson(new InputStreamReader(exchange.getRequestBody()), new TypeToken<Map<String, Object>>(){}.getType());
                int bookId = ((Double) body.get("bookId")).intValue();
                String message = libraryService.reserveBook(authenticatedUserId, bookId);
                sendJsonResponse(exchange, 200, Map.of("message", message));
            } else if (path.equals("/api/library/user/reservations/cancel") && "POST".equalsIgnoreCase(method)) {
                Map<String, Object> body = gson.fromJson(new InputStreamReader(exchange.getRequestBody()), new TypeToken<Map<String, Object>>(){}.getType());
                int reservationId = ((Double) body.get("reservationId")).intValue();
                String message = libraryService.cancelReservation(reservationId);
                sendJsonResponse(exchange, 200, Map.of("message", message));
            } else if (path.equals("/api/library/user/fines/pay") && "POST".equalsIgnoreCase(method)) {
                Map<String, Object> body = gson.fromJson(new InputStreamReader(exchange.getRequestBody()), new TypeToken<Map<String, Object>>(){}.getType());
                double amount = (Double) body.get("amount");
                String message = libraryService.payFine(authenticatedUserId, amount);
                sendJsonResponse(exchange, 200, Map.of("message", message));
            }
            // --- 管理员接口 (需要认证) ---
            else if (path.equals("/api/library/admin/books") && "POST".equalsIgnoreCase(method)) {
                Book book = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), Book.class);
                libraryService.addBook(book);
                sendJsonResponse(exchange, 201, Map.of("message", "书籍添加成功"));
            } else if (path.matches("/api/library/admin/books/\\d+") && "PUT".equalsIgnoreCase(method)) {
                Book book = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), Book.class);
                libraryService.updateBook(book);
                sendJsonResponse(exchange, 200, Map.of("message", "书籍更新成功"));
            }
            else {
                sendJsonResponse(exchange, 404, Map.of("error", "未知的图书馆API路径: " + path));
            }
        } catch (JsonSyntaxException e) {
            sendJsonResponse(exchange, 400, Map.of("error", "无效的JSON格式"));
        } catch (Exception e) {
            logger.log("业务逻辑错误: " + e.getMessage());
            sendJsonResponse(exchange, 400, Map.of("error", e.getMessage()));
        }
    }
}

