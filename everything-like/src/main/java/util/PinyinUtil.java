package util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

//拼音的相关功能
    public class PinyinUtil {
        /**
         * 中文字符格式
         */
        private static final String CHINESE_PATTERN = "[\\u4E00-\\u9FA5]";//正则表达式

        /**
         * 汉语拼音格式化类
         */
        private static final HanyuPinyinOutputFormat FORMAT = new HanyuPinyinOutputFormat();

        //初始化
        static {
            //设置拼音小写
            FORMAT.setCaseType(HanyuPinyinCaseType.LOWERCASE);
            //设置不带音调
            FORMAT.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
            //设置带V字符，如：绿lv
            FORMAT.setVCharType(HanyuPinyinVCharType.WITH_V);
        }

        /**
         * 检验字符串是否包含中文
         *
         * @param name
         * @return
         */
        public static boolean containChinese(String name) {
            return name.matches(".*" + CHINESE_PATTERN + ".*");
        }

        /**
         * 通过文件名获取全拼+拼音首字母
         * 中华人民共和国 --> zhonghuarenmingongheguo
         *
         * @param name 文件名
         * @return 拼音全拼字符串+拼音首字母字符串 数组
         */
        public static String[] get(String name) {
            String[] result = new String[2];
            //StringBuilder线程不安全，故效率高一些
            StringBuilder pinyin = new StringBuilder();//全拼
            StringBuilder pinyinFirst = new StringBuilder();//拼音首字母
            //中华人民共和国
            for (char c : name.toCharArray()) {
                // [zhong] [he,hu,huo ...]
                try {
                    String[] pinyins = PinyinHelper.toHanyuPinyinStringArray(c, FORMAT);
                    if (pinyins == null || pinyins.length == 0) {
                        pinyin.append(c);
                        pinyinFirst.append(c);
                    } else {
                        //全拼：多音字数组中的，和 -> he
                        pinyin.append(pinyins[0]);
                        //首字母：多音字数组中的，和 -> h
                        pinyinFirst.append(pinyins[0].charAt(0));
                    }
                } catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
                    pinyin.append(c);
                    pinyinFirst.append(c);
                }
            }
            //[zhonghuarenmingongheguo,zhrmghg]
            result[0] = pinyin.toString();
            result[1] = pinyinFirst.toString();
            return result;
        }

        /**
         * 多音字
         * 例：和[he,hu,huo ...]长[zhang,chang]和[he,hu,huo ...]
         * 最终返回：hezhanghe/hezhanghu/hezhanghuo/...
         *
         * @param name      文件名
         * @param fullSpell true表示全拼，false表示取拼音首字母
         * @return 包含多音字的字符串二维数组：
         * [[he,hu,huo, ...],[zhang,chang],[he,hu,huo, ...]]
         */
        public static String[][] get(String name, boolean fullSpell) {
            char[] chars = name.toCharArray();
            //chars.length == 3
            String[][] result = new String[chars.length][];
            for (int i = 0; i < chars.length; i++) {
                //和：[he,hu,huo ...]； *：[]或null
                try {
                    //去除音调，"只"：[zhi一声,zhi三声]考虑去重
                    //pinyins存储的是汉字的所有读音，长度为读音个数
                    //eg和：hehehuohuohuohaihehu
                    String[] pinyins = PinyinHelper.toHanyuPinyinStringArray(chars[i], FORMAT);
//                    System.out.println(pinyins.length);  //8
//                    for (int j = 0;j < pinyins.length;j++) {
//                        System.out.print(pinyins[j]);
//                    }
                    if (pinyins == null || pinyins.length == 0) {
                        result[i] = new String[]{String.valueOf(chars[i])};
                    }
//                    } else if (fullSpell){  //全拼
//                        result[i] = pinyins;
//                    }else {    //拼音首字母
//                        String[] array = new String[pinyins.length];
//                        for (int j = 0;j < pinyins.length;j++){
//                            array[j] = String.valueOf(pinyins[j].charAt(0));
//                        }
//                        result[i] = array;
//                    }
                    else {//拼音首字母
                        result[i] = unique(pinyins, fullSpell);
                    }
                } catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
                    result[i] = new String[]{String.valueOf(chars[i])};
                }
            }
            return result;
        }

        /**
         * 字符串数组去重操作
         *
         * @param array
         * @param fullSpell
         * @return
         */
        public static String[] unique(String[] array, boolean fullSpell) {
            Set<String> set = new HashSet<String>();
            for (String s : array) {
                if (fullSpell)
                    set.add(s);
                else
                    set.add(String.valueOf(s.charAt(0)));
            }
            return set.toArray(new String[set.size()]);
        }

        /**
         * 排列组合
         * [he,hu,huo, ...],[zhang,chang]
         * hezhang,hechang,huzhang,huchang,huozhang,huochang
         * 每个中文字符返回拼音是字符串数组，每两个字符串数组合并为一个字符串数组
         * 之后依次类推
         * @param pinyinArray
         */
        public static String[] compose(String[][] pinyinArray) {
            if (pinyinArray == null || pinyinArray.length == 0) {
                return null;
            } else if (pinyinArray.length == 1) {
                return pinyinArray[0];
            } else {
                for (int i = 1; i < pinyinArray.length; i++) {
                    pinyinArray[0] = compose(pinyinArray[0], pinyinArray[i]);
                }
                return pinyinArray[0];
            }
        }

        /**
         * 合并两个拼音数组为一个
         * @param pinyins1 [he,hu,huo, ...]
         * @param pinyins2 [zhang,chang]
         * @return result  [hezhang, hechang, huzhang, huchang, huozhang, huochang]
         */
        public static String[] compose(String[] pinyins1, String[] pinyins2) {
            String[] result = new String[pinyins1.length * pinyins2.length];
            for (int i = 0; i < pinyins1.length; i++) {
                for (int j = 0; j < pinyins2.length; j++) {
                    result[i * pinyins2.length + j] = pinyins1[i] + pinyins2[j];
                }
            }
            return result;
        }

        public static void main(String[] args) {
//            System.out.println(Arrays.toString(get("中华人民共和国")));
//            System.out.println(Arrays.toString(get("中华1人b民A共和国")));
//            System.out.println(Arrays.toString(get("和长和",true)));
//            System.out.println(Arrays.toString(get("和长和",false)));
        System.out.println(Arrays.toString(compose(get("和长和",true))));
        System.out.println(Arrays.toString(compose(get("和长和",false))));

            //正则表达式
//            System.out.println("abc".matches(".*"));
//            System.out.println("abc".matches("a[bc]"));
//            System.out.println("abc".matches("a[bc]c"));
//            System.out.println("abc".matches(".*b.*"));
//            System.out.println("abc".matches(".*[a-z].*"));
        }
    }
