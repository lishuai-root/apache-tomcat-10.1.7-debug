/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.coyote;

import org.apache.tomcat.util.net.SocketEvent;

/**
 * Adapter. This represents the entry point in a coyote-based servlet container.
 * 适配器。这表示基于coyote的servlet容器中的入口点。
 *
 * @author Remy Maucherat
 *
 * @see ProtocolHandler
 */
public interface Adapter {

    /**
     * Call the service method, and notify all listeners
     * 调用service方法，并通知所有监听器
     *
     * @param req The request object
     * @param res The response object
     *
     * @exception Exception if an error happens during handling of the request. Common errors are:
     *                          <ul>
     *                          <li>IOException if an input/output error occurs and we are processing an included
     *                          servlet (otherwise it is swallowed and handled by the top level error handler mechanism)
     *                          <li>ServletException if a servlet throws an exception and we are processing an included
     *                          servlet (otherwise it is swallowed and handled by the top level error handler mechanism)
     *                          </ul>
     *                          Tomcat should be able to handle and log any other exception ( including runtime
     *                          exceptions )
     */
    void service(Request req, Response res) throws Exception;

    /**
     * Prepare the given request/response for processing. This method requires that the request object has been
     * populated with the information available from the HTTP headers.
     * 为处理准备给定的请求响应。此方法要求请求对象已经用HTTP头中可用的信息填充。
     *
     * @param req The request object
     * @param res The response object
     *
     * @return <code>true</code> if processing can continue, otherwise <code>false</code> in which case an appropriate
     *             error will have been set on the response
     *
     * @throws Exception If the processing fails unexpectedly
     */
    boolean prepare(Request req, Response res) throws Exception;

    boolean asyncDispatch(Request req, Response res, SocketEvent status) throws Exception;

    void log(Request req, Response res, long time);

    /**
     * Assert that request and response have been recycled. If they have not then log a warning and force a recycle.
     * This method is called as a safety check when a processor is being recycled and may be returned to a pool for
     * reuse.
     * 断言请求和响应已被回收。如果没有，则记录警告并强制回收。当处理器被回收并可能返回到池中进行重用时，调用此方法作为安全检查。
     *
     * @param req Request
     * @param res Response
     */
    void checkRecycled(Request req, Response res);

    /**
     * Provide the name of the domain to use to register MBeans for components associated with the connector.
     * 提供要用于为与连接器关联的组件注册mbean的域的名称。
     *
     * @return The MBean domain name
     */
    String getDomain();
}
