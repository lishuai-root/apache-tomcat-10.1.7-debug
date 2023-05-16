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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.management.ObjectName;

import org.apache.catalina.Contained;
import org.apache.catalina.Container;
import org.apache.catalina.JmxEnabled;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Pipeline;
import org.apache.catalina.Valve;
import org.apache.catalina.util.LifecycleBase;
import org.apache.catalina.util.ToStringUtil;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.res.StringManager;

/**
 * Standard implementation of a processing <b>Pipeline</b> that will invoke a series of Valves that have been configured
 * to be called in order. This implementation can be used for any type of Container. <b>IMPLEMENTATION WARNING</b> -
 * This implementation assumes that no calls to <code>addValve()</code> or <code>removeValve</code> are allowed while a
 * request is currently being processed. Otherwise, the mechanism by which per-thread state is maintained will need to
 * be modified.
 * 处理<b>管道<b>的标准实现，它将调用一系列已配置为按顺序调用的阀门。此实现可用于任何类型的容器。
 * <b>IMPLEMENTATION WARNING<b>
 *     -此实现假设当前正在处理请求时不允许调用<code>addValve()<code>或<code>removeValve<code>。否则，将需要修改维护每个线程状态的机制。
 *
 *
 * @author Craig R. McClanahan
 */
public class StandardPipeline extends LifecycleBase implements Pipeline {

    private static final Log log = LogFactory.getLog(StandardPipeline.class);
    private static final StringManager sm = StringManager.getManager(StandardPipeline.class);

    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new StandardPipeline instance with no associated Container.
     */
    public StandardPipeline() {

        this(null);

    }


    /**
     * Construct a new StandardPipeline instance that is associated with the specified Container.
     *
     * @param container The container we should be associated with
     */
    public StandardPipeline(Container container) {

        super();
        setContainer(container);

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The basic Valve (if any) associated with this Pipeline.
     * 与此管道相关联的基本阀门(如果有的话)。
     *
     * 基本阀门用来调起请求处理的web应用程序接口
     */
    protected Valve basic = null;


    /**
     * The Container with which this Pipeline is associated.
     * 与此管道相关联的容器。
     */
    protected Container container = null;


    /**
     * The first valve associated with this Pipeline.
     */
    protected Valve first = null;


    // --------------------------------------------------------- Public Methods

    @Override
    public boolean isAsyncSupported() {
        Valve valve = (first != null) ? first : basic;
        boolean supported = true;
        while (supported && valve != null) {
            supported = supported & valve.isAsyncSupported();
            valve = valve.getNext();
        }
        return supported;
    }


    @Override
    public void findNonAsyncValves(Set<String> result) {
        Valve valve = (first != null) ? first : basic;
        while (valve != null) {
            if (!valve.isAsyncSupported()) {
                result.add(valve.getClass().getName());
            }
            valve = valve.getNext();
        }
    }


    // ------------------------------------------------------ Contained Methods

    /**
     * Return the Container with which this Pipeline is associated.
     * 返回与此管道相关联的容器。
     */
    @Override
    public Container getContainer() {
        return this.container;
    }


    /**
     * Set the Container with which this Pipeline is associated.
     *
     * @param container The new associated container
     */
    @Override
    public void setContainer(Container container) {
        this.container = container;
    }


    @Override
    protected void initInternal() {
        // NOOP
    }


    /**
     * Start {@link Valve}s) in this pipeline and implement the requirements of {@link LifecycleBase#startInternal()}.
     * 在这个管道中启动{@link Valve})，并实现{@link LifecycleBase#startInternal()}的要求。
     *
     * @exception LifecycleException if this component detects a fatal error that prevents this component from being
     *                                   used
     */
    @Override
    protected synchronized void startInternal() throws LifecycleException {

        // Start the Valves in our pipeline (including the basic), if any
        /**
         * 启动管道中的阀门(包括基本的)，如果有的话
         */
        Valve current = first;
        if (current == null) {
            current = basic;
        }
        while (current != null) {
            if (current instanceof Lifecycle) {
                ((Lifecycle) current).start();
            }
            current = current.getNext();
        }

        setState(LifecycleState.STARTING);
    }


    /**
     * Stop {@link Valve}s) in this pipeline and implement the requirements of {@link LifecycleBase#stopInternal()}.
     * 停止该管道中的{@link Valve})，并实现{@link LifecycleBase#stopInternal()}的要求。
     *
     * @exception LifecycleException if this component detects a fatal error that prevents this component from being
     *                                   used
     */
    @Override
    protected synchronized void stopInternal() throws LifecycleException {

        setState(LifecycleState.STOPPING);

        // Stop the Valves in our pipeline (including the basic), if any
        Valve current = first;
        if (current == null) {
            current = basic;
        }
        while (current != null) {
            if (current instanceof Lifecycle) {
                ((Lifecycle) current).stop();
            }
            current = current.getNext();
        }
    }


    @Override
    protected void destroyInternal() {
        Valve[] valves = getValves();
        for (Valve valve : valves) {
            removeValve(valve);
        }
    }


    /**
     * Return a String representation of this component.
     * 返回该组件的字符串表示形式。
     */
    @Override
    public String toString() {
        return ToStringUtil.toString(this);
    }


    // ------------------------------------------------------- Pipeline Methods


    /**
     * <p>
     * Return the Valve instance that has been distinguished as the basic Valve for this Pipeline (if any).
     * 返回已被区分为此管道的基本阀门的阀门实例(如果有的话)。
     */
    @Override
    public Valve getBasic() {
        return this.basic;
    }


    /**
     * <p>
     * Set the Valve instance that has been distinguished as the basic Valve for this Pipeline (if any). Prior to
     * setting the basic Valve, the Valve's <code>setContainer()</code> will be called, if it implements
     * 设置已被区分为此管道的基本阀门的阀门实例(如果有的话)。在设置基本的Valve之前，如果实现的话，Valve的<code>setContainer()<code>将被调用
     *
     * <code>Contained</code>, with the owning Container as an argument. The method may throw an
     * <code>包含<code>，其所属容器作为参数。该方法可能抛出错误
     *
     * <code>IllegalArgumentException</code> if this Valve chooses not to be associated with this Container, or
     * <code>IllegalStateException</code> if it is already associated with a different Container.
     * <code>IllegalArgumentException<code>如果这个阀门选择不与这个容器相关联，
     * 或者<code>IllegalStateException<code>如果它已经与不同的容器相关联。
     *
     * </p>
     *
     * @param valve Valve to be distinguished as the basic Valve
     */
    @Override
    public void setBasic(Valve valve) {

        // Change components if necessary
        /**
         * 必要时更改组件
         */
        Valve oldBasic = this.basic;
        if (oldBasic == valve) {
            return;
        }

        // Stop the old component if necessary
        /**
         * 必要时停止旧组件
         * 如果老的basic阀门实现了{@link Lifecycle}接口，调用{@link Lifecycle#stop()} 停止阀门组件
         * 如果老的basic阀门实现了{@link Contained}接口，将关联的容器设置为null
         */
        if (oldBasic != null) {
            if (getState().isAvailable() && (oldBasic instanceof Lifecycle)) {
                try {
                    ((Lifecycle) oldBasic).stop();
                } catch (LifecycleException e) {
                    log.error(sm.getString("standardPipeline.basic.stop"), e);
                }
            }
            if (oldBasic instanceof Contained) {
                try {
                    ((Contained) oldBasic).setContainer(null);
                } catch (Throwable t) {
                    ExceptionUtils.handleThrowable(t);
                }
            }
        }

        // Start the new component if necessary
        /**
         * 如有必要，启动新组件
         * 如果新的basic阀门实现了{@link Contained}接口，设置关联容器
         * 如果新的basic阀门实现了{@link Lifecycle}接口，调用{@link Lifecycle#start()} 方法，启动阀门组件
         */
        if (valve == null) {
            return;
        }
        if (valve instanceof Contained) {
            ((Contained) valve).setContainer(this.container);
        }
        if (getState().isAvailable() && valve instanceof Lifecycle) {
            try {
                ((Lifecycle) valve).start();
            } catch (LifecycleException e) {
                log.error(sm.getString("standardPipeline.basic.start"), e);
                return;
            }
        }

        // Update the pipeline
        /**
         * 更新管道
         * 用新的basic替换旧的basic组件，旧的basic阀门将被删除
         */
        Valve current = first;
        while (current != null) {
            if (current.getNext() == oldBasic) {
                current.setNext(valve);
                break;
            }
            current = current.getNext();
        }

        this.basic = valve;

    }


    /**
     * <p>
     * Add a new Valve to the end of the pipeline associated with this Container. Prior to adding the Valve, the Valve's
     * <code>setContainer()</code> method will be called, if it implements <code>Contained</code>, with the owning
     * Container as an argument. The method may throw an <code>IllegalArgumentException</code> if this Valve chooses not
     * to be associated with this Container, or <code>IllegalStateException</code> if it is already associated with a
     * different Container.
     * </p>
     * <p>在与此容器关联的管道末端添加一个新阀门。在添加阀门之前，阀门的<code>setContainer()<code>方法将被调用，
     * 如果它实现了<code>Contained<code>，拥有的容器作为参数。
     * 如果此阀门选择不与此容器关联，该方法可能会抛出<code>IllegalArgumentException<code>，或者<code>IllegalStateException<code>如果它已经与不同的容器关联。
     * </p>
     *
     *
     * @param valve Valve to be added
     *
     * @exception IllegalArgumentException if this Container refused to accept the specified Valve
     * @exception IllegalArgumentException if the specified Valve refuses to be associated with this Container
     * @exception IllegalStateException    if the specified Valve is already associated with a different Container
     */
    @Override
    public void addValve(Valve valve) {

        // Validate that we can add this Valve
        /**
         * 验证我们可以添加这个阀门
         * 给阀门设置关联容器
         */
        if (valve instanceof Contained) {
            ((Contained) valve).setContainer(this.container);
        }

        // Start the new component if necessary
        /**
         * 如有必要，启动新组件
         * 如果阀门实现了{@link Lifecycle}接口，调用{@link Lifecycle#start()} 启动阀门
         */
        if (getState().isAvailable()) {
            if (valve instanceof Lifecycle) {
                try {
                    ((Lifecycle) valve).start();
                } catch (LifecycleException e) {
                    log.error(sm.getString("standardPipeline.valve.start"), e);
                }
            }
        }

        // Add this Valve to the set associated with this Pipeline
        /**
         * 将此阀门添加到与此管道相关联的集合中
         * 将指定阀门组件添加到管道的末尾，但是在basic阀门之前
         */
        if (first == null) {
            first = valve;
            valve.setNext(basic);
        } else {
            Valve current = first;
            while (current != null) {
                if (current.getNext() == basic) {
                    current.setNext(valve);
                    valve.setNext(basic);
                    break;
                }
                current = current.getNext();
            }
        }

        /**
         * 触发{@link Container#ADD_VALVE_EVENT} 添加阀门事件
         */
        container.fireContainerEvent(Container.ADD_VALVE_EVENT, valve);
    }


    /**
     * Return the set of Valves in the pipeline associated with this Container, including the basic Valve (if any). If
     * there are no such Valves, a zero-length array is returned.
     * 返回与此容器关联的管道中的一组阀门，包括基本阀门(如果有的话)。如果没有这样的valve，则返回一个长度为零的数组。
     */
    @Override
    public Valve[] getValves() {

        List<Valve> valveList = new ArrayList<>();
        Valve current = first;
        if (current == null) {
            current = basic;
        }
        while (current != null) {
            valveList.add(current);
            current = current.getNext();
        }

        return valveList.toArray(new Valve[0]);

    }

    public ObjectName[] getValveObjectNames() {

        List<ObjectName> valveList = new ArrayList<>();
        Valve current = first;
        if (current == null) {
            current = basic;
        }
        while (current != null) {
            if (current instanceof JmxEnabled) {
                valveList.add(((JmxEnabled) current).getObjectName());
            }
            current = current.getNext();
        }

        return valveList.toArray(new ObjectName[0]);

    }

    /**
     * Remove the specified Valve from the pipeline associated with this Container, if it is found; otherwise, do
     * nothing. If the Valve is found and removed, the Valve's <code>setContainer(null)</code> method will be called if
     * it implements <code>Contained</code>.
     * 从与此容器关联的管道中移除指定的阀门(如果找到的话);否则，什么都不做。
     * 如果找到并删除了Valve，如果Valve的<code>setContainer(null)<code>方法实现了<code>Contained<code>，那么它将被调用。
     *
     *
     * @param valve Valve to be removed
     */
    @Override
    public void removeValve(Valve valve) {

        Valve current;
        if (first == valve) {
            first = first.getNext();
            current = null;
        } else {
            current = first;
        }
        while (current != null) {
            if (current.getNext() == valve) {
                current.setNext(valve.getNext());
                break;
            }
            current = current.getNext();
        }

        if (first == basic) {
            first = null;
        }

        if (valve instanceof Contained) {
            ((Contained) valve).setContainer(null);
        }

        if (valve instanceof Lifecycle) {
            // Stop this valve if necessary
            if (getState().isAvailable()) {
                try {
                    ((Lifecycle) valve).stop();
                } catch (LifecycleException e) {
                    log.error(sm.getString("standardPipeline.valve.stop"), e);
                }
            }
            try {
                ((Lifecycle) valve).destroy();
            } catch (LifecycleException e) {
                log.error(sm.getString("standardPipeline.valve.destroy"), e);
            }
        }

        container.fireContainerEvent(Container.REMOVE_VALVE_EVENT, valve);
    }


    @Override
    public Valve getFirst() {
        if (first != null) {
            return first;
        }

        return basic;
    }
}
