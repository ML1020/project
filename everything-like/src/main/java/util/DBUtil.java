package util;

import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

//提供数据库的一些功能
public class DBUtil {
    //单例  volatile
    private static volatile DataSource DATA_SOURSE;
    //提供获取数据库连接池的功能
    //使用单例模式（多线程安全版本）
    //回顾多线程安全版本的单例模式：
    //1.为什么在外层判断是否等于null：提高效率
    //2.synchronized加锁后，为什么还要判断等于null：
    //  保证DATA_SOURSE的可见性，是同一个对象，避免所有的线程进入后都进行初始化
    //3.为什么DataSourse变量要使用valatile关键字修饰：
    //   在加锁之外也要保证可见性，防止不同的线程执行后结果不对，同时也可以保证有序性
    //多线程操作：原子性，可见性（主内存拷贝到工作内存），有序性
    //synchronized 保证三个特性，valatile保证可见性，有序性
    private static DataSource getDataSource(){
        //双重校验锁
        //不同的线程读取相同的对象，DATA_SOURSE 从主内存读取到工作内存
        if (DATA_SOURSE == null){//提高效率，之前的valatile关键字可以保证其可见性
            //刚开始所有进入这行代码的线程，DATA_SOURSE的对象都是null，然后：
            //可能是第一个进去的线程，这时DATA_SOURSE的对象都是null
            //也可能是第一个线程之后的线程进入并执行，DATA_SOURSE 已经被初始化了
            synchronized (DBUtil.class){
                if (DATA_SOURSE == null){   //为空了才new新对象，满足单例模式的设计
                    //初始化操作，使用valatile关键字禁止指令重排序，建立内存屏障
                    SQLiteConfig config = new SQLiteConfig();
                    config.setDateStringFormat(Util.DATE_PATTERN);
                    DATA_SOURSE = new SQLiteDataSource(config);  //禁止指令重排序
                    ((SQLiteDataSource)DATA_SOURSE).setUrl(getUrl());
                }
            }
        }
        return DATA_SOURSE;
    }
    /**
     * 获取sqlite数据库文件url的方法
     * @return
     */
    private static String getUrl(){
        try {
            //获取target编译文件夹的路径
            //通过classLoader.getResource()/classLoader.getResourceAsStream()这样的方法
            // 默认的根路径为编译文件夹的路径(target/classes)
            URL classesURL = DBUtil.class.getClassLoader().getResource("./");
            //获取target/classes文件夹的父目录路径
            String dir = new File(classesURL.getPath()).getParent();
            String url = "jdbc:sqlite://"+ dir + File.separator + "everything-like.db";
            //new SqliteDateSource()，把这个对象的url设置进去，才会创建这个文件
            //如果这个文件已经存在，就会读取这个文件
            url = URLDecoder.decode(url,"UTF-8");//处理路径有中文的情况
            System.out.println("获取数据库文件路径："+ url);
            return url;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException("获取数据库文件路径失败",e);
        }
    }
    /**
     * 提供获取数据库连接的方法
     * 从数据库连接池DataSource.getConnection()来获取数据库连接
     * @return
     */
    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }


    public static void main(String[] args) throws SQLException {
        System.out.println(getConnection());
    }

    public static void close(Connection connection, Statement statement) {
        close(connection,statement,null);
    }
    //释放数据库资源
    //connection  数据库连接
    //statement  sql执行对象
    //result 结果集
    public static void close(Connection connection,
                             Statement statement, ResultSet resultset) {
        try {
            if (connection != null)
                connection.close();
            if(statement != null)
                statement.close();
            if(resultset != null)
                resultset.close();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("释放数据库资源错误");
        }
    }

}
