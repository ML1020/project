package app;

import util.PinyinUtil;
import util.Util;

import java.io.File;
import java.util.Date;
import java.util.Objects;

//将数据库所含字段写入此类中，用来比对文件
public class FileMeta {
    //文件名称
    private String name;
    //文件所在的父目录的路径
    private String path;
    //文件大小
    private Long size;//为空，包装类Long
    //文件上次修改时间
    private Date lastModfied;
    //是否是文件夹
    private boolean isDirectory;
    //客户端控件使用，和app.fxml中定义的名称要一致
    //name和path已经定义，缺失sizeText与lastModfiedText
    private String sizeText;
    //和app.fxml中定义的名称要一致【表格控件】
    private String lastModfiedText;
    //文件名拼音
    private String pinyin;
    //文件名拼音首字母
    private String pinyinFirst;

    //通过文件设置属性
    public FileMeta(File file){
        this(file.getName(),file.getParent(),file.isDirectory(),file.length(),new Date(file.lastModified()));
    }

    //通过数据库获取的数据设置FileMeta
    public FileMeta(String name,String path,Boolean isDirectory,long size,Date lastModfied){
        this.name = name;
        this.path = path;
        this.size = size;
        this.isDirectory = isDirectory;
        this.lastModfied = lastModfied;
        //如果文件名包含中文
        if (PinyinUtil.containChinese(name)){
            String[] pinyins = PinyinUtil.get(name);   //获取拼音全拼和首字母
            pinyin = pinyins[0];
            pinyinFirst = pinyins[1];
        }
        //客户端表格控件大小，文件上次修改时间设置
        sizeText = Util.parseSize(size);
        lastModfiedText = Util.parseDate(lastModfied);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileMeta meta = (FileMeta) o;
        return isDirectory == meta.isDirectory &&
                Objects.equals(name, meta.name) &&
                Objects.equals(path, meta.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, path, isDirectory);
    }

    @Override
    public String toString() {
        return "FileMeta{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", isDirectory=" + isDirectory +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getSize() { return size; }

    public void setSize(Long size) {
        this.size = size;
    }

    public Date getLastModfied() {
        return lastModfied;
    }

    public void setLastModfied(Date lastModfied) {
        this.lastModfied = lastModfied;
    }

    public boolean getisDirectory() { return isDirectory; }

    public void setDirectory(boolean directory) {
        this.isDirectory = directory;
    }

    public String getSizeText() {
        return sizeText;
    }

    public void setSizeText(String sizeText) {
        this.sizeText = sizeText;
    }

    public String getLastModfiedText() {
        return lastModfiedText;
    }

    public void setLastModfiedText(String lastModfiedText) {
        this.lastModfiedText = lastModfiedText;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public String getPinyinFirst() {
        return pinyinFirst;
    }

    public void setPinyinFirst(String pinyinFirst) {
        this.pinyinFirst = pinyinFirst;
    }

}
