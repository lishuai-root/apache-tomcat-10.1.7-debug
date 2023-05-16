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

import java.util.Set;

/**
 * <p>Interface describing a collection of Valves that should be executed
 * in sequence when the <code>invoke()</code> method is invoked.  It is
 * required that a Valve somewhere in the pipeline (usually the last one)
 * must process the request and create the corresponding response, rather
 * than trying to pass the request on.</p>
 * 接口，描述在调用<code>invoke()<code>方法时应按顺序执行的valve集合。
 * 管道中的某个Valve(通常是最后一个)必须处理请求并创建相应的响应，而不是试图传递请求。
 *
 * <p>There is generally a single Pipeline instance associated with each
 * Container.  The container's normal request processing functionality is
 * generally encapsulated in a container-specific Valve, which should always
 * be executed at the end of a pipeline.  To facilitate this, the
 * <code>setBasic()</code> method is provided to set the Valve instance that
 * will always be executed last.  Other Valves will be executed in the order
 * that they were added, before the basic Valve is executed.</p>
 * 一般来说，每个容器都有一个单独的Pipeline实例。容器的正常请求处理功能通常封装在容器特定的Valve中，它应该始终在管道的末端执行。
 * 为了方便起见，提供了<code>setBasic()<code>方法来设置总是最后执行的Valve实例。在执行基本阀门之前，其他阀门将按照添加的顺序执行。
 * <p>
 *
 *
 * @author Craig R. McClanahan
 * @author Peter Donald
 */
public interface Pipeline extends Contained {

    /**
     * @return the Valve instance that has been distinguished as the basic
     * Valve for this Pipeline (if any).
     */
    Valve getBasic();


    /**
     * <p>Set the Valve instance that has been distinguished as the basic
     * Valve for this Pipeline (if any).  Prior to setting the basic Valve,
     * the Valve's <code>setContainer()</code> will be called, if it
     * implements <code>Contained</code>, with the owning Container as an
     * argument.  The method may throw an <code>IllegalArgumentException</code>
     * if this Valve chooses not to be associated with this Container, or
     * <code>IllegalStateException</code> if it is already associated with
     * a different Container.</p>
     * <p>设置已被区分为该管道的基本阀门的阀门实例(如果有的话)。在设置基本的Valve之前，Valve的<code>setContainer()<code>将被调用，
     * 如果它实现了<code>Contained<code>，拥有的容器作为参数。
     * 如果此阀门选择不与此容器关联，该方法可能抛出<code>IllegalArgumentException<code>，或者<code>IllegalStateException<code>如果它已经与其他容器关联。<p>
     *
     *
     * @param valve Valve to be distinguished as the basic Valve
     */
    void setBasic(Valve valve);


    /**
     * <p>Add a new Valve to the end of the pipeline associated with this
     * Container.  Prior to adding the Valve, the Valve's
     * <code>setContainer()</code> method will be called, if it implements
     * <code>Contained</code>, with the owning Container as an argument.
     * 在与此容器关联的管道末端添加一个新阀门。在添加阀门之前，阀门的<code>setContainer()<code>方法将被调用，
     * 如果它实现了<code>Contained<code>，拥有的容器作为参数。
     *
     * The method may throw an
     * <code>IllegalArgumentException</code> if this Valve chooses not to
     * be associated with this Container, or <code>IllegalStateException</code>
     * if it is already associated with a different Container.</p>
     * 如果此阀门选择不与此容器关联，
     * 该方法可能抛出<code>IllegalArgumentException<code>，或者<code>IllegalStateException<code>如果它已经与其他容器关联。<p>
     *
     *
     * <p>Implementation note: Implementations are expected to trigger the
     * {@link Container#ADD_VALVE_EVENT} for the associated container if this
     * call is successful.</p>
     * <p>实现说明:如果调用成功，则期望实现触发关联容器的{@link Container#ADD_VALVE_EVENT}。<p>
     *
     * @param valve Valve to be added
     *
     * @exception IllegalArgumentException if this Container refused to
     *  accept the specified Valve
     * @exception IllegalArgumentException if the specified Valve refuses to be
     *  associated with this Container
     * @exception IllegalStateException if the specified Valve is already
     *  associated with a different Container
     */
    void addValve(Valve valve);


    /**
     * @return the set of Valves in the pipeline associated with this
     * Container, including the basic Valve (if any).  If there are no
     * such Valves, a zero-length array is returned.
     */
    Valve[] getValves();


    /**
     * Remove the specified Valve from the pipeline associated with this
     * Container, if it is found; otherwise, do nothing.  If the Valve is
     * found and removed, the Valve's <code>setContainer(null)</code> method
     * will be called if it implements <code>Contained</code>.
     * 从与此容器关联的管道中移除指定的阀门(如果找到的话);否则，什么都不做。
     * 如果找到并删除了Valve，如果Valve的<code>setContainer(null)<code>方法实现了<code>Contained<code>，那么它将被调用。
     *
     *
     * <p>Implementation note: Implementations are expected to trigger the
     * {@link Container#REMOVE_VALVE_EVENT} for the associated container if this
     * call is successful.</p>
     *
     * @param valve Valve to be removed
     */
    void removeValve(Valve valve);


    /**
     * @return the Valve instance that has been distinguished as the basic
     * Valve for this Pipeline (if any).
     */
    Valve getFirst();


    /**
     * Returns true if all the valves in this pipeline support async, false otherwise
     * 如果此管道中的所有阀门都支持异步，则返回true，否则返回false
     *
     * @return true if all the valves in this pipeline support async, false otherwise
     */
    boolean isAsyncSupported();


    /**
     * Identifies the Valves, if any, in this Pipeline that do not support
     * async.
     * 标识此管道中不支持异步的阀门(如果有的话)。
     *
     * @param result The Set to which the fully qualified class names of each
     *               Valve in this Pipeline that does not support async will be
     *               added
     */
    void findNonAsyncValves(Set<String> result);
}
