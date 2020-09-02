package compile;

import common.FileUtil;
import java.io.File;
import java.io.IOException;

//借助这个类来描述一次编译运行的结果，方便调试
public class Task {
    //编译运行过程中以来一些临时文件，需要约定临时文件按名字
    //所有的临时文件到放到temp目录中
    //临时文件为了把执行过程中涉及到的各种中间结果记录下来，方便后面调试
    private static final String WORK_DIR = "./temp/";    //工作目录
    //要编译的代码的类型的类命
    private static final String CLASS = "Solution";
    //源代码文件。要编译代码对应的文件，需要和类型一致
    private static final String CODE = WORK_DIR + "Solution.java";
    //标准输入对应的文件
    private static final String STDIN = WORK_DIR + "stdin.txt";//没用到
    //标准输出对应的文件【编译执行的代码的结果保存到这个文件中】
    private static final String STDOUT = WORK_DIR + "stdout.txt";
    //标准错误对应的文件【编译执行的代码的结果保存到这个文件中】
    private static final String STDERR = WORK_DIR + "stderr.txt";
    //编译错误对应的文件【编译出错的具体原因】
    private static final String COMPILE_ERROR  = WORK_DIR + "complie_error.txt";
//    private Answer answer;
//    private String stdError;

    //  compileAndRun
    //	1.构造编译命令并执行
    //	2.构造运行命令并执行
    public Answer compileAndRun(Question question) throws IOException, InterruptedException {
        Answer answer = new Answer();
        //0.先创建好存放临时文件的目录
        File workDir = new File(WORK_DIR);
        if (!workDir.exists()){
            //mkdirs()可以建立多级文件夹， mkdir()只会建立一级的文件夹
            workDir.mkdirs();
        }
        //1.根据Question对象，构造需要的一些临时文件
        FileUtil.writeFile(CODE,question.getCode());
        FileUtil.writeFile(STDIN,question.getStdin());

        //2.构造编译命令，并执行
        //   编译命令形如 javac -encoding utf8 ./tmp/Solution.java -d ./tmp/
        //  -d指定放置生成的类文件的位置
        //   直接通过字符串拼接，有的时候如果太复杂，容易拼错，
        //      尤其是命令选项多的时候，很容易少空格之类的
        String cmd = String.format("javac -encoding utf8 %s -d %s", CODE, WORK_DIR); //%s替换为字符串
        System.out.println("编译命令：" + cmd);
        //String cmd,String stdoutFile,String stderrFile
        CommandUtil.run(cmd,null,COMPILE_ERROR);
        //还需要判断一下编译是否出错，若出错，就不需要继续运行
        //认为COMPILE_ERROR文件为空，就表示编译顺利，如果非空就表示编译出错
        String compileError = FileUtil.readFile(COMPILE_ERROR);
        if (!"".equals(compileError)){
            //编译出错
            System.out.println("编译出错");
            answer.setError(1);
            answer.setReason(compileError);
            return answer;
        }
        //3.构造运行命令，并执行
        //   运行命令，如java -classpath ./tmp/ Solution
        //   为了能让java命令正确找到类对应的.class文件，
        //   需要指定加载路径，-classpath选项来指定
        //-d输出的目录就是-classpath输入的目录
        cmd = String.format("java -classpath %s %s",WORK_DIR,CLASS);
        System.out.println("运行命令："+cmd);
        CommandUtil.run(cmd,STDOUT,STDERR);
        //判断运行是否出错(是否存在异常)，查看STDERR文件
        String stdError = FileUtil.readFile(STDERR);
        if (!"".equals(stdError)){
            System.out.println("运行出错");
            answer.setError(2);
            answer.setReason(stdError);
            answer.setStderr(stdError);
            return answer;
        }
        //4.将最终的运行结果包装到Anser中
        answer.setError(0);
        answer.setStdout(FileUtil.readFile(STDOUT));
        return answer;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        //验证Task是否正确运行
        Question question = new Question();
        question.setCode(
                "public class Solution{\n" +
                        "public static void main(String[] args){\n" +
                        "String s = \"abcd\";\n"+
                        "System.out.println(s.length());\n" +
                        "}\n " +
                        "}\n"
        );
        question.setStdin("");
        Task task = new Task();
        Answer answer = task.compileAndRun(question);
        System.out.println(answer);
    }
}
