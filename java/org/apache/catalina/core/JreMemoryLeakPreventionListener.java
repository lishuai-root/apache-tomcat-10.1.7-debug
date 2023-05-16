/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.catalina.core;

import java.net.URLConnection;
import java.sql.DriverManager;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Server;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;

/**
 * Provide a workaround for known places where the Java Runtime environment can cause a memory leak or lock files.
 * 为Java运行时环境可能导致内存泄漏或锁定文件的已知位置提供解决方案。
 *
 * <p>
 * Memory leaks occur when JRE code uses the context class loader to load a singleton as this will cause a memory leak
 * if a web application class loader happens to be the context class loader at the time. The work-around is to
 * initialise these singletons when Tomcat's common class loader is the context class loader.
 * 当JRE代码使用上下文类加载器加载单例时，就会发生内存泄漏，因为如果web应用程序类加载器恰好是上下文类加载器，则会导致内存泄漏。
 * 解决方法是在Tomcat的公共类装入器是上下文类装入器时初始化这些单例。
 *
 * <p>
 * Locked files usually occur when a resource inside a JAR is accessed without first disabling Jar URL connection
 * caching. The workaround is to disable this caching by default.
 * 锁定文件通常发生在访问JAR中的资源时没有首先禁用JAR URL连接缓存。解决方法是默认禁用此缓存。
 *
 * <p>
 * This listener must only be nested within {@link Server} elements.
 * 此监听器只能嵌套在{@link Server}元素中。
 */
public class JreMemoryLeakPreventionListener implements LifecycleListener {

    private static final Log log = LogFactory.getLog(JreMemoryLeakPreventionListener.class);
    private static final StringManager sm = StringManager.getManager(JreMemoryLeakPreventionListener.class);

    /**
     * Protect against the memory leak caused when the first call to <code>sun.awt.AppContext.getAppContext()</code> is
     * triggered by a web application. Defaults to <code>false</code> since Tomcat code no longer triggers this although
     * application code may.
     * 防止在web应用程序触发对<code>sun.awt.AppContext.getAppContext()<code>的第一次调用时造成内存泄漏。
     * 默认为<code>false<code>，因为Tomcat代码不再触发这个，尽管应用程序代码可以。
     */
    private boolean appContextProtection = false;

    public boolean isAppContextProtection() {
        return appContextProtection;
    }

    public void setAppContextProtection(boolean appContextProtection) {
        this.appContextProtection = appContextProtection;
    }

    /**
     * Protect against resources being read for JAR files and, as a side-effect, the JAR file becoming locked. Note this
     * disables caching for all {@link URLConnection}s, regardless of type. Defaults to <code>true</code>.
     * 防止为JAR文件读取资源，作为副作用，JAR文件被锁定。
     * 注意，这将禁用所有{@link URLConnection}的缓存，无论类型如何。默认为<code>true<code>。
     */
    private boolean urlCacheProtection = true;

    public boolean isUrlCacheProtection() {
        return urlCacheProtection;
    }

    public void setUrlCacheProtection(boolean urlCacheProtection) {
        this.urlCacheProtection = urlCacheProtection;
    }

    /**
     * The first access to {@link DriverManager} will trigger the loading of all {@link java.sql.Driver}s in the the
     * current class loader. The web application level memory leak protection can take care of this in most cases but
     * triggering the loading here has fewer side-effects.
     * 第一次访问{@link DriverManager}将触发所有当前类装入器中的{@link java.sql.Driver}的加载。
     * 在大多数情况下，web应用程序级别的内存泄漏保护可以解决这个问题，但是在这里触发加载的副作用更少。
     */
    private boolean driverManagerProtection = true;

    public boolean isDriverManagerProtection() {
        return driverManagerProtection;
    }

    public void setDriverManagerProtection(boolean driverManagerProtection) {
        this.driverManagerProtection = driverManagerProtection;
    }

    /**
     * List of comma-separated fully qualified class names to load and initialize during the startup of this Listener.
     * This allows to pre-load classes that are known to provoke classloader leaks if they are loaded during a request
     * processing.
     * 在此侦听器启动期间要加载和初始化的以逗号分隔的完全限定类名列表。这允许预加载已知会引发类加载器泄漏的类，如果它们是在请求处理期间加载的。
     */
    private String classesToInitialize = null;

    public String getClassesToInitialize() {
        return classesToInitialize;
    }

    public void setClassesToInitialize(String classesToInitialize) {
        this.classesToInitialize = classesToInitialize;
    }


    @Override
    public void lifecycleEvent(LifecycleEvent event) {
        // Initialise these classes when Tomcat starts
        if (Lifecycle.BEFORE_INIT_EVENT.equals(event.getType())) {
            if (!(event.getLifecycle() instanceof Server)) {
                log.warn(sm.getString("listener.notServer", event.getLifecycle().getClass().getSimpleName()));
            }

            /*
             * First call to this loads all drivers visible to the current class loader and its parents.
             *
             * Note: This is called before the context class loader is changed because we want any drivers located in
             * CATALINA_HOME/lib and/or CATALINA_HOME/lib to be visible to DriverManager. Users wishing to avoid having
             * JDBC drivers loaded by this class loader should add the JDBC driver(s) to the class path so they are
             * loaded by the system class loader.
             */
            if (driverManagerProtection) {
                DriverManager.getDrivers();
            }

            Thread currentThread = Thread.currentThread();
            ClassLoader loader = currentThread.getContextClassLoader();

            try {
                // Use the system classloader as the victim for all this
                // ClassLoader pinning we're about to do.
                currentThread.setContextClassLoader(ClassLoader.getSystemClassLoader());

                /*
                 * Several components end up calling: sun.awt.AppContext.getAppContext()
                 *
                 * Those libraries / components known to trigger memory leaks due to eventual calls to getAppContext()
                 * are: - Google Web Toolkit via its use of javax.imageio - Batik - others TBD
                 *
                 * Note that a call to sun.awt.AppContext.getAppContext() results in a thread being started named
                 * AWT-AppKit that requires a graphical environment to be available.
                 */

                // Trigger a call to sun.awt.AppContext.getAppContext(). This
                // will pin the system class loader in memory but that shouldn't
                // be an issue.
                if (appContextProtection) {
                    ImageIO.getCacheDirectory();
                }

                /*
                 * Several components end up opening JarURLConnections without first disabling caching. This effectively
                 * locks the file. Whilst more noticeable and harder to ignore on Windows, it affects all operating
                 * systems.
                 *
                 * Those libraries/components known to trigger this issue include: - log4j versions 1.2.15 and earlier -
                 * javax.xml.bind.JAXBContext.newInstance()
                 *
                 * https://bugs.openjdk.java.net/browse/JDK-8163449
                 *
                 * Disable caching for JAR URLConnections
                 */

                // Set the default URL caching policy to not to cache
                if (urlCacheProtection) {
                    URLConnection.setDefaultUseCaches("JAR", false);
                }

                if (classesToInitialize != null) {
                    StringTokenizer strTok = new StringTokenizer(classesToInitialize, ", \r\n\t");
                    while (strTok.hasMoreTokens()) {
                        String classNameToLoad = strTok.nextToken();
                        try {
                            Class.forName(classNameToLoad);
                        } catch (ClassNotFoundException e) {
                            log.error(sm.getString("jreLeakListener.classToInitializeFail", classNameToLoad), e);
                            // continue with next class to load
                        }
                    }
                }

            } finally {
                currentThread.setContextClassLoader(loader);
            }
        }
    }
}
