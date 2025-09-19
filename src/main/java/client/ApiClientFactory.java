package client;

import client.bank.BankClient;
import client.course.CourseClient; // 导入 CourseClient
import client.library.LibraryClient;
import client.schoolroll.SchoolRollClient;
import client.shop.ShopClient;
import client.user.UserManagementClient;
import lombok.Getter;

public class ApiClientFactory {

	@Getter
	private static final ApiClient generalApiClient = new ApiClient();
	@Getter
	private static final ApiClient bankApiClient = new ApiClient();

	@Getter
	private static final LibraryClient libraryClient = new LibraryClient(generalApiClient);

	@Getter
	private static final BankClient bankClient = new BankClient(bankApiClient); // <-- 使用银行专用的ApiClient

	@Getter
	private static final ShopClient shopClient = new ShopClient(generalApiClient);

	@Getter
	private static final SchoolRollClient schoolRollClient = new SchoolRollClient(generalApiClient);

	@Getter
	private static final CourseClient courseClient = new CourseClient(generalApiClient);

	@Getter
	private static final UserManagementClient userManagementClient = new UserManagementClient(generalApiClient);

}