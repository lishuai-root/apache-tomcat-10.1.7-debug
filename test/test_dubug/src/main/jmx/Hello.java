package test_dubug.src.main.jmx;

/**
 * @description:
 * @author: LISHUAI
 * @createDate: 2023/4/24 21:39
 * @version: 1.0
 */

public class Hello implements HelloMBean{

    String name;

    String age;

    @Override
    public void setName(String name) {
        System.out.println("setName: " + name);
        this.name = name;
    }

    @Override
    public String getName() {
        System.out.println("getName: " + name);
        return this.name;
    }

    @Override
    public void setAge(String age) {
        System.out.println("setAge: " + age);
        this.age = age;
    }

    @Override
    public String getAge() {
        System.out.println("getAge: " + age);
        return this.age;
    }

    @Override
    public void clear() {
        System.out.println("clear.");
    }

    @Override
    public String resources() {
        System.out.println("resources.");
        return "resources";
    }

    @Override
    public String resource(String sourceName) {
        System.out.println("resource : " + sourceName);
        return sourceName;
    }
}
