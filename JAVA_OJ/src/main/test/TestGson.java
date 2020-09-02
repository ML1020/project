import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

//gson格式
public class TestGson {
    static class Hero{
        public String name;
        public String skill1;
        public String skill2;
        public String skill3;
        public String skill4;
    }

    public static void main(String[] args) {
        Hero hero = new Hero();
        hero.name = "曹操";
        hero.skill1 = "hhh";
        hero.skill2 = "Hhh上述";
        hero.skill3 = "到到到";
        hero.skill4 = "你分开";

        //把对象构造成 json 格式的字符串
        Gson gson = new GsonBuilder().create();
        String jsonString = gson.toJson(hero);
        //json的键值对输出
        //{"name":"曹操","skill1":"hhh","skill2":"Hhh上述","skill3":"到到到","skill4":"你分开"}
        System.out.println(jsonString);

        //把 json 格式的字符串转换为对象
        Hero hero1 = gson.fromJson(jsonString,Hero.class);
        System.out.println(hero1.name);
        System.out.println(hero1.skill1);
        System.out.println(hero1.skill2);
        System.out.println(hero1.skill3);
        System.out.println(hero1.skill4);
    }
}