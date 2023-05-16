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
package org.apache.catalina;

import java.io.File;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.catalina.deploy.NamingResourcesImpl;
import org.apache.catalina.startup.Catalina;

/**
 * server是tomcat中的顶层组件，一个tomcat应用只有一个server实例，即一个server就代表了一个tomcat
 * server默认会通过8005端口监听"SHUTDOWN"指令
 *
 * 一个server组件中可以包含多个service组件
 *
 * A <code>Server</code> element represents the entire Catalina
 * servlet container.  Its attributes represent the characteristics of
 * the servlet container as a whole.  A <code>Server</code> may contain
 * one or more <code>Services</code>, and the top level set of naming
 * resources.
 * <code>Server<code>元素表示整个Catalina servlet容器。
 * 它的属性代表了servlet容器的整体特征。一个<code>Server<code>可以包含一个或多个<code>Services<code>，以及顶级命名资源集。
 *
 * <p>
 * Normally, an implementation of this interface will also implement
 * <code>Lifecycle</code>, such that when the <code>start()</code> and
 * <code>stop()</code> methods are called, all of the defined
 * <code>Services</code> are also started or stopped.
 * 通常，这个接口的实现也将实现<code>Lifecycle<code>，这样当<code>start()<code>和<code>stop()<code>方法被调用时，
 * 所有定义的<code>Services<code>也被启动或停止。
 *
 * <p>
 * In between, the implementation must open a server socket on the port number
 * specified by the <code>port</code> property.  When a connection is accepted,
 * the first line is read and compared with the specified shutdown command.
 * If the command matches, shutdown of the server is initiated.
 * 在此期间，实现必须在<code>port<code>属性指定的端口号上打开服务器套接字。
 * 当接受连接时，读取第一行并与指定的关闭命令进行比较。如果匹配，则启动服务器关机。
 *
 *
 * @author Craig R. McClanahan
 */
public interface Server extends Lifecycle {

    // ------------------------------------------------------------- Properties

    /**
     * @return the global naming resources.
     */
    NamingResourcesImpl getGlobalNamingResources();


    /**
     * Set the global naming resources.
     *
     * @param globalNamingResources The new global naming resources
     */
    void setGlobalNamingResources
        (NamingResourcesImpl globalNamingResources);


    /**
     * @return the global naming resources context.
     */
    javax.naming.Context getGlobalNamingContext();


    /**
     * @return the port number we listen to for shutdown commands.
     *
     * @see #getPortOffset()
     * @see #getPortWithOffset()
     */
    int getPort();


    /**
     * Set the port number we listen to for shutdown commands.
     * 设置我们监听关机命令的端口号。
     *
     * @param port The new port number
     *
     * @see #setPortOffset(int)
     */
    void setPort(int port);

    /**
     * Get the number that offsets the port used for shutdown commands.
     * For example, if port is 8005, and portOffset is 1000,
     * the server listens at 9005.
     * 获取用于关闭命令的端口偏移的数字。例如，如果端口为8005，端口偏移为1000，则服务器在9005侦听。
     *
     * @return the port offset
     */
    int getPortOffset();

    /**
     * Set the number that offsets the server port used for shutdown commands.
     * For example, if port is 8005, and you set portOffset to 1000,
     * connector listens at 9005.
     *
     * @param portOffset sets the port offset
     */
    void setPortOffset(int portOffset);

    /**
     * Get the actual port on which server is listening for the shutdown commands.
     * If you do not set port offset, port is returned. If you set
     * port offset, port offset + port is returned.
     *
     * @return the port with offset
     */
    int getPortWithOffset();

    /**
     * @return the address on which we listen to for shutdown commands.
     */
    String getAddress();


    /**
     * Set the address on which we listen to for shutdown commands.
     *
     * @param address The new address
     */
    void setAddress(String address);


    /**
     * @return the shutdown command string we are waiting for.
     */
    String getShutdown();


    /**
     * Set the shutdown command we are waiting for.
     *
     * @param shutdown The new shutdown command
     */
    void setShutdown(String shutdown);


    /**
     * @return the parent class loader for this component. If not set, return
     * {@link #getCatalina()} {@link Catalina#getParentClassLoader()}. If
     * catalina has not been set, return the system class loader.
     */
    ClassLoader getParentClassLoader();


    /**
     * Set the parent class loader for this server.
     *
     * @param parent The new parent class loader
     */
    void setParentClassLoader(ClassLoader parent);


    /**
     * @return the outer Catalina startup/shutdown component if present.
     */
    Catalina getCatalina();

    /**
     * Set the outer Catalina startup/shutdown component if present.
     * 设置外部Catalina startup/shutdown组件(如果存在)。
     *
     * @param catalina the outer Catalina component
     */
    void setCatalina(Catalina catalina);


    /**
     * @return the configured base (instance) directory. Note that home and base
     * may be the same (and are by default). If this is not set the value
     * returned by {@link #getCatalinaHome()} will be used.
     */
    File getCatalinaBase();

    /**
     * Set the configured base (instance) directory. Note that home and base
     * may be the same (and are by default).
     *
     * @param catalinaBase the configured base directory
     */
    void setCatalinaBase(File catalinaBase);


    /**
     * @return the configured home (binary) directory. Note that home and base
     * may be the same (and are by default).
     */
    File getCatalinaHome();

    /**
     * Set the configured home (binary) directory. Note that home and base
     * may be the same (and are by default).
     *
     * @param catalinaHome the configured home directory
     */
    void setCatalinaHome(File catalinaHome);


    /**
     * Get the utility thread count.
     * @return the thread count
     */
    int getUtilityThreads();


    /**
     * Set the utility thread count.
     * @param utilityThreads the new thread count
     */
    void setUtilityThreads(int utilityThreads);


    // --------------------------------------------------------- Public Methods


    /**
     * Add a new Service to the set of defined Services.
     *
     * @param service The Service to be added
     */
    void addService(Service service);


    /**
     * Wait until a proper shutdown command is received, then return.
     */
    void await();


    /**
     * Find the specified Service
     *
     * @param name Name of the Service to be returned
     * @return the specified Service, or <code>null</code> if none exists.
     */
    Service findService(String name);


    /**
     * @return the set of Services defined within this Server.
     */
    Service[] findServices();


    /**
     * Remove the specified Service from the set associated from this
     * Server.
     *
     * @param service The Service to be removed
     */
    void removeService(Service service);


    /**
     * @return the token necessary for operations on the associated JNDI naming
     * context.
     */
    Object getNamingToken();

    /**
     * @return the utility executor managed by the Service.
     */
    ScheduledExecutorService getUtilityExecutor();

}
