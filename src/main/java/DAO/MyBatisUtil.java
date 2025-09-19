package DAO;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.function.Function;


public class MyBatisUtil {

    private static final SqlSessionFactory sqlSessionFactory;

    static {
        try (InputStream inputStream = Resources.getResourceAsStream("mybatis-config.xml")) {
            // 1. 获取所有的 Java 系统属性
             Properties systemProperties = System.getProperties();

            // 2. 将系统属性作为参数传递给 build 方法
            //    MyBatis 会使用这个 Properties 对象来解析配置文件中的 ${...} 占位符
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream, systemProperties);

            System.out.println("成功使用系统属性初始化MyBatis, 数据库URL为: " + systemProperties.getProperty("db.url"));
        } catch (IOException e) {
            // 在实际应用中，这里应该使用日志框架记录错误
            throw new ExceptionInInitializerError("Failed to build SqlSessionFactory: " + e.getMessage());
        }
    }

    /**
     * 通用的数据库写操作执行器 (CUD: Create, Update, Delete)
     * @param mapperClass Mapper接口的Class对象
     * @param operation 一个函数，接收一个Mapper实例并返回受影响的行数(int)
     * @param <M> Mapper接口的类型
     * @return 受影响的行数(一般是1)
     */
    public static <M> int executeUpdate(Class<M> mapperClass, Function<M, Integer> operation) {
        // 1. 开启Session (try-with-resources 保证自动关闭)
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            // 2. 获取指定类型的Mapper
            M mapper = sqlSession.getMapper(mapperClass);

            // 3. 执行传入的具体操作
            int result = operation.apply(mapper);

            // 4. 根据结果提交或回滚
            if (result > 0) {
                sqlSession.commit();
            } else {
                sqlSession.rollback();
            }
            return result;
        } catch (Exception e) {
            // 在实际应用中，这里应该记录日志
            e.printStackTrace();
            // 发生异常时，try-with-resources会关闭session，但不会回滚，所以显式回滚不是必须的，
            // 但如果session不是自动提交模式，回滚是一个好习惯。
            // 不过由于session已关闭，回滚也无法执行。最重要的是记录异常。
            return 0;
        }
    }

    /**
     * 通用的数据库读操作执行器 (R: Read)
     * @param mapperClass Mapper接口的Class对象
     * @param operation 一个函数，接收一个Mapper实例并返回类型为R的结果
     * @param <M> Mapper接口的类型
     * @param <R> 返回结果的类型
     * @return 返回数据库查询的结果
     */
    public static <M, R> R executeQuery(Class<M> mapperClass, Function<M, R> operation) {
        // 1. 开启Session
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            // 2. 获取指定类型的Mapper
            M mapper = sqlSession.getMapper(mapperClass);

            // 3. 执行传入的查询操作并直接返回结果
            return operation.apply(mapper);
        } catch (Exception e) {
            // 在实际应用中，这里应该记录日志
            e.printStackTrace();
            return null; // 或者抛出自定义异常
        }
    }

    public static SqlSessionFactory getSqlSessionFactory() {
        return sqlSessionFactory;
    }
}