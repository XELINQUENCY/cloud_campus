package client;

import client.bank.BankClient;
import client.bank.IBankClientSrv;
import client.library.LibraryClient;
import client.shop.IShopClientSrv;
import client.shop.ShopClient;
import lombok.Getter;

/**
 * 客户端服务工厂 (已更新)
 * 这是获取所有模块客户端的统一入口。
 */
public class ApiClientFactory {

    // 缓存已创建的模块客户端实例
    @Getter
    private static final LibraryClient libraryClient = new LibraryClient(ApiClient.getInstance());

    public static LibraryClient getLibraryClient() {
		return libraryClient;
	}

	public static BankClient getBankClient() {
		return bankClient;
	}

	public static ShopClient getShopClient() {
		return shopClient;
	}

	@Getter
    private static final BankClient bankClient = new BankClient(ApiClient.getInstance());

    @Getter
    private static final ShopClient shopClient = new ShopClient(ApiClient.getInstance());
    
    

}
