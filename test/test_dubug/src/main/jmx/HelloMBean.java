package test_dubug.src.main.jmx;

/**
 * @description:
 * @author: LISHUAI
 * @createDate: 2023/4/24 21:38
 * @version: 1.0
 */

public interface HelloMBean {

    public void setName(String name);

    public String getName();

    public void setAge(String age);

    public String getAge();

    public void clear();

    public String resources();

    public String resource(String sourceName);
}
