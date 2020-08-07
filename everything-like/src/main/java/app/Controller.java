package app;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import task.*;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

//提供界面相关功能
public class Controller implements Initializable {

    @FXML
    private GridPane rootPane;

    @FXML
    private TextField searchField;

    @FXML
    private TableView<FileMeta> fileTable;

    @FXML
    private Label srcDirectory;

    private Thread task;

    public void initialize(URL location, ResourceBundle resources) {
        //界面初始化，需要初始化数据库和表
        DBInit.init();
        // 添加搜索框监听器，内容改变时执行监听事件
        searchField.textProperty().addListener(new ChangeListener<String>() {

            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                freshTable();
            }
        });
    }

    //点击 选择目录按钮
    public void choose(Event event) {
        // 选择文件目录
        DirectoryChooser directoryChooser=new DirectoryChooser();
        Window window = rootPane.getScene().getWindow();
        File file = directoryChooser.showDialog(window);
        if(file == null)
            return;
        // 获取选择的目录路径，并显示
        final String path = file.getPath();
        srcDirectory.setText(path);   //标签的路径
        //选择了目录，就需要执行目录的扫描任务，
        //将该目录下所有的子文件和子文件夹都扫描出来
        //若重新选择目录，则之前的线程被中断
        if (task != null){
            task.interrupt();
        }
        //启动一个线程实现文件任务扫描
        task = new Thread(new Runnable() {
            @Override
            public void run() {
                //文件扫描回调接口，做文件夹下一级子文件和子文件夹保存数据库的操作
                ScanCallback callback = new FileSave();
                //传入扫描任务类
                FileScanner scanner = new FileScanner(callback);
                try {
                    System.out.println("执行文件扫描任务");
                    scanner.scan(path);//为了提高效率，多线程执行扫描任务
                    //多线程扫描完后 可能会直接执行下一行代码，所以要阻塞等待
                    // 等待文件扫描任务执行完毕，scanner.waitFinish()需要阻塞等待
                    scanner.waitFinish();   //如果doscan方法没有结束，那waitFinish会一直阻塞等待
                    //刷新表格，将扫描出来的子文件和子文件夹都展示在表格里
                    System.out.println("任务执行完毕，刷新表格");
                    freshTable();
                } catch (InterruptedException e) {
                    e.printStackTrace();
//                    scanner.shutdown();
                }
            }
        });
        task.start();
    }

    // 刷新表格数据
    private void freshTable(){
        ObservableList<FileMeta> metas = fileTable.getItems();
        metas.clear();
        //如果选择了某个目录，代表需要再根据搜索框的内容，
        //来进行数据库文件信息的查询
        String dir = srcDirectory.getText();//目录
        if (dir != null && dir.trim().length() != 0){
            String content = searchField.getText();//搜索内容
            //提供数据库的查询方法
            List<FileMeta> fileMetas = FileSearch.search(dir,content);
            //接口继承是单继承，接口实现可以多实现
            //Connection--->List/Set--->ArrayList+LinkedList/HashSet+TreeSet
            //Map ---> HashMap/HashTable/TreeMap
            //通过反射
            metas.addAll(fileMetas);
        }
        //方法返回后，java.fx表单做什么？
        //通过反射获取fileMate类型中的属性(app.fxml文件中定义的属性)

    }
}