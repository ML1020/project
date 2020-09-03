package api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import problem.Problem;
import problem.ProblemDAO;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;


//实现API.使用Servlet
//此类实现查询所有题目的api
//实现查看题目详情的api
public class ProblemServlet extends HttpServlet {
    private Gson gson = new GsonBuilder().create();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");
        if (id == null || "".equals(id)) {
            // 没有 id 这个参数, 执行 "查找全部" 逻辑
            selectAll(resp);
        } else {
            // 存在 id 这个参数, 执行 "查找指定题目" 逻辑
            selectOne(Integer.parseInt(id), resp);
        }
    }

    private void selectAll(HttpServletResponse resp) throws IOException {
        //ContentType 描述了body中的数据类型
        //常见取值：
        //html:text/html
        //图片：image/png image/jpg
        //json：application/json
        //css：text/css
        //JavaScript：application/JavaScript
        resp.setContentType("application/json; charset=utf-8");
        ProblemDAO problemDAO = new ProblemDAO();
        //没有分页，若想分页可以使用SQL中的limit  offset进行分页，请求中可以带上怕个字段
        List<Problem> problems = problemDAO.selectAll();
        // 把结果组织成 json 结构.
        // [注意!] 需要把 problem 中的有些字段去掉.
        String jsonString = gson.toJson(problems);
        resp.getWriter().write(jsonString);
    }

    private void selectOne(int problemId, HttpServletResponse resp) {
        resp.setContentType("application/json;charset=utf-8");//设置统一的编码类型，防止乱码
        ProblemDAO problemDAO = new ProblemDAO();
        Problem problem = problemDAO.selectOne(problemId);
        //测试代码不应该告诉前端，此时手动把这个内容清理
        problem.setTestCode("");
        //toJson()方法是实现从java实体到Json相关对象的方法
        //例如：problem传入数据(1,"个位相加","简单")，则会生成
        //"id":1,"title":"各位相加","level":"简单"
        String jsonString = gson.toJson(problem);
        try {
            resp.getWriter().write(jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
