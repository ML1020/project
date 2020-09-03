package common;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import javax.sql.DataSource;
import javax.xml.transform.Result;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

//借助这个类和数据库建立连接，进一步进行操作
public class DBUtil {
    //环回ip，自己访问自己
    //jdbc:mysql://ip:端口号/数据库名称         服务器的ip   数据库默认端口3306
    //服务器端的ip地址   116.62.178.33
    //主机IP  192.168.1.5
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/test?" +
            "characterEncoding=utf8&useSSL=true";    //SSL实现密码登录，更加安全
    private static final String USERNAME = "root";
    private static final String PASSSWORD = "jiao19991020";

    private static volatile DataSource dataSource = null;//保证内存可见性

    //单例模式的线程安全：双重if校验锁、volatile关键字、 synchronized加锁

    //将构造方法私有化
    private DBUtil(){ }

    //获取当前的实例
    public static DataSource getDataSource() {
        //单例模式。这是类方法，可针对类对象加锁
        if (dataSource == null) {
            synchronized (DBUtil.class) {
                if (dataSource == null) {
                    dataSource = new MysqlDataSource();
                    //设置相关属性，才能正确建立连接
                    ((MysqlDataSource)dataSource).setURL(URL);
                    ((MysqlDataSource)dataSource).setUser(USERNAME);
                    ((MysqlDataSource)dataSource).setPassword(PASSSWORD);
                }
            }
        }
        return dataSource;
    }

    //获取具体的连接
    public static Connection getConnection(){
        try {
            //内置数据库连接池
            return getDataSource().getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    //关闭数据库  释放资源
    public static void close(Connection connection, PreparedStatement preparedStatement, ResultSet resultSet) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
