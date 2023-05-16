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

import java.io.IOException;

import jakarta.servlet.ServletException;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;

/**
 * Valve负责在请求发送到应用之前拦截HTTP请求，可以定义在任何容器中。
 * 默认配置中定义了一个AccessLogValve，负责拦截HTTP请求，并写入到日志文件中。
 *
 * <p>A <b>Valve</b> is a request processing component associated with a
 * particular Container.  A series of Valves are generally associated with
 * each other into a Pipeline.  The detailed contract for a Valve is included
 * in the description of the <code>invoke()</code> method below.</p>
 * <p>A <b>Valve<b>是与特定容器关联的请求处理组件。一系列阀门通常相互关联成一个管道。
 * 下面的<code>invoke()<code>方法的描述中包含了Valve的详细合同
 *
 *
 * <b>HISTORICAL NOTE</b>:  The "Valve" name was assigned to this concept
 * because a valve is what you use in a real world pipeline to control and/or
 * modify flows through it.
 * <b>历史注释<b>:“阀门”这个名称被分配给这个概念，因为阀门是你在现实世界的管道中用来控制或修改流经它的流的东西。
 *
 * @author Craig R. McClanahan
 * @author Gunnar Rjnning
 * @author Peter Donald
 */
public interface Valve {


    //-------------------------------------------------------------- Properties

    /**
     * @return the next Valve in the pipeline containing this Valve, if any.
     */
    Valve getNext();


    /**
     * Set the next Valve in the pipeline containing this Valve.
     *
     * @param valve The new next valve, or <code>null</code> if none
     */
    void setNext(Valve valve);


    //---------------------------------------------------------- Public Methods


    /**
     * Execute a periodic task, such as reloading, etc. This method will be
     * invoked inside the classloading context of this container. Unexpected
     * throwables will be caught and logged.
     */
    void backgroundProcess();


    /**
     * <p>Perform request processing as required by this Valve.</p>
     * <p>按此阀门的要求执行请求处理。<p>
     *
     * <p>An individual Valve <b>MAY</b> perform the following actions, in
     * the specified order:</p>
     * <p>单个阀门<b>可<b>按指定的顺序执行以下操作:<p>
     * <ul>
     * <li>Examine and/or modify the properties of the specified Request and
     *     Response.
     *     检查或修改指定请求和响应的属性。
     *
     * <li>Examine the properties of the specified Request, completely generate
     *     the corresponding Response, and return control to the caller.
     *     检查指定请求的属性，完全生成相应的响应，并将控制权返回给调用方。
     *
     * <li>Examine the properties of the specified Request and Response, wrap
     *     either or both of these objects to supplement their functionality,
     *     and pass them on.
     *     检查指定的Request和Response的属性，包装其中一个或两个对象以补充其功能，然后传递它们。
     *
     * <li>If the corresponding Response was not generated (and control was not
     *     returned, call the next Valve in the pipeline (if there is one) by
     *     executing <code>getNext().invoke()</code>.
     *     如果没有生成相应的Response(并且没有返回控制)，则通过执行<code>getNext().invoke()<code>来调用管道中的下一个Valve(如果有)。
     *
     * <li>Examine, but not modify, the properties of the resulting Response
     *     (which was created by a subsequently invoked Valve or Container).
     *     检查(但不修改)结果响应(由随后调用的Valve或Container创建)的属性。
     *
     * </ul>
     *
     * <p>A Valve <b>MUST NOT</b> do any of the following things:</p>
     * <p>A阀<b>绝对不能<b>做以下任何事情:<p>
     *
     * <ul>
     * <li>Change request properties that have already been used to direct
     *     the flow of processing control for this request (for instance,
     *     trying to change the virtual host to which a Request should be
     *     sent from a pipeline attached to a Host or Context in the
     *     standard implementation).
     *     更改已经用于指导此请求的处理控制流的请求属性(例如，尝试更改应将请求从连接到标准实现中的主机或上下文的管道发送到的虚拟主机)。
     *
     * <li>Create a completed Response <strong>AND</strong> pass this
     *     Request and Response on to the next Valve in the pipeline.
     *     <li>创建一个完整的响应<strong>AND<strong>将此请求和响应传递到管道中的下一个阀门。
     *
     * <li>Consume bytes from the input stream associated with the Request,
     *     unless it is completely generating the response, or wrapping the
     *     request before passing it on.
     *     从与请求相关联的输入流中消耗字节，除非它完全生成响应，或者在传递请求之前包装请求。
     *
     * <li>Modify the HTTP headers included with the Response after the
     *     <code>getNext().invoke()</code> method has returned.
     *     <li>在<code>getNext().invoke()<code>方法返回后修改响应中包含的HTTP标头。
     *
     * <li>Perform any actions on the output stream associated with the
     *     specified Response after the <code>getNext().invoke()</code> method has
     *     returned.
     *     在<code>getNext().invoke()<code>方法返回后，对与指定的Response关联的输出流执行任何操作。
     * </ul>
     *
     * @param request The servlet request to be processed
     * @param response The servlet response to be created
     *
     * @exception IOException if an input/output error occurs, or is thrown
     *  by a subsequently invoked Valve, Filter, or Servlet
     *  如果发生输入输出错误，或者由随后调用的阀门、过滤器或Servlet抛出
     *
     * @exception ServletException if a servlet error occurs, or is thrown
     *  by a subsequently invoked Valve, Filter, or Servlet
     *  如果发生servlet错误，或由随后调用的阀门、过滤器或servlet抛出
     */
    void invoke(Request request, Response response)
        throws IOException, ServletException;


    boolean isAsyncSupported();
}
