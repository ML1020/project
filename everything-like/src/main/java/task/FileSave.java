package task;

import app.FileMeta;
import util.DBUtil;
import util.PinyinUtil;
import util.Util;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//将文件信息保存到数据库
//基于FileMeta类与Util类进行文件比对操作
//本地查找文件后，在数据库中进行插入删除操作
public class FileSave implements ScanCallback {

    @Override
    public void callback(File dir) {   //回调类
        //文件夹下一级子文件和文件夹保存到数据库
        //获取本地目录下一级子文件和子文件夹
        //集合框架中使用自定义类型，判断是否某个对象在集合存在；比对两个集合中的元素
        File[] children = dir.listFiles();
        List<FileMeta> locals = new ArrayList();
        if (children != null){
            for (File child : children){
                //将本地文件加入到本地文件集合中
                locals.add(new FileMeta(child));
            }
        }

        //获取数据库保存的dir目录的下一级子文件和子文件夹
        List<FileMeta> metas = query(dir);
        //数据库有，本地没有，在数据库中做删除
        for (FileMeta meta : metas){
            if (!locals.contains(meta)){
                delete(meta);
                //meta的删除；
                //1.删除meta信息本身
                //2.如果meta是目录，还要将meta所有的子文件，子文件夹都删除
            }
        }
        //本地有，数据库没有，在数据库中做插入
        for (FileMeta meta:locals){
            if (!metas.contains(meta)){
                save(meta);
            }
        }
    }

    //meta的删除
    //1.删除meta信息本身
    //2.如果meta是目录，还要将meta所有子文件，子文件夹都删除
    private void delete(FileMeta meta) {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = DBUtil.getConnection();
            String sql = "delete from file_meta where"+
                    " (name=? and path=? and is_directory=?)";//删除文件自身
            if (meta.getisDirectory()){//如果是文件夹，需要删除子文件夹以及子文件
                sql += " or path=?"+//匹配数据库文件夹的儿子
                        " or path like ?";//匹配文件夹的孙后辈
            }
            ps = connection.prepareStatement(sql);
            ps.setString(1,meta.getName());
            ps.setString(2,meta.getPath());
            ps.setBoolean(3,meta.getisDirectory());
            if (meta.getisDirectory()){
                ps.setString(4,
                        meta.getPath()+File.separator+meta.getName());
                ps.setString(5,
                        meta.getPath()+File.separator+meta.getName()+File.separator);
            }
            System.out.printf("删除文件信息，dir = %s\n",
                    meta.getPath()+File.separator+meta.getName());
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("删除文件信息出错，检查delete语句",e);
        }finally {
            DBUtil.close(connection,ps);
        }

    }

    //对数据库进行相关信息的查询
    private List<FileMeta> query(File dir){
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<FileMeta> metas = new ArrayList<>();
        try {
            //1.创建数据库连接
           connection = DBUtil.getConnection();
           String sql = "select name, path, is_directory, size, last_modified"+
                   " from file_meta where path = ?";
           //2.创建dbc操作命令对象statement
           ps = connection.prepareStatement(sql);
           ps.setString(1,dir.getPath());
           //3.执行sql语句
            rs = ps.executeQuery();
            //4.处理结果集ResultSet
            while (rs.next()){
                String name = rs.getString("name");
                String path = rs.getString("path");
                Boolean isDirectory = rs.getBoolean("is_directory");
                Long size = rs.getLong("size");
                Timestamp lastModified = rs.getTimestamp("last_modified");
                FileMeta meta = new FileMeta(name,path,isDirectory,
                        size,new java.util.Date(lastModified.getTime()));
                System.out.printf("查询文件信息：name=%s, path=%s, is_directory=%s,"+
                                " size=%s, last_modified=%s\n",name,path,String.valueOf(isDirectory),
                        String.valueOf(size), Util.parseDate(new java.util.Date(lastModified.getTime())));
                //把数据加入到metas集合中
                metas.add(meta);
            }
           return metas;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("查询文件信息出错，检查sql语句",e);
        } finally {
            DBUtil.close(connection,ps,rs);
        }
    }

    /**
     * 文件信息保存到数据库
     * @param meta
     */
    private void save(FileMeta meta) {
        //1.获取数据库连接
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            //1.获取数据库连接
            connection = DBUtil.getConnection();
            String sql = "insert into file_meta"+
                    "(name, path, is_directory, size, last_modified, pinyin, pinyin_first)" +
                    "values(?, ?, ?, ?, ?, ? ,?)";
            //2.获取sql操作命令对象statement
            statement = connection.prepareStatement(sql);
            statement.setString(1,meta.getName());
            statement.setString(2, meta.getPath());  //父目录路径
            statement.setBoolean(3,meta.getisDirectory());
            statement.setLong(4, meta.getSize());
            statement.setString(5,meta.getLastModfiedText());
            statement.setString(6,meta.getPinyin());
            statement.setString(7,meta.getPinyinFirst());
            //文件名包含汉字，需要获取拼音和拼音的首字母，并保存到数据库
            //System.out.println("执行文件保存操作" + sql);
            //3.执行sql
            System.out.printf("insert name=%s,path=%s\n",meta.getName(),meta.getPath());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("文件保存失败，检查sql insert语句", e);
        } finally {
            //4.释放资源
            DBUtil.close(connection, statement);
        }
    }

    public static void main(String[] args) {
        //数据库重新初始化
//        DBInit.init();
//        File file = new File("D:\\桌面\\原桌面重要");
//        FileSave fileSave = new FileSave();
//        fileSave.save(file);
//        fileSave.query(file.getParentFile());

        //本地保存的
        List<FileMeta> locals = new ArrayList<FileMeta>();
        locals.add(new FileMeta("歌词","D:\\文件测试",
                true,0,new Date()));
        locals.add(new FileMeta("只道寻常","D:\\文件测试\\歌词",
                true,0,new Date()));
        locals.add(new FileMeta("供应链申请表","D:\\文件测试",
                true,0,new Date()));

        //数据库中保存的
        List<FileMeta> metas = new ArrayList<FileMeta>();
        metas.add(new FileMeta("歌词1","D:\\文件测试",
                true,0,new Date()));
        metas.add(new FileMeta("只道寻常","D:\\文件测试\\歌词1",
                true,0,new Date()));
        metas.add(new FileMeta("供应链申请表","D:\\文件测试",
                true,0,new Date()));

        Boolean contains = locals.contains(new FileMeta(new File(" ")));
        //集合中是否包含某个元素，不一定代表传入这个对象在Java内存中是同一个对象的引用
        //满足一定条件(集合中的元素类型需要重写hashCode和equals
        // 根据业务需要哪些属性来判断元素同一个----> )，
        // list.contains方法可以返回true
        for (FileMeta meta:locals){
            //如果数据库中不包含本地文件，则输出此本地文件
            if (!metas.contains(meta)){
                System.out.println(meta);
            }
        }
    }
}
