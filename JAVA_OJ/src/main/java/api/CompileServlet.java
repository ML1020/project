package api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import compile.Answer;
import compile.Question;
import compile.Task;
import problem.Problem;
import problem.ProblemDAO;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

//接入层模块：实现相应的API
public class CompileServlet extends HttpServlet {
    private Gson gson = new GsonBuilder().create();

    //创建两个辅助的类完成请求解析和响应构建

    //1、辅助解析body中的数据请求
    static class CompileRequest{
        private int id;//用户的题目id
        private String code;//用户的代码

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }

    //辅助构造最终的相应body数据
    static class CompileResponse{
        private int ok;//编译运行情况 0表示编译运行正确、1表示编译出错、2表示运行出错（出现异常）
        private String reason;//包含编译错误信息或者异常信息
        private String stdout;//当前测试用例的执行情况 有 test ok /test failed

        public int getOk() {
            return ok;
        }

        public void setOk(int ok) {
            this.ok = ok;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public String getStdout() {
            return stdout;
        }

        public void setStdout(String stdout) {
            this.stdout = stdout;
        }
    }

    @Override
    //处理POST请求
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        //1.读取请求的body的所有数据
        String body = readBody(req);
        //2.按照API约定的格式来解析JSON数据，得到CompileRequest
        CompileRequest compileRequest = gson.fromJson(body,CompileRequest.class);
        //3.按照id从数据库选择读取相应的测试用例
        ProblemDAO problemDAO = new ProblemDAO();
        Problem problem = problemDAO.selectOne(compileRequest.getId());
        String testCode = problem.getTestCode();//得到gai题目测试题目
        String resquestCode = compileRequest.getCode();//得到用户输入代码
        //4.把用户输入的代码和测试用例进行组装，组装成一个完整的可以编译运行的代码
        String finalCode = mergeCode(resquestCode,testCode);
        //5.创建Task对象对刚才组装好的代码进行编译运行
        Question question = new Question();
        question.setCode(finalCode);
        question.setStdin("");
        Task task = new Task();
        Answer answer = null;
        try {
            answer = task.compileAndRun(question);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //6.把运行结果构造成响应数据返回给客户端
        CompileResponse compileResponse = new CompileResponse();
        compileResponse.setOk(answer.getError());
        compileResponse.setReason(answer.getReason());
        compileResponse.setStdout(answer.getStdout());
        String jsonString = gson.toJson(compileResponse);
        resp.setContentType("application/json;charset=utf-8");
        resp.getWriter().write(jsonString);

    }

    private String mergeCode(String resquestCode, String testCode) {
        //把测试代码testCode中的main方法嵌入到resquestCode中
        //1.先找到 requestCode 中的最后一个 }
        //2.把最后一个 } 去掉，和testCode进行字符串拼接
        //3.拼接完之后  在最后补充上一个 }
        int pos = resquestCode.lastIndexOf("}");
        if (pos == -1) {   //没找到}
            //此时requestCode不对
            return null;
        }
        //此处取得子块不包含pos位置的元素 substring[0,0)
        return resquestCode.substring(0,pos) + testCode + "\n}";
    }

    private String readBody(HttpServletRequest req) {
        //body 的长度在header中的一个Content-Length字段中
        //contentLength的单位是字节
        int contentLength = req.getContentLength();
        byte[] buf = new byte[contentLength];

        try (InputStream inputStream = req.getInputStream()) {
            inputStream.read(buf,0,contentLength);
        }catch (IOException e) {
            e.printStackTrace();
        }

        return new String(buf);
    }
}
