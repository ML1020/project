package compile;

import java.io.*;

//借助这个类，让Java代码能够去执行一个具体的指令
//例如：jvavc Test.java
//多进程编程exec ；重定向
public class CommandUtil {
    //cmd表示要执行的命令
    //stdoutFile表示标准输出结果重定向到哪个文件中，如果为null表示不需要重定向
    //stderrFile表示标准错误结果重定向到哪个文件中
    public static int run(String cmd,String stdoutFile,
                           String stderrFile) throws IOException,InterruptedException {
        //1.获取Runtime对象，Runtime对象是一个单例的。唯一的runtime对象
//        【这种模式涉及到一个单一的类，该类负责创建自己的对象，同时确保只有单个对象被创建。
//        这个类提供了一种访问其唯一的对象的方式，可以直接访问，不需要实例化该类的对象。】
//        【JDK中有一个Runtime对象，借助这个对象，可以创建出一个子进程，
//        并且让子进程来执行一个相关的命令（ps：编译javac、运行java）】
        Runtime runtime = Runtime.getRuntime();

        //2.通过Runtime对象中的exec方法来执行命令（具体解释文档中有）
        //相当于在命令行中输入cmd命令并执行
        //exec()：创建子进程、针对子进程进行程序替换
        Process process = runtime.exec(cmd);

        //3.针对标准输出进行重定向
        if (stdoutFile != null){
            //进程的标准输出中的结果就可以通过这个InputStream获取到
            InputStream stdoutFrom = process.getInputStream();
            OutputStream stdoutTo = new FileOutputStream(stdoutFile);
            int ch = -1;
            while ((ch = stdoutFrom.read()) != -1){
                stdoutTo.write(ch);
            }
            stdoutFrom.close();
            stdoutTo.close();
        }

        //4.针对标准错误也进行重定向
        if (stderrFile != null){
            //父进程【自己写的代码】的输入就是子进程【javac】的输出
            //父进程中读取子进程的输出结果，就得借助输入流
            InputStream stderrFrom = process.getErrorStream();
            OutputStream stderrTo = new FileOutputStream(stderrFile);
            int ch = -1;
            while ((ch = stderrFrom.read()) != -1){
                stderrTo.write(ch);
            }
            stderrFrom.close();
            stderrTo.close();
        }
//        预期目标：用父进程中的run方法来控制子进程执行功能
//        在执行run的过程中，子进程也在执行
//        当run执行结束之后，也必须确保子进程也执行完了
//        上述的3.4代码【父进程】中，子进程和父进程之间是并发的关系，谁先执行完是无法确定的
//        为了能够明确让子进程先执行完，就要让父进程进行等待

        //5.为了确保 子进程 先执行完，需要加上进程等待
        //父进程会在waitFor阻塞等待，直到子进程执行结束，再继续往下执行
        int exitCode = process.waitFor();
        return exitCode;  //java1号进程获取到的java2号进程的退出码
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        run("javac","d:/stout.txt","d:/sterr.txt");
    }
}
