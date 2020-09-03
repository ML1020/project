package common;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

//一个简单的辅助工具类，帮助我们更方便的读写文件
//封装读写文件操作
public class FileUtil {
    //读文件：一下把整个文件内容都读到String中
    public static String readFile(String filePath){
        //当前涉及到的编译错误，标准输出结果等文件内容都是文本文件，
        // 此处使用字符流的方式来实现
        //注意字符流的关闭
        //try括号里的对象是 能自动被关闭的对象，实现close包的对象
        try (FileReader fileReader = new FileReader(filePath);
             BufferedReader bufferedReader = new BufferedReader(fileReader)){
            StringBuffer stringBuffer = new StringBuffer();
            //按行读文件内容
            String line = "";
            while ((line = bufferedReader.readLine()) != null){
                stringBuffer.append(line);
            }
            return stringBuffer.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    //写文件：一下把整个String的内容都写到指定文件中
    //filePath表示要把数据写到哪个指定文件中
    //content表示要写的文件内容
    public static void writeFile(String filePath,String content){
        try (FileWriter fileWriter = new FileWriter(filePath)){
            fileWriter.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
