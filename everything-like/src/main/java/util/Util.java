package util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

//获取表格控件大小
//获取文件上次修改时间

public class Util {
    public static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
    /**
     * 解析文件代码由中文描述
     * @param size
     * @return
     */
    //文件的大小
    public static String parseSize(long size) {
        String[] danweis = {"B","KB","MB","GB","PB","TB"};
        int index = 0;
        while (size > 1024 && index < danweis.length-1){
            size /= 1024;
            index++;
        }
        return size + danweis[index];
    }

    /**
     * 解析日期为中文日期描述
     * @param lastModfied
     * @return
     */
    //文件的日期
    public static String parseDate(Date lastModfied) {
        return new SimpleDateFormat(DATE_PATTERN).format(lastModfied);
    }

    public static void main(String[] args) {
        System.out.println(new File("D:\\桌面").length());   //输出为字节
        System.out.println(parseSize(new File("D:\\桌面").length()));
      //  System.out.println(parseSize(100000000000000L));
        System.out.println(parseSize(10000));
        System.out.println(parseDate(new Date()));
    }
}
