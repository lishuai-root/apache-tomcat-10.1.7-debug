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
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;


/**
 * Host表示虚拟主机，一个Host实例表示一个虚拟主机，Host用来处理对应域名的请求，一个Host可以对应一个或多个域名
 * Host可以通过<Alias></Alias>标签定义别名，别名的作用和域名相同
 *
 * A <b>Host</b> is a Container that represents a virtual host in the
 * Catalina servlet engine.  It is useful in the following types of scenarios:
 * A <b>Host<b>是一个容器，表示Catalina servlet引擎中的虚拟主机。它在以下类型的场景中很有用:
 *
 * <ul>
 * <li>You wish to use Interceptors that see every single request processed
 *     by this particular virtual host.
 *     您希望使用拦截器来查看该特定虚拟主机处理的每个请求。
 *
 * <li>You wish to run Catalina in with a standalone HTTP connector, but still
 *     want support for multiple virtual hosts.
 *     您希望使用独立的HTTP连接器运行Catalina，但仍然希望支持多个虚拟主机。
 *
 * </ul>
 * In general, you would not use a Host when deploying Catalina connected
 * to a web server (such as Apache), because the Connector will have
 * utilized the web server's facilities to determine which Context (or
 * perhaps even which Wrapper) should be utilized to process this request.
 * 一般来说，当部署连接到web服务器(如Apache)的Catalina时，您不会使用Host，
 * 因为Connector将利用web服务器的设施来确定应该使用哪个上下文(甚至可能是哪个Wrapper)来处理此请求。
 *
 * <p>
 * The parent Container attached to a Host is generally an Engine, but may
 * be some other implementation, or may be omitted if it is not necessary.
 * 附加到主机上的父容器通常是一个引擎，但也可能是其他实现，或者在没有必要的情况下可以省略。
 *
 * <p>
 * The child containers attached to a Host are generally implementations
 * of Context (representing an individual servlet context).
 * 附加到Host的子容器通常是Context的实现(表示单个servlet上下文)。
 *
 * @author Craig R. McClanahan
 */
public interface Host extends Container {


    // ----------------------------------------------------- Manifest Constants


    /**
     * The ContainerEvent event type sent when a new alias is added
     * by <code>addAlias()</code>.
     * 当<code>addAlias()<code>添加新别名时发送的ContainerEvent事件类型。
     */
    String ADD_ALIAS_EVENT = "addAlias";


    /**
     * The ContainerEvent event type sent when an old alias is removed
     * by <code>removeAlias()</code>.
     * 当<code>removeAlias()<code>删除旧别名时发送的ContainerEvent事件类型。
     */
    String REMOVE_ALIAS_EVENT = "removeAlias";


    // ------------------------------------------------------------- Properties


    /**
     * @return the XML root for this Host.  This can be an absolute
     * pathname or a relative pathname.
     * 这个主机的XML根。可以是绝对路径名，也可以是相对路径名。
     *
     * If null, the base path defaults to
     * ${catalina.base}/conf/&lt;engine name&gt;/&lt;host name&gt; directory
     * 如果为空，则基本路径默认为${catalina.base}/conf/&lt;engine name&gt;/&lt;host name&gt;目录
     */
    String getXmlBase();

    /**
     * Set the Xml root for this Host.  This can be an absolute
     * pathname or a relative pathname.
     * 为这个主机设置Xml根目录。可以是绝对路径名，也可以是相对路径名。
     *
     * If null, the base path defaults to
     * ${catalina.base}/conf/&lt;engine name&gt;/&lt;host name&gt; directory
     * 如果为空，则基本路径默认为{catalina.base}引擎名&gt;&lt;主机名&gt;目录
     *
     * @param xmlBase The new XML root
     */
    void setXmlBase(String xmlBase);

    /**
     * @return a default configuration path of this Host. The file will be
     * canonical if possible.
     * 该主机的默认配置路径。如果可能的话，该文件将是规范的。
     */
    File getConfigBaseFile();

    /**
     * @return the application root for this Host.  This can be an absolute
     * pathname, a relative pathname, or a URL.
     * 该主机的应用程序根目录。它可以是绝对路径名、相对路径名或URL。
     */
    String getAppBase();


    /**
     * @return an absolute {@link File} for the appBase of this Host. The file
     * will be canonical if possible. There is no guarantee that that the
     * appBase exists.
     */
    File getAppBaseFile();


    /**
     * Set the application root for this Host.  This can be an absolute
     * pathname, a relative pathname, or a URL.
     *
     * @param appBase The new application root
     */
    void setAppBase(String appBase);


    /**
     * @return the legacy (Java EE) application root for this Host.  This can be
     * an absolute pathname, a relative pathname, or a URL.
     */
    String getLegacyAppBase();


    /**
     * @return an absolute {@link File} for the legacy (Java EE) appBase of this
     * Host. The file will be canonical if possible. There is no guarantee that
     * that the appBase exists.
     */
    File getLegacyAppBaseFile();


    /**
     * Set the legacy (Java EE) application root for this Host.  This can be an
     * absolute pathname, a relative pathname, or a URL.
     *
     * @param legacyAppBase The new legacy application root
     */
    void setLegacyAppBase(String legacyAppBase);


    /**
     * @return the value of the auto deploy flag.  If true, it indicates that
     * this host's child webapps should be discovered and automatically
     * deployed dynamically.
     */
    boolean getAutoDeploy();


    /**
     * Set the auto deploy flag value for this host.
     *
     * @param autoDeploy The new auto deploy flag
     */
    void setAutoDeploy(boolean autoDeploy);


    /**
     * @return the Java class name of the context configuration class
     * for new web applications.
     */
    String getConfigClass();


    /**
     * Set the Java class name of the context configuration class
     * for new web applications.
     *
     * @param configClass The new context configuration class
     */
    void setConfigClass(String configClass);


    /**
     * @return the value of the deploy on startup flag.  If true, it indicates
     * that this host's child webapps should be discovered and automatically
     * deployed.
     */
    boolean getDeployOnStartup();


    /**
     * Set the deploy on startup flag value for this host.
     *
     * @param deployOnStartup The new deploy on startup flag
     */
    void setDeployOnStartup(boolean deployOnStartup);


    /**
     * @return the regular expression that defines the files and directories in
     * the host's appBase that will be ignored by the automatic deployment
     * process.
     */
    String getDeployIgnore();


    /**
     * @return the compiled regular expression that defines the files and
     * directories in the host's appBase that will be ignored by the automatic
     * deployment process.
     */
    Pattern getDeployIgnorePattern();


    /**
     * Set the regular expression that defines the files and directories in
     * the host's appBase that will be ignored by the automatic deployment
     * process.
     *
     * @param deployIgnore A regular expression matching file names
     */
    void setDeployIgnore(String deployIgnore);


    /**
     * @return the executor that is used for starting and stopping contexts. This
     * is primarily for use by components deploying contexts that want to do
     * this in a multi-threaded manner.
     */
    ExecutorService getStartStopExecutor();


    /**
     * Returns <code>true</code> if the Host will attempt to create directories for appBase and xmlBase
     * unless they already exist.
     * @return true if the Host will attempt to create directories
     */
    boolean getCreateDirs();


    /**
     * Should the Host attempt to create directories for xmlBase and appBase
     * upon startup.
     *
     * @param createDirs The new value for this flag
     */
    void setCreateDirs(boolean createDirs);


    /**
     * @return <code>true</code> of the Host is configured to automatically undeploy old
     * versions of applications deployed using parallel deployment. This only
     * takes effect is {@link #getAutoDeploy()} also returns <code>true</code>.
     */
    boolean getUndeployOldVersions();


    /**
     * Set to <code>true</code> if the Host should automatically undeploy old versions of
     * applications deployed using parallel deployment. This only takes effect
     * if {@link #getAutoDeploy()} returns <code>true</code>.
     *
     * @param undeployOldVersions The new value for this flag
     */
    void setUndeployOldVersions(boolean undeployOldVersions);


    // --------------------------------------------------------- Public Methods

    /**
     * Add an alias name that should be mapped to this same Host.
     *
     * @param alias The alias to be added
     */
    void addAlias(String alias);


    /**
     * @return the set of alias names for this Host.  If none are defined,
     * a zero length array is returned.
     */
    String[] findAliases();


    /**
     * Remove the specified alias name from the aliases for this Host.
     *
     * @param alias Alias name to be removed
     */
    void removeAlias(String alias);
}
