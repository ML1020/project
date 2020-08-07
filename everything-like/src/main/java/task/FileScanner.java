package task;

import java.io.File;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

//线程池完成文件扫描任务
//一层文件夹 需要 一个线程
public class FileScanner {
    //1.核心线程数，始终运行的线程数量【正式工】
    //2.最大线程数，有新任务，并且当前运行线程数小于最大线程数，会创建新的线程来处理任务【正式工+临时工】
    //3-4.超过3这个数 与 4这个时间单位匹配的时间，(空闲的时间和时间单位)
    //        2-1(最大线程数-核心线程数)个,多余这些线程【临时工】就会关闭
    //5.工作的阻塞队列(能存放的最大任务)
    //6.如果超出工作队列的长度，任务要处理方式
    private ThreadPoolExecutor pool = new ThreadPoolExecutor(
            3,3,0, TimeUnit.MICROSECONDS,
            new LinkedBlockingDeque<Runnable>(),
            new ThreadPoolExecutor.AbortPolicy()
    );
    //之前多线程讲解的方法的快捷模式
    //private ExecutorService exe = Executors.newFixedThreadPool(4);

    //计数器，不传入数值，表示初始化的值为零
    private volatile AtomicInteger count = new AtomicInteger();
    //线程等待的锁对象
    //第一种是实现：synchronized(Lock)进行wait等待
    private Object lock = new Object();
    //第二种实现，await()阻塞等待,直到latch = 0
    private CountDownLatch latch = new CountDownLatch(1);
    //第三种实现，acquire()阻塞等待一定数量的许可证
    private Semaphore semaphore = new Semaphore(0);

    private ScanCallback callback;

    public FileScanner(ScanCallback callback) {
        this.callback = callback;
    }

    /**
     * 扫描文件任务
     * 刚开始不知道有多少个文件夹，so不知道要启动多少个线程数
     * @param path
     */
    public void scan(String path) {
        count.incrementAndGet();//启动根目录扫描任务，i++
        doScan(new File(path));
    }
//递归处理任务
    private void doScan(final File dir) {
        //线程池运行，有多少个子目录，就启动多少个任务并运行
        pool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    callback.callback(dir);   //文件保存操作
                    File[] children = dir.listFiles();  //下一层文件或文件夹
                    if (children != null) {
                        for (File child : children) {
                            //如果是文件夹，处理递归
                            if (child.isDirectory()) {
//                                将打印操作改为保存文件至数据库的操作
//                                System.out.println("文件夹" + child.getPath());
                                //【多线程下的安全技术器，线程安全，保证多线程的可见性】
                                count.incrementAndGet();//启动子文件夹扫描任务，++i
                                System.out.println("启动任务数" + count);
                                doScan(child);
                            }
//                            else {//如果是文件，待做工作
//                                System.out.println("文件" + child.getPath());
//                            }
                        }
                    }
                } finally {//保证线程计数不管是否出现异常。都能够进行-1操作
                    int r = count.decrementAndGet();//减操作
                    if (r == 0) {
                        //第一种，通知这把锁可以往下执行了，否则会一直阻塞等待
//                        synchronized (lock) {lock.notify();}
                        //第二种
//                        latch.countDown();  //计数   i--
                        //第三种
                        semaphore.release();  //颁发一定数量许可证
                    }
                }
            }
        });
    }
    /**
     * 等待扫描任务结束（scan方法）
     * 多线程的任务等待：thread.start()；
     * 1.join();需要使用Thread类的引用对象
     * 2.wait()线程之间等待，Object类
     */
    public void waitFinish() throws InterruptedException {
        //第一种
//        synchronized (lock){
//            lock.wait();
//        }
        //第二种
//        latch.countDown();
        try {
            //第三种
            semaphore.acquire();  //请求足够的资源后才会执行，无参为1
        }finally {
            //阻塞等待，直到任务完成，完成后需要关闭线程池
            shutdown();
        }
    }

    //关闭线程池
    public void shutdown(){
        System.out.println("关闭线程池…");
        //两种关闭线程池的方式，内部实现原理都是通过内部thread.interrput()来中断
//      pool.shutdown();
        //内部实现原理是通过内部thread.stop()来停止线程，不安全
        pool.shutdownNow();
    }

    public static void main(String[] args) throws InterruptedException {
//        Thread t1 = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                System.out.println(Thread.currentThread().getName());//后执行
//            }
//        });
//        t.start();
//        System.out.println(Thread.currentThread().getName());//先执行

        //需要执行t2代码，再执行一段代码
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName());//后执行
            }
        });
        t2.start();
        //把join方法造成线程等待，改为wait()方法实现
        t2.join();
        System.out.println(Thread.currentThread().getName());//先执行
    }
}
