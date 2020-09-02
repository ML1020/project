package compile;

//要编译和执行的代码

//描述了一次编译过程中都依赖了哪些数据
public class Question {
    //要编译和执行的代码

    //通过命令来编译代码，需要让代码在文件上
    //就需要先把code中的内容写到一个临时文件中再编译
    private String code;
    //执行时标准输入要输入的内容
    private String stdin;//实际上没有用到

    public String getCode() {
        return code;
    }

    public String getStdin() {
        return stdin;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setStdin(String stdin) {
        this.stdin = stdin;
    }
}
