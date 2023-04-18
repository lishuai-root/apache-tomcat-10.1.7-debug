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

import java.beans.PropertyChangeListener;
import java.io.File;

import javax.management.ObjectName;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.juli.logging.Log;


/**
 * A <b>Container</b> is an object that can execute requests received from
 * a client, and return responses based on those requests.  A Container may
 * optionally support a pipeline of Valves that process the request in an
 * order configured at runtime, by implementing the <b>Pipeline</b> interface
 * as well.
 * A <b>容器<b>是一个对象，它可以执行从客户机接收到的请求，并根据这些请求返回响应。
 * 通过实现<b> pipeline <b>接口，容器也可以选择支持以运行时配置的顺序处理请求的阀门管道。
 *
 * <p>
 * Containers will exist at several conceptual levels within Catalina.  The
 * following examples represent common cases:
 * 容器将在Catalina的几个概念层面上存在。下面是一些常见的例子:
 * <ul>
 * <li><b>Engine</b> - Representation of the entire Catalina servlet engine,
 *     most likely containing one or more subcontainers that are either Host
 *     or Context implementations, or other custom groups.
 *     <b>引擎</b> - 整个Catalina servlet引擎的表示，很可能包含一个或多个子容器，
 *     这些子容器要么是主机实现，要么是上下文实现，要么是其他自定义组。
 *
 * <li><b>Host</b> - Representation of a virtual host containing a number
 *     of Contexts.
 *     <b>Host</b> - 包含多个上下文的虚拟主机的表示形式。
 *
 * <li><b>Context</b> - Representation of a single ServletContext, which will
 *     typically contain one or more Wrappers for the supported servlets.
 *     <b>Context</b> - 单个ServletContext的表示，它通常包含一个或多个支持servlet的wrapper。
 *
 * <li><b>Wrapper</b> - Representation of an individual servlet definition.
 *     <b>Wrapper</b> - 单个servlet定义的表示。
 * </ul>
 * A given deployment of Catalina need not include Containers at all of the
 * levels described above.  For example, an administration application
 * embedded within a network device (such as a router) might only contain
 * a single Context and a few Wrappers, or even a single Wrapper if the
 * application is relatively small.  Therefore, Container implementations
 * need to be designed so that they will operate correctly in the absence
 * of parent Containers in a given deployment.
 * 给定的Catalina部署不需要包括上述所有级别的容器。
 * 例如，嵌入在网络设备(如路由器)中的管理应用程序可能只包含一个Context和几个Wrapper，如果应用程序相对较小，
 * 甚至可能只包含一个Wrapper。因此，需要对容器实现进行设计，使其在给定部署中没有父容器的情况下也能正确运行。
 *
 * <p>
 * A Container may also be associated with a number of support components
 * that provide functionality which might be shared (by attaching it to a
 * parent Container) or individually customized.  The following support
 * components are currently recognized:
 * 容器还可以与许多提供功能的支持组件相关联，这些功能可以共享(通过将其附加到父容器上)或单独定制。目前认可的支持组件如下:
 *
 * <ul>
 * <li><b>Loader</b> - Class loader to use for integrating new Java classes
 *     for this Container into the JVM in which Catalina is running.
 *     <b>Loader</b> - 类装入器，用于将此容器的新Java类集成到运行Catalina的JVM中。
 *
 * <li><b>Logger</b> - Implementation of the <code>log()</code> method
 *     signatures of the <code>ServletContext</code> interface.
 *     <b>Logger</b> - 实现了<code>log()<code>方法签名的<code>ServletContext<code>接口。
 *
 * <li><b>Manager</b> - Manager for the pool of Sessions associated with
 *     this Container.
 *     <b>Manager</b> - 与此容器关联的会话池的管理器。
 *
 * <li><b>Realm</b> - Read-only interface to a security domain, for
 *     authenticating user identities and their corresponding roles.
 *     <b>Realm</b> - 安全域的只读接口，用于对用户身份及其对应的角色进行认证。
 *
 * <li><b>Resources</b> - JNDI directory context enabling access to static
 *     resources, enabling custom linkages to existing server components when
 *     Catalina is embedded in a larger server.
 *     <b>Resources</b> - JNDI目录上下文支持对静态资源的访问，当Catalina嵌入到更大的服务器中时，支持对现有服务器组件的自定义链接。
 *
 * </ul>
 *
 * @author Craig R. McClanahan
 * @author Remy Maucherat
 */
public interface Container extends Lifecycle {


    // ----------------------------------------------------- Manifest Constants


    /**
     * The ContainerEvent event type sent when a child container is added
     * by <code>addChild()</code>.
     * 当<code>addChild()<code>添加子容器时发送的ContainerEvent事件类型。
     */
    String ADD_CHILD_EVENT = "addChild";


    /**
     * The ContainerEvent event type sent when a valve is added
     * by <code>addValve()</code>, if this Container supports pipelines.
     * 如果容器支持管道，则当<code>addValve()<code>添加阀门时发送的ContainerEvent事件类型。
     */
    String ADD_VALVE_EVENT = "addValve";


    /**
     * The ContainerEvent event type sent when a child container is removed
     * by <code>removeChild()</code>.
     * 当子容器被<code>removeChild()<code>移除时发送的ContainerEvent事件类型。
     */
    String REMOVE_CHILD_EVENT = "removeChild";


    /**
     * The ContainerEvent event type sent when a valve is removed
     * by <code>removeValve()</code>, if this Container supports pipelines.
     * 当阀门被<code>removeValve()<code>移除时发送的ContainerEvent事件类型，如果此容器支持管道。
     */
    String REMOVE_VALVE_EVENT = "removeValve";


    // ------------------------------------------------------------- Properties

    /**
     * Obtain the log to which events for this container should be logged.
     * 获取应该记录此容器的事件的日志。
     *
     * @return The Logger with which this Container is associated.  If there is
     *         no associated Logger, return the Logger associated with the
     *         parent Container (if any); otherwise return <code>null</code>.
     *         与此容器相关联的记录器。如果没有关联的Logger，则返回与父容器关联的Logger(如果有);否则返回<代码>null<代码>。
     */
    Log getLogger();


    /**
     * Return the logger name that the container will use.
     * 返回容器将使用的记录器名称。
     *
     * @return the abbreviated name of this container for logging messages
     */
    String getLogName();


    /**
     * Obtain the JMX name for this container.
     * 获取该容器的JMX名称。
     *
     * @return the JMX name associated with this container.
     */
    ObjectName getObjectName();


    /**
     * Obtain the JMX domain under which this container will be / has been
     * registered.
     * 获取将在其中注册此容器的JMX域。
     *
     * @return The JMX domain name
     */
    String getDomain();


    /**
     * Calculate the key properties string to be added to an object's
     * {@link ObjectName} to indicate that it is associated with this container.
     * 计算要添加到对象{@link ObjectName}的关键属性字符串，以表明它与此容器相关联。
     *
     * @return          A string suitable for appending to the ObjectName
     *
     */
    String getMBeanKeyProperties();


    /**
     * Return the Pipeline object that manages the Valves associated with
     * this Container.
     * 返回管理与此容器关联的阀门的Pipeline对象。
     *
     * @return The Pipeline
     */
    Pipeline getPipeline();


    /**
     * Get the Cluster for this container.
     * 获取此容器的Cluster。
     *
     * @return The Cluster with which this Container is associated. If there is
     *         no associated Cluster, return the Cluster associated with our
     *         parent Container (if any); otherwise return <code>null</code>.
     */
    Cluster getCluster();


    /**
     * Set the Cluster with which this Container is associated.
     *
     * @param cluster the Cluster with which this Container is associated.
     */
    void setCluster(Cluster cluster);


    /**
     * Get the delay between the invocation of the backgroundProcess method on
     * this container and its children. Child containers will not be invoked if
     * their delay value is positive (which would mean they are using their own
     * thread). Setting this to a positive value will cause a thread to be
     * spawned. After waiting the specified amount of time, the thread will
     * invoke the {@link #backgroundProcess()} method on this container and all
     * children with non-positive delay values.
     *
     * 获取在此容器上调用backgroundProcess方法与其子容器之间的延迟。
     * 如果子容器的延迟值为正值(这意味着它们正在使用自己的线程)，则不会调用子容器。将其设置为正值将导致生成一个线程。
     * 等待指定的时间后，线程将调用该容器和所有延迟值为非正的子容器的{@link #backgroundProcess()}方法。
     *
     *
     * @return The delay between the invocation of the backgroundProcess method
     *         on this container and its children. A non-positive value
     *         indicates that background processing will be managed by the
     *         parent.
     */
    int getBackgroundProcessorDelay();


    /**
     * Set the delay between the invocation of the execute method on this
     * container and its children.
     * 设置在此容器上调用execute方法与其子容器之间的延迟。
     *
     * @param delay The delay in seconds between the invocation of
     *              backgroundProcess methods
     */
    void setBackgroundProcessorDelay(int delay);


    /**
     * Return a name string (suitable for use by humans) that describes this
     * Container.  Within the set of child containers belonging to a particular
     * parent, Container names must be unique.
     * 返回描述此容器的名称字符串(适合人类使用)。在属于特定父容器的子容器集中，容器名称必须是唯一的。
     *
     * @return The human readable name of this container.
     */
    String getName();


    /**
     * Set a name string (suitable for use by humans) that describes this
     * Container.  Within the set of child containers belonging to a particular
     * parent, Container names must be unique.
     * 设置描述此容器的名称字符串(适合人类使用)。在属于特定父容器的子容器集中，容器名称必须是唯一的。
     *
     * @param name New name of this container
     *
     * @exception IllegalStateException if this Container has already been
     *  added to the children of a parent Container (after which the name
     *  may not be changed)
     */
    void setName(String name);


    /**
     * Get the parent container.
     * 获取父容器。
     *
     * @return Return the Container for which this Container is a child, if
     *         there is one. If there is no defined parent, return
     *         <code>null</code>.
     */
    Container getParent();


    /**
     * Set the parent Container to which this Container is being added as a
     * child.  This Container may refuse to become attached to the specified
     * Container by throwing an exception.
     * 将此容器添加到的父容器设置为子容器。此容器可能会抛出异常，拒绝附加到指定的容器。
     *
     * @param container Container to which this Container is being added
     *  as a child
     *
     * @exception IllegalArgumentException if this Container refuses to become
     *  attached to the specified Container
     */
    void setParent(Container container);


    /**
     * Get the parent class loader.
     * 获取父类装入器。
     *
     * @return the parent class loader for this component. If not set, return
     *         {@link #getParent()}.{@link #getParentClassLoader()}. If no
     *         parent has been set, return the system class loader.
     *         此组件的父类装入器。如果没有设置，则返回{@link #getParent()}.{@link #getParentClassLoader()}。
     *         如果没有设置父类，则返回系统类加载器。
     */
    ClassLoader getParentClassLoader();


    /**
     * Set the parent class loader for this component. For {@link Context}s
     * this call is meaningful only <strong>before</strong> a Loader has
     * been configured, and the specified value (if non-null) should be
     * passed as an argument to the class loader constructor.
     * 为这个组件设置父类加载器。
     * 对于{@link Context}来说，这个调用只有在<strong> >加载器被配置之前才有意义，并且指定的值(如果非空)应该作为参数传递给类加载器构造器。
     *
     *
     * @param parent The new parent class loader
     */
    void setParentClassLoader(ClassLoader parent);


    /**
     * Obtain the Realm with which this Container is associated.
     * 获取与此容器相关联的领域。
     *
     * @return The associated Realm; if there is no associated Realm, the
     *         Realm associated with the parent Container (if any); otherwise
     *         return <code>null</code>.
     */
    Realm getRealm();


    /**
     * Set the Realm with which this Container is associated.
     *
     * @param realm The newly associated Realm
     */
    void setRealm(Realm realm);


    /**
     * Find the configuration path where a configuration resource
     * is located.
     * 查找配置资源所在的配置路径。
     *
     * @param container The container
     * @param resourceName The resource file name
     * @return the configuration path
     */
    static String getConfigPath(Container container, String resourceName) {
        StringBuilder result = new StringBuilder();
        Container host = null;
        Container engine = null;
        while (container != null) {
            if (container instanceof Host) {
                host = container;
            } else if (container instanceof Engine) {
                engine = container;
            }
            container = container.getParent();
        }
        if (host != null && ((Host) host).getXmlBase() != null) {
            result.append(((Host) host).getXmlBase()).append('/');
        } else {
            result.append("conf/");
            if (engine != null) {
                result.append(engine.getName()).append('/');
            }
            if (host != null) {
                result.append(host.getName()).append('/');
            }
        }
        result.append(resourceName);
        return result.toString();
    }


    /**
     * Return the Service to which this container belongs.
     * 返回此容器所属的服务。
     *
     * @param container The container to start from
     * @return the Service, or null if not found
     */
    static Service getService(Container container) {
        while (container != null && !(container instanceof Engine)) {
            container = container.getParent();
        }
        if (container == null) {
            return null;
        }
        return ((Engine) container).getService();
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Execute a periodic task, such as reloading, etc. This method will be
     * invoked inside the classloading context of this container. Unexpected
     * throwables will be caught and logged.
     * 执行周期性任务，如重新加载等。此方法将在此容器的类加载上下文中调用。意外的抛出将被捕获并记录。
     */
    void backgroundProcess();


    /**
     * Add a new child Container to those associated with this Container,
     * if supported.  Prior to adding this Container to the set of children,
     * the child's <code>setParent()</code> method must be called, with this
     * Container as an argument.  This method may thrown an
     * <code>IllegalArgumentException</code> if this Container chooses not
     * to be attached to the specified Container, in which case it is not added
     * 如果支持，向与此容器关联的子容器添加一个新的子容器。
     * 在将此容器添加到子集合之前，必须调用子容器的<code>setParent()<code>方法，并将此容器作为参数。
     * 这个方法可能会抛出一个<code>IllegalArgumentException<code>如果这个容器选择不被附加到指定的容器，在这种情况下，它没有被添加
     *
     *
     * @param child New child Container to be added
     *
     * @exception IllegalArgumentException if this exception is thrown by
     *  the <code>setParent()</code> method of the child Container
     * @exception IllegalArgumentException if the new child does not have
     *  a name unique from that of existing children of this Container
     * @exception IllegalStateException if this Container does not support
     *  child Containers
     */
    void addChild(Container child);


    /**
     * Add a container event listener to this component.
     * 向此组件添加容器事件侦听器。
     *
     * @param listener The listener to add
     */
    void addContainerListener(ContainerListener listener);


    /**
     * Add a property change listener to this component.
     * 向此组件添加属性更改侦听器。
     *
     * @param listener The listener to add
     */
    void addPropertyChangeListener(PropertyChangeListener listener);


    /**
     * Obtain a child Container by name.
     * 按名称获取子容器。
     *
     * @param name Name of the child Container to be retrieved
     *
     * @return The child Container with the given name or <code>null</code> if
     *         no such child exists.
     */
    Container findChild(String name);


    /**
     * Obtain the child Containers associated with this Container.
     * 获取与此容器关联的子容器。
     *
     * @return An array containing all children of this container. If this
     *         Container has no children, a zero-length array is returned.
     *         包含此容器的所有子元素的数组。如果此容器没有子容器，则返回一个零长度数组。
     */
    Container[] findChildren();


    /**
     * Obtain the container listeners associated with this Container.
     * 获取与此容器关联的容器侦听器。
     *
     * @return An array containing the container listeners associated with this
     *         Container. If this Container has no registered container
     *         listeners, a zero-length array is returned.
     *         包含与此容器关联的容器侦听器的数组。如果此容器没有注册的容器侦听器，则返回一个零长度数组。
     */
    ContainerListener[] findContainerListeners();


    /**
     * Remove an existing child Container from association with this parent
     * Container.
     * 从与父容器的关联中删除现有子容器。
     *
     * @param child Existing child Container to be removed
     */
    void removeChild(Container child);


    /**
     * Remove a container event listener from this component.
     * 从此组件中删除容器事件侦听器。
     *
     * @param listener The listener to remove
     */
    void removeContainerListener(ContainerListener listener);


    /**
     * Remove a property change listener from this component.
     * 从该组件中删除属性更改侦听器。
     *
     * @param listener The listener to remove
     */
    void removePropertyChangeListener(PropertyChangeListener listener);


    /**
     * Notify all container event listeners that a particular event has
     * occurred for this Container.  The default implementation performs
     * this notification synchronously using the calling thread.
     * 通知所有容器事件侦听器此容器发生了特定事件。默认实现使用调用线程同步执行此通知。
     *
     * @param type Event type
     * @param data Event data
     */
    void fireContainerEvent(String type, Object data);


    /**
     * Log a request/response that was destined for this container but has been
     * handled earlier in the processing chain so that the request/response
     * still appears in the correct access logs.
     * 记录预定用于此容器的请求响应，但已在处理链中较早处理，以便请求响应仍然出现在正确的访问日志中。
     *
     * @param request       Request (associated with the response) to log
     * @param response      Response (associated with the request) to log
     * @param time          Time taken to process the request/response in
     *                      milliseconds (use 0 if not known)
     * @param   useDefault  Flag that indicates that the request/response should
     *                      be logged in the engine's default access log
     */
    void logAccess(Request request, Response response, long time,
            boolean useDefault);


    /**
     * Obtain the AccessLog to use to log a request/response that is destined
     * for this container. This is typically used when the request/response was
     * handled (and rejected) earlier in the processing chain so that the
     * request/response still appears in the correct access logs.
     * 获取AccessLog以用于记录发送到此容器的请求响应。
     * 这通常用于处理链中较早的请求响应被处理(和拒绝)时，以便请求响应仍然出现在正确的访问日志中。
     *
     * @return The AccessLog to use for a request/response destined for this
     *         container
     */
    AccessLog getAccessLog();


    /**
     * Obtain the number of threads available for starting and stopping any
     * children associated with this container. This allows start/stop calls to
     * children to be processed in parallel.
     * 获取可用于启动和停止与此容器关联的任何子线程的线程数。这允许并行处理对子进程的start/stop调用。
     *
     * @return The currently configured number of threads used to start/stop
     *         children associated with this container
     */
    int getStartStopThreads();


    /**
     * Sets the number of threads available for starting and stopping any
     * children associated with this container. This allows start/stop calls to
     * children to be processed in parallel.
     * 设置可用于启动和停止与此容器关联的任何子线程的线程数。这允许并行处理对子进程的start/stop调用。
     *
     * @param   startStopThreads    The new number of threads to be used
     */
    void setStartStopThreads(int startStopThreads);


    /**
     * Obtain the location of CATALINA_BASE.
     * 获取CATALINA_BASE所在位置。
     *
     * @return  The location of CATALINA_BASE.
     */
    File getCatalinaBase();


    /**
     * Obtain the location of CATALINA_HOME.
     * 获取CATALINA_HOME的位置。
     *
     * @return The location of CATALINA_HOME.
     */
    File getCatalinaHome();
}
