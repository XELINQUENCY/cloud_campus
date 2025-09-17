package client;

import client.bank.BankClient;
import client.library.LibraryClient;
import client.schoolroll.SchoolRollClient; // 导入 SchoolRollClient
import client.shop.ShopClient;
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

	// [新增] 为学籍管理客户端添加一个静态实例和getter
	@Getter
	private static final SchoolRollClient schoolRollClient = new SchoolRollClient(ApiClient.getInstance());

}
