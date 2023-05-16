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

/**
 * Engine:通过域名将请求映射到对应的Host
 * Engine是请求的处理引擎，负责处理Connector发送过来的请求，并将处理完成的结果返回给Connector，
 * 一个Engine中可以由多个Host容器，每个Host容器可以通过name属性指定可以处理的请求域名
 *
 * Engine中必须要有一个name属性和defaultHost相同的Host
 *
 * Engine通过域名匹配处理当前请求的Host主机，如果没有就通过defaultHost属性指定的虚拟主机处理当前请求
 *
 * An <b>Engine</b> is a Container that represents the entire Catalina servlet
 * engine.  It is useful in the following types of scenarios:
 * <b>引擎<b>是一个容器，它代表了整个Catalina servlet引擎。它在以下类型的场景中很有用:
 *
 * <ul>
 * <li>You wish to use Interceptors that see every single request processed
 *     by the entire engine.
 *     你希望使用拦截器来查看整个引擎处理的每个请求。
 *
 * <li>You wish to run Catalina in with a standalone HTTP connector, but still
 *     want support for multiple virtual hosts.
 *     您希望使用独立的HTTP连接器运行Catalina，但仍然希望支持多个虚拟主机。
 * </ul>
 * In general, you would not use an Engine when deploying Catalina connected
 * to a web server (such as Apache), because the Connector will have
 * utilized the web server's facilities to determine which Context (or
 * perhaps even which Wrapper) should be utilized to process this request.
 * 通常，在部署连接到web服务器(如Apache)的Catalina时，您不会使用引擎，
 * 因为连接器将利用web服务器的功能来确定应该使用哪个上下文(甚至可能是哪个包装器)来处理此请求。
 *
 * <p>
 * The child containers attached to an Engine are generally implementations
 * of Host (representing a virtual host) or Context (representing individual
 * an individual servlet context), depending upon the Engine implementation.
 * 附加到引擎的子容器通常是主机(表示虚拟主机)或上下文(表示单个servlet上下文)的实现，具体取决于引擎实现。
 *
 * <p>
 * If used, an Engine is always the top level Container in a Catalina
 * hierarchy. Therefore, the implementation's <code>setParent()</code> method
 * should throw <code>IllegalArgumentException</code>.
 * 如果使用了引擎，它总是Catalina层次结构中的顶层容器。
 * 因此，实现的<code>setParent()<code>方法应该抛出<code>IllegalArgumentException<code>。
 *
 * @author Craig R. McClanahan
 */
public interface Engine extends Container {

    /**
     * @return the default host name for this Engine.
     */
    String getDefaultHost();


    /**
     * Set the default hostname for this Engine.
     *
     * @param defaultHost The new default host
     */
    void setDefaultHost(String defaultHost);


    /**
     * @return the JvmRouteId for this engine.
     */
    String getJvmRoute();


    /**
     * Set the JvmRouteId for this engine.
     *
     * @param jvmRouteId the (new) JVM Route ID. Each Engine within a cluster
     *        must have a unique JVM Route ID.
     */
    void setJvmRoute(String jvmRouteId);


    /**
     * @return the <code>Service</code> with which we are associated (if any).
     */
    Service getService();


    /**
     * Set the <code>Service</code> with which we are associated (if any).
     *
     * @param service The service that owns this Engine
     */
    void setService(Service service);
}
