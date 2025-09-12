package client;

import client.library.IAdminClientSrv;
import client.library.IBookClientSrv;
import client.library.IUserClientSrv;
import client.library.LibraryClient;
import lombok.Getter;

/**
 * 客户端服务工厂
 * 这是获取所有模块客户端的统一入口。
 */
public class ApiClientFactory {

    /**
     * -- GETTER --
     *  获取图书馆模块的客户端实例。
     *  返回的是接口类型，符合面向接口编程的原则。
     *
     * @return 实现了 IUserClientSrv, IAdminClientSrv, IBookClientSrv 的客户端实例
     */
    // 缓存已创建的模块客户端实例
    @Getter
    private static final LibraryClient libraryClient = new LibraryClient(ApiClient.getInstance());
    // private static final StoreClient storeClient = new StoreClient(ApiClient.getInstance()); // 未来可添加

    /**
     * 获取商店模块的客户端实例（示例）。
     */
    // public static IStoreClientSrv getStoreClient() {
    //     return storeClient;
    // }
}