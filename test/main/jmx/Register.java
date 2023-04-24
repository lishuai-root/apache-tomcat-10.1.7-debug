package main.jmx;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

/**
 * @description:
 * @author: LISHUAI
 * @createDate: 2023/4/24 21:42
 * @version: 1.0
 */

public class Register {

    public static void main(String[] args) throws Exception {
        register();
    }

    public static void register() throws Exception {
        String jmxName = "jxm:type=hello,name=hello_001";
        ObjectName objectName = new ObjectName(jmxName);
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        server.registerMBean(new Hello(), objectName);
        Thread.sleep(1000 * 60 * 60);
    }

}
