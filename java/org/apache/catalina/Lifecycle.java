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
 * 组件生命周期定义接口
 *
 * Common interface for component life cycle methods.  Catalina components
 * may implement this interface (as well as the appropriate interface(s) for
 * the functionality they support) in order to provide a consistent mechanism
 * to start and stop the component.
 * 组件生命周期方法的通用接口。Catalina组件可以实现这个接口(以及它们支持的功能的适当接口)，以便提供一致的机制来启动和停止组件。
 *
 * <br>
 * The valid state transitions for components that support {@link Lifecycle}
 * 支持{@link Lifecycle}的组件的有效状态转换
 *
 * are:
 * <pre>
 *            start()
 *  -----------------------------
 *  |                           |
 *  | init()                    |
 * NEW -»-- INITIALIZING        |
 * | |           |              |     ------------------«-----------------------
 * | |           |auto          |     |                                        |
 * | |          \|/    start() \|/   \|/     auto          auto         stop() |
 * | |      INITIALIZED --»-- STARTING_PREP --»- STARTING --»- STARTED --»---  |
 * | |         |                                                            |  |
 * | |destroy()|                                                            |  |
 * | --»-----«--    ------------------------«--------------------------------  ^
 * |     |          |                                                          |
 * |     |         \|/          auto                 auto              start() |
 * |     |     STOPPING_PREP ----»---- STOPPING ------»----- STOPPED -----»-----
 * |    \|/                               ^                     |  ^
 * |     |               stop()           |                     |  |
 * |     |       --------------------------                     |  |
 * |     |       |                                              |  |
 * |     |       |    destroy()                       destroy() |  |
 * |     |    FAILED ----»------ DESTROYING ---«-----------------  |
 * |     |                        ^     |                          |
 * |     |     destroy()          |     |auto                      |
 * |     --------»-----------------    \|/                         |
 * |                                 DESTROYED                     |
 * |                                                               |
 * |                            stop()                             |
 * ----»-----------------------------»------------------------------
 *
 * Any state can transition to FAILED.
 * 任何状态都可以转换为FAILED.
 *
 * Calling start() while a component is in states STARTING_PREP, STARTING or
 * STARTED has no effect.
 * 当组件处于STARTING_PREP、STARTING或STARTED状态时调用start()没有效果。
 *
 * Calling start() while a component is in state NEW will cause init() to be
 * called immediately after the start() method is entered.
 * 当组件处于NEW状态时调用start()将导致init()在start()方法输入后立即被调用。
 *
 * Calling stop() while a component is in states STOPPING_PREP, STOPPING or
 * STOPPED has no effect.
 * 当组件处于STOPPING_PREP、STOPPED或STOPPED状态时调用stop()不起作用。
 *
 * Calling stop() while a component is in state NEW transitions the component
 * to STOPPED. This is typically encountered when a component fails to start and
 * does not start all its sub-components. When the component is stopped, it will
 * try to stop all sub-components - even those it didn't start.
 * 当组件处于NEW状态时调用stop()将组件转换为STOPPED。这通常发生在组件启动失败且未启动其所有子组件时。
 * 当组件停止时，它将尝试停止所有子组件——即使是那些它没有启动的子组件。
 *
 * Attempting any other transition will throw {@link LifecycleException}.
 * 尝试任何其他转换都会抛出{@link LifecycleException}。
 *
 * </pre>
 * The {@link LifecycleEvent}s fired during state changes are defined in the
 * methods that trigger the changed. No {@link LifecycleEvent}s are fired if the
 * attempted transition is not valid.
 * 在状态更改期间触发的{@link LifecycleEvent}定义在触发更改的方法中。如果尝试的转换无效，则不会触发{@link LifecycleEvent}。
 *
 * @author Craig R. McClanahan
 */
public interface Lifecycle {


    // ----------------------------------------------------- Manifest Constants


    /**
     * The LifecycleEvent type for the "component before init" event.
     * “初始化之前的组件”事件的LifecycleEvent类型。
     */
    String BEFORE_INIT_EVENT = "before_init";


    /**
     * The LifecycleEvent type for the "component after init" event.
     * “初始化后的组件”事件的LifecycleEvent类型。
     */
    String AFTER_INIT_EVENT = "after_init";


    /**
     * The LifecycleEvent type for the "component start" event.
     * “组件启动”事件的LifecycleEvent类型。
     */
    String START_EVENT = "start";


    /**
     * The LifecycleEvent type for the "component before start" event.
     * “启动前组件”事件的LifecycleEvent类型。
     */
    String BEFORE_START_EVENT = "before_start";


    /**
     * The LifecycleEvent type for the "component after start" event.
     * “组件启动后”事件的LifecycleEvent类型。
     */
    String AFTER_START_EVENT = "after_start";


    /**
     * The LifecycleEvent type for the "component stop" event.
     * “组件停止”事件的LifecycleEvent类型。
     */
    String STOP_EVENT = "stop";


    /**
     * The LifecycleEvent type for the "component before stop" event.
     * “停止前组件”事件的LifecycleEvent类型。
     */
    String BEFORE_STOP_EVENT = "before_stop";


    /**
     * The LifecycleEvent type for the "component after stop" event.
     * “停止后的组件”事件的LifecycleEvent类型。
     */
    String AFTER_STOP_EVENT = "after_stop";


    /**
     * The LifecycleEvent type for the "component after destroy" event.
     * “销毁后的组件”事件的LifecycleEvent类型。
     */
    String AFTER_DESTROY_EVENT = "after_destroy";


    /**
     * The LifecycleEvent type for the "component before destroy" event.
     * “销毁之前的组件”事件的LifecycleEvent类型。
     */
    String BEFORE_DESTROY_EVENT = "before_destroy";


    /**
     * The LifecycleEvent type for the "periodic" event.
     * “周期性”事件的LifecycleEvent类型。
     */
    String PERIODIC_EVENT = "periodic";


    /**
     * The LifecycleEvent type for the "configure_start" event. Used by those
     * components that use a separate component to perform configuration and
     * need to signal when configuration should be performed - usually after
     * {@link #BEFORE_START_EVENT} and before {@link #START_EVENT}.
     * “configure_start”事件的LifecycleEvent类型。用于那些使用单独组件来执行配置的组件，
     * 并且需要在应该执行配置时发出信号——通常在{@link #BEFORE_START_EVENT}之后和{@link #START_EVENT}之前。
     *
     */
    String CONFIGURE_START_EVENT = "configure_start";


    /**
     * The LifecycleEvent type for the "configure_stop" event. Used by those
     * components that use a separate component to perform configuration and
     * need to signal when de-configuration should be performed - usually after
     * {@link #STOP_EVENT} and before {@link #AFTER_STOP_EVENT}.

     * “configure_stop”事件的LifecycleEvent类型。用于那些使用单独组件来执行配置的组件，
     * 并且需要在何时执行反配置时发出信号——通常在{@link #STOP_EVENT}之后和{@link #AFTER_STOP_EVENT}之前。
     */
    String CONFIGURE_STOP_EVENT = "configure_stop";


    // --------------------------------------------------------- Public Methods


    /**
     * Add a LifecycleEvent listener to this component.
     * 向该组件添加LifecycleEvent侦听器。
     *
     * @param listener The listener to add
     */
    void addLifecycleListener(LifecycleListener listener);


    /**
     * Get the life cycle listeners associated with this life cycle.
     * 获取与此生命周期关联的生命周期侦听器。
     *
     * @return An array containing the life cycle listeners associated with this
     *         life cycle. If this component has no listeners registered, a
     *         zero-length array is returned.
     *         包含与此生命周期关联的生命周期侦听器的数组。如果该组件没有注册监听器，则返回一个零长度数组。
     */
    LifecycleListener[] findLifecycleListeners();


    /**
     * Remove a LifecycleEvent listener from this component.
     * 从该组件中移除LifecycleEvent侦听器。
     *
     * @param listener The listener to remove
     */
    void removeLifecycleListener(LifecycleListener listener);


    /**
     * Prepare the component for starting. This method should perform any
     * initialization required post object creation. The following
     * {@link LifecycleEvent}s will be fired in the following order:
     * 准备启动组件。此方法应在创建对象后执行所需的任何初始化。下面的{@link LifecycleEvent}将按照以下顺序被触发:
     *
     * <ol>
     *   <li>INIT_EVENT: On the successful completion of component
     *                   initialization.</li>
     *                   成功完成组件初始化
     * </ol>
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     *  如果该组件检测到致命错误，阻止该组件被使用
     */
    void init() throws LifecycleException;

    /**
     * Prepare for the beginning of active use of the public methods other than
     * property getters/setters and life cycle methods of this component. This
     * method should be called before any of the public methods other than
     * property getters/setters and life cycle methods of this component are
     * utilized. The following {@link LifecycleEvent}s will be fired in the
     * following order:
     * 为开始主动使用该组件的属性getter和生命周期方法以外的公共方法做好准备。
     * 该方法应该在使用该组件的属性getter和生命周期方法以外的任何公共方法之前调用。
     * 下面的{@link LifecycleEvent}将按照以下顺序被触发:
     *
     * <ol>
     *   <li>BEFORE_START_EVENT: At the beginning of the method. It is as this
     *                           point the state transitions to
     *                           {@link LifecycleState#STARTING_PREP}.</li>
     *                           在方法的开始。正是在这一点上，状态转换到{@link LifecycleState#STARTING_PREP}。
     *   <li>START_EVENT: During the method once it is safe to call start() for
     *                    any child components. It is at this point that the
     *                    state transitions to {@link LifecycleState#STARTING}
     *                    and that the public methods other than property
     *                    getters/setters and life cycle methods may be
     *                    used.</li>
     *                    在该方法中，一旦安全，就可以为任何子组件调用start()。
     *                    正是在这一点上，状态转换到{@link LifecycleState#STARTING}，并且可以使用属性getters/setters和生命周期方法以外的公共方法。
     *
     *   <li>AFTER_START_EVENT: At the end of the method, immediately before it
     *                          returns. It is at this point that the state
     *                          transitions to {@link LifecycleState#STARTED}.
     *                          在方法的末尾，在它返回之前。正是在这一点上，状态转换到{@link LifecycleState#STARTED}。
     *                          </li>
     * </ol>
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     */
    void start() throws LifecycleException;


    /**
     * Gracefully terminate the active use of the public methods other than
     * property getters/setters and life cycle methods of this component. Once
     * the STOP_EVENT is fired, the public methods other than property
     * getters/setters and life cycle methods should not be used. The following
     * {@link LifecycleEvent}s will be fired in the following order:
     * 优雅地终止该组件除属性getter和生命周期方法之外的公共方法的活动使用。
     * 一旦触发STOP_EVENT，除了属性getter和生命周期方法之外的公共方法就不应该被使用。下面的{@link LifecycleEvent}将按照以下顺序被触发:
     *
     * <ol>
     *   <li>BEFORE_STOP_EVENT: At the beginning of the method. It is at this
     *                          point that the state transitions to
     *                          {@link LifecycleState#STOPPING_PREP}.</li>
     *                          在方法的开始。正是在这一点上，状态转换到{@link LifecycleState#STOPPING_PREP}。
     *   <li>STOP_EVENT: During the method once it is safe to call stop() for
     *                   any child components. It is at this point that the
     *                   state transitions to {@link LifecycleState#STOPPING}
     *                   and that the public methods other than property
     *                   getters/setters and life cycle methods may no longer be
     *                   used.</li>
     *                   在该方法中，一旦安全，就可以为任何子组件调用stop()。
     *                   在这一点上，状态转换到{@link LifecycleState#STOPPING}，除了属性getters/setters和生命周期方法之外的公共方法可能不再使用。
     *
     *   <li>AFTER_STOP_EVENT: At the end of the method, immediately before it
     *                         returns. It is at this point that the state
     *                         transitions to {@link LifecycleState#STOPPED}.
     *                         在方法的末尾，在它返回之前。正是在这一点上，状态转换到{@link LifecycleState#STOPPED}。
     *                         </li>
     * </ol>
     *
     * Note that if transitioning from {@link LifecycleState#FAILED} then the
     * three events above will be fired but the component will transition
     * directly from {@link LifecycleState#FAILED} to
     * {@link LifecycleState#STOPPING}, bypassing
     * {@link LifecycleState#STOPPING_PREP}
     * 注意，如果从{@link LifecycleState#FAILED}转换，那么上面的三个事件将被触发，但是组件将直接从{@link LifecycleState#STOPPING}转换，
     * 绕过{@link LifecycleState#STOPPING_PREP}
     *
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that needs to be reported
     */
    void stop() throws LifecycleException;

    /**
     * Prepare to discard the object. The following {@link LifecycleEvent}s will
     * be fired in the following order:
     * 准备丢弃该对象。下面的{@link LifecycleEvent}将按照以下顺序被触发:
     *
     * <ol>
     *   <li>DESTROY_EVENT: On the successful completion of component
     *                      destruction.</li>
     *                      成功完成组件销毁。
     * </ol>
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     */
    void destroy() throws LifecycleException;


    /**
     * Obtain the current state of the source component.
     * 获取源组件的当前状态。
     *
     * @return The current state of the source component.
     */
    LifecycleState getState();


    /**
     * Obtain a textual representation of the current component state. Useful
     * for JMX. The format of this string may vary between point releases and
     * should not be relied upon to determine component state. To determine
     * component state, use {@link #getState()}.
     * 获取当前组件状态的文本表示。对JMX很有用。此字符串的格式在不同的点发布之间可能有所不同，不应依赖于此格式来确定组件状态。
     * 要确定组件的状态，使用{@link #getState()}。
     *
     * @return The name of the current component state.
     */
    String getStateName();


    /**
     * Marker interface used to indicate that the instance should only be used
     * once. Calling {@link #stop()} on an instance that supports this interface
     * will automatically call {@link #destroy()} after {@link #stop()}
     * completes.
     *
     * 标记接口，用于指示该实例只应使用一次。在支持此接口的实例上调用{@link #stop()}将在{@link #stop()}完成后自动调用{@link #destroy()}。
     */
    interface SingleUse {
    }
}
