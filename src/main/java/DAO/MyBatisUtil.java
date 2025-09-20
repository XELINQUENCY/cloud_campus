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
             Properties systemProperties = System.getProperties();

            // 将系统属性作为参数传递给 build 方法
            // MyBatis 会使用这个 Properties 对象来解析配置文件中的 ${...} 占位符
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream, systemProperties);

            System.out.println("成功使用系统属性初始化MyBatis, 数据库URL为: " + systemProperties.getProperty("db.url"));
        } catch (IOException e) {
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
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            M mapper = sqlSession.getMapper(mapperClass);
            int result = operation.apply(mapper);
            if (result > 0) {
                sqlSession.commit();
            } else {
                sqlSession.rollback();
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
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
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            M mapper = sqlSession.getMapper(mapperClass);
            return operation.apply(mapper);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static SqlSessionFactory getSqlSessionFactory() {
        return sqlSessionFactory;
    }
}