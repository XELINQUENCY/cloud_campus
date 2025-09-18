package client;

import client.bank.BankClient;
import client.course.CourseClient; // 导入 CourseClient
import client.library.LibraryClient;
import client.schoolroll.SchoolRollClient;
import client.shop.ShopClient;
import client.user.UserManagementClient;
import lombok.Getter;

/**
 * 客户端服务工厂 (已更新)
 * 这是获取所有模块客户端的统一入口。
 */
public class ApiClientFactory {

	@Getter
	private static final LibraryClient libraryClient = new LibraryClient(ApiClient.getInstance());

	@Getter
	private static final BankClient bankClient = new BankClient(ApiClient.getInstance());

	@Getter
	private static final ShopClient shopClient = new ShopClient(ApiClient.getInstance());

	@Getter
	private static final SchoolRollClient schoolRollClient = new SchoolRollClient(ApiClient.getInstance());

	@Getter
	private static final CourseClient courseClient = new CourseClient(ApiClient.getInstance());

	@Getter // 新增 UserManagementClient 的实例
	private static final UserManagementClient userManagementClient = new UserManagementClient(ApiClient.getInstance());
}
