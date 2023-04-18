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
package org.apache.catalina.util;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleState;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.res.StringManager;

/**
 * 组件生命周期的基本实现类，实现了{@link #init()},{@link #start()},{@link #stop()}和{@link #destroy()}生命周期函数的基本状态转换，
 * 并为各个生命周期方法定义了模板方法，由子类实现具体的模板方法
 *
 * Base implementation of the {@link Lifecycle} interface that implements the
 * state transition rules for {@link Lifecycle#start()} and
 * {@link Lifecycle#stop()}
 *
 * {@link Lifecycle}接口的基本实现，该接口实现了{@link Lifecycle#start()}和{@link Lifecycle#stop()}的状态转换规则
 */
public abstract class LifecycleBase implements Lifecycle {

    private static final Log log = LogFactory.getLog(LifecycleBase.class);

    private static final StringManager sm = StringManager.getManager(LifecycleBase.class);


    /**
     * The list of registered LifecycleListeners for event notifications.
     * 为事件通知注册的LifecycleListener列表。
     */
    private final List<LifecycleListener> lifecycleListeners = new CopyOnWriteArrayList<>();


    /**
     * The current state of the source component.
     * 源组件的当前状态。
     */
    private volatile LifecycleState state = LifecycleState.NEW;


    /**
     * 标记组件在生命周期函数中产生的{@link LifecycleException}是否抛出还是记录
     */
    private boolean throwOnFailure = true;

    /**
     * Will a {@link LifecycleException} thrown by a sub-class during
     * {@link #initInternal()}, {@link #startInternal()},
     * {@link #stopInternal()} or {@link #destroyInternal()} be re-thrown for
     * the caller to handle or will it be logged instead?
     *
     * 由子类在{@link #initInternal()}、{@link #startInternal()}、{@link #stopInternal()}或{@link #destroyInternal()}
     * 期间抛出的{@link LifecycleException}是否会被重新抛出以供调用者处理，还是会被记录下来?
     *
     * @return {@code true} if the exception will be re-thrown, otherwise
     *         {@code false}
     */
    public boolean getThrowOnFailure() {
        return throwOnFailure;
    }


    /**
     * Configure if a {@link LifecycleException} thrown by a sub-class during
     * {@link #initInternal()}, {@link #startInternal()},
     * {@link #stopInternal()} or {@link #destroyInternal()} will be re-thrown
     * for the caller to handle or if it will be logged instead.
     *
     * 如果在{@link #initInternal()}、{@link #startInternal()}、{@link #stopInternal()}或{@link #destroyInternal()}
     * 期间由子类抛出的{@link LifecycleException}将被重新抛出以供调用者处理，或者它将被记录下来，请配置。
     *
     * @param throwOnFailure {@code true} if the exception should be re-thrown,
     *                       otherwise {@code false}
     */
    public void setThrowOnFailure(boolean throwOnFailure) {
        this.throwOnFailure = throwOnFailure;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void addLifecycleListener(LifecycleListener listener) {
        lifecycleListeners.add(listener);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public LifecycleListener[] findLifecycleListeners() {
        return lifecycleListeners.toArray(new LifecycleListener[0]);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void removeLifecycleListener(LifecycleListener listener) {
        lifecycleListeners.remove(listener);
    }


    /**
     * Allow sub classes to fire {@link Lifecycle} events.
     * 允许子类触发{@link Lifecycle}事件。
     *
     * @param type  Event type
     * @param data  Data associated with event.
     */
    protected void fireLifecycleEvent(String type, Object data) {
        LifecycleEvent event = new LifecycleEvent(this, type, data);
        for (LifecycleListener listener : lifecycleListeners) {
            listener.lifecycleEvent(event);
        }
    }


    /**
     * 默认实现的组件初始化方法，实现了组件的状态转换，该实现中调用了{@link #initInternal()}模板方法
     * 组件不能重复初始化，如果当前组件的状态已经不是{@link LifecycleState#NEW}新创建状态，则抛出异常
     *
     * 状态转换:
     * {@link LifecycleState#NEW} -> {@link LifecycleState#INITIALIZING} -> {@link LifecycleState#INITIALIZED}
     *                                                   |
     *                                                 \|/
     *                                {@link LifecycleState#FAILED}
     *
     * @throws LifecycleException
     */
    @Override
    public final synchronized void init() throws LifecycleException {
        /**
         * 如果当前组件的状态不是{@link LifecycleState#NEW}，也就是说该组件已经初始化过了，
         * 抛出异常，组件不能重复初始化
         */
        if (!state.equals(LifecycleState.NEW)) {
            invalidTransition(Lifecycle.BEFORE_INIT_EVENT);
        }

        try {
            /**
             * 转换当前组件的状态为{@link LifecycleState#INITIALIZING},并触发组件初始化前的{@link BEFORE_INIT_EVENT}事件
             */
            setStateInternal(LifecycleState.INITIALIZING, null, false);
            /**
             * 调用模板方法，由子类实现具体初始化流程
             */
            initInternal();
            /**
             * 组件初始化完成后，将组件状态转换为{@link LifecycleState#INITIALIZED}初始化完成状态，
             * 并触发组件初始化完成后的{@link AFTER_INIT_EVENT}事件
             */
            setStateInternal(LifecycleState.INITIALIZED, null, false);
        } catch (Throwable t) {
            handleSubClassException(t, "lifecycleBase.initFail", toString());
        }
    }


    /**
     * Sub-classes implement this method to perform any instance initialisation
     * required.
     * 子类实现此方法来执行所需的任何实例初始化。
     *
     * @throws LifecycleException If the initialisation fails
     */
    protected abstract void initInternal() throws LifecycleException;


    /**
     * 默认实现的组件启动方法，实现了组件的状态转换，该实现中调用了{@link #startInternal()}模板方法
     * 组件不能重复启动，如果组件正在启动或者已经启动，直接退出该方法
     *
     * 状态转换:
     *                  {@link LifecycleState#INITIALIZED} ---------------
     *                                       /|\                         |
     *                                       |                          \|/
     * {@link LifecycleState#NEW} -> {@link #init()}    {@link LifecycleState#STARTING_PREP} -> {@link LifecycleState#STARTING}
     *                                                                  |                                    |
     *                                                                 \|/                                  \|/
     *                                                  {@link LifecycleState#FAILED}           {@link LifecycleState#STARTED}
     *
     * {@inheritDoc}
     */
    @Override
    public final synchronized void start() throws LifecycleException {

        /**
         * 如果组件正在启动或者已经启动完成，不需要重复启动，直接退出
         */
        if (LifecycleState.STARTING_PREP.equals(state) || LifecycleState.STARTING.equals(state) ||
                LifecycleState.STARTED.equals(state)) {

            if (log.isDebugEnabled()) {
                Exception e = new LifecycleException();
                log.debug(sm.getString("lifecycleBase.alreadyStarted", toString()), e);
            } else if (log.isInfoEnabled()) {
                log.info(sm.getString("lifecycleBase.alreadyStarted", toString()));
            }

            return;
        }

        /**
         * 如果组件还没有初始化，先初始化
         */
        if (state.equals(LifecycleState.NEW)) {
            init();
        }
        /**
         * 如果组件的状态已经是{@link LifecycleState#FAILED}调用{@link #stop()} 方法停止组件
         */
        else if (state.equals(LifecycleState.FAILED)) {
            stop();
        }
        /**
         * 如果组件已经销毁，抛出异常
         */
        else if (!state.equals(LifecycleState.INITIALIZED) &&
                !state.equals(LifecycleState.STOPPED)) {
            invalidTransition(Lifecycle.BEFORE_START_EVENT);
        }

        try {
            /**
             * 转换组件的状态为{@link LifecycleState#STARTING_PREP}启动前的状态，并触发组件启动前的{@link BEFORE_START_EVENT}事件
             */
            setStateInternal(LifecycleState.STARTING_PREP, null, false);
            /**
             * 调用模板方法，由子类实现具体启动流程
             * 子类实现需要确保在{@link #startInternal()} 方法执行期间将组件状态修改为{@link LifecycleState#STARTING}
             * 并触发组件启动的{@link START_EVENT}事件
             */
            startInternal();
            /**
             * 如果组件状态为{@link LifecycleState#FAILED}调用{@link #stop()} 停止组件
             */
            if (state.equals(LifecycleState.FAILED)) {
                // This is a 'controlled' failure. The component put itself into the
                // FAILED state so call stop() to complete the clean-up.
                /**
                 * 这是一种“可控的”失败。该组件将自己置于FAILED状态，因此调用stop()来完成清理。
                 */
                stop();
            }
            /**
             * 如果子类实现的{@link #startInternal()}模板方法没有将组件的状态修改为{@link LifecycleState#STARTING}，则抛出异常
             */
            else if (!state.equals(LifecycleState.STARTING)) {
                // Shouldn't be necessary but acts as a check that sub-classes are
                // doing what they are supposed to.
                /**
                 * 不应该是必要的，但可以作为检查子类是否正在做它们应该做的事情。
                 */
                invalidTransition(Lifecycle.AFTER_START_EVENT);
            }
            /**
             * 如果子类正确的启动组件，并将组件状态设置为{@link LifecycleState#STARTING}，修改组件状态为{@link LifecycleState#STARTED}
             * 并触发组件启动后的{@link AFTER_START_EVENT}事件
             */
            else {
                setStateInternal(LifecycleState.STARTED, null, false);
            }
        } catch (Throwable t) {
            // This is an 'uncontrolled' failure so put the component into the
            // FAILED state and throw an exception.
            /**
             * 这是一个“不受控制”的失败，因此将组件置于FAILED状态并抛出异常。
             * 将组件的状态修改为{@link LifecycleState#FAILED}状态，并抛出异常
             */
            handleSubClassException(t, "lifecycleBase.startFail", toString());
        }
    }


    /**
     * Sub-classes must ensure that the state is changed to
     * {@link LifecycleState#STARTING} during the execution of this method.
     * Changing state will trigger the {@link Lifecycle#START_EVENT} event.
     *
     * 子类必须确保在该方法执行期间状态被更改为{@link LifecycleState#STARTING}。
     * 改变状态将触发{@link Lifecycle#START_EVENT}事件。
     *
     * If a component fails to start it may either throw a
     * {@link LifecycleException} which will cause it's parent to fail to start
     * or it can place itself in the error state in which case {@link #stop()}
     * will be called on the failed component but the parent component will
     * continue to start normally.
     * 如果一个组件启动失败，它可能会抛出一个{@link LifecycleException}，这将导致它的父组件启动失败，
     * 或者它可以将自己置于错误状态，在这种情况下，将在失败的组件上调用{@link #stop()}，但父组件将继续正常启动。
     *
     *
     * @throws LifecycleException Start error occurred
     */
    protected abstract void startInternal() throws LifecycleException;


    /**
     * 默认实现的组件停止方法，实现了组件的状态转换，该实现中调用了{@link #stopInternal()} 模板方法
     * 组件不需要重复停止，如果组件正在停止或者已经停止，直接退出stop方法
     *
     * 状态转换:
     *
     * {@link LifecycleState#STARTED} -> {@link LifecycleState#STOPPING_PREP} -> {@link LifecycleState#STOPPING}
     *                                                                              /|\              |
     *                                                                              |               \|/
     * {@link LifecycleState#FAILED} -----------------------------------------------      {@link LifecycleState#STOPPED}
     *                                                                                               |
     *                                                                                              \|/
     * {@link LifecycleState#NEW} -> {@link LifecycleState#STOPPED} <--------------------------------
     *
     *
     * {@inheritDoc}
     */
    @Override
    public final synchronized void stop() throws LifecycleException {

        /**
         * 如果组件正在停止或者已经停止，直接退出，组件不需要重复停止
         */
        if (LifecycleState.STOPPING_PREP.equals(state) || LifecycleState.STOPPING.equals(state) ||
                LifecycleState.STOPPED.equals(state)) {

            if (log.isDebugEnabled()) {
                Exception e = new LifecycleException();
                log.debug(sm.getString("lifecycleBase.alreadyStopped", toString()), e);
            } else if (log.isInfoEnabled()) {
                log.info(sm.getString("lifecycleBase.alreadyStopped", toString()));
            }

            return;
        }

        /**
         * 如果组件是新创建的，直接修改状态即可
         */
        if (state.equals(LifecycleState.NEW)) {
            state = LifecycleState.STOPPED;
            return;
        }

        /**
         * 如果组件处于初始化状态或者正在启动，抛出异常
         */
        if (!state.equals(LifecycleState.STARTED) && !state.equals(LifecycleState.FAILED)) {
            invalidTransition(Lifecycle.BEFORE_STOP_EVENT);
        }

        try {
            if (state.equals(LifecycleState.FAILED)) {
                // Don't transition to STOPPING_PREP as that would briefly mark the
                // component as available but do ensure the BEFORE_STOP_EVENT is
                // fired
                /**
                 * 不要转换到STOPPING_PREP，因为这会短暂地将组件标记为可用，但要确保触发BEFORE_STOP_EVENT
                 * 触发组件停止前的{@link BEFORE_STOP_EVENT}事件
                 */
                fireLifecycleEvent(BEFORE_STOP_EVENT, null);
            } else {
                /**
                 * 将组件的状态修改为{@link LifecycleState#STOPPING_PREP}状态，并触发组件停止前的{@link BEFORE_STOP_EVENT}事件
                 */
                setStateInternal(LifecycleState.STOPPING_PREP, null, false);
            }

            /**
             * 调用模板方法，由子类实现具体停止流程
             * 子类必须确保在该方法执行期间状态被更改为{@link LifecycleState#STOPPING}，并触发{@link Lifecycle#STOP_EVENT}事件。
             */
            stopInternal();

            // Shouldn't be necessary but acts as a check that sub-classes are
            // doing what they are supposed to.
            /**
             * 不应该是必要的，但可以作为检查子类是否正在做它们应该做的事情。
             * 如果子类实现的模板方法{@link #stopInternal()} 中没有把组件的状态修改为{@link LifecycleState#STOPPING}或者
             * 子类停止组件时出现错误，抛出异常
             */
            if (!state.equals(LifecycleState.STOPPING) && !state.equals(LifecycleState.FAILED)) {
                invalidTransition(Lifecycle.AFTER_STOP_EVENT);
            }

            /**
             * 设置组件的状态为{@link LifecycleState#STOPPED}，并触发组件停止后的{@link #AFTER_STOP_EVENT}时间
             */
            setStateInternal(LifecycleState.STOPPED, null, false);
        } catch (Throwable t) {
            handleSubClassException(t, "lifecycleBase.stopFail", toString());
        } finally {
            /**
             * 如果组件是一次性的，不可重用，调用组件的销毁方法{@link #destroy()}，并触发相应事件
             */
            if (this instanceof Lifecycle.SingleUse) {
                // Complete stop process first
                // 先完成停止过程
                setStateInternal(LifecycleState.STOPPED, null, false);
                destroy();
            }
        }
    }


    /**
     * Sub-classes must ensure that the state is changed to
     * {@link LifecycleState#STOPPING} during the execution of this method.
     * Changing state will trigger the {@link Lifecycle#STOP_EVENT} event.
     *
     * 子类必须确保在该方法执行期间状态被更改为{@link LifecycleState#STOPPING}。改变状态将触发{@link Lifecycle#STOP_EVENT}事件。
     *
     * @throws LifecycleException Stop error occurred
     */
    protected abstract void stopInternal() throws LifecycleException;


    /**
     * 默认实现的组件销毁方法，实现了组件的状态转换，该实现中调用了{@link #destroyInternal()} ()} 模板方法
     *
     * 状态转换:
     *
     * {@link LifecycleState#FAILED} -> {@link #stop()} -> {@link LifecycleState#DESTROYING} -> {@link LifecycleState#DESTROYED}
     *                    -----------<------------|                    /|\
     *                  \|/                                             |
     * {@link LifecycleState#FAILED} --------------->-------------------|
     * {@link LifecycleState#STOPPED} --------------->------------------|
     * {@link LifecycleState#NEW} ----------------->--------------------|
     * {@link LifecycleState#INITIALIZED} -------------->---------------|
     *
     * @throws LifecycleException
     */
    @Override
    public final synchronized void destroy() throws LifecycleException {
        /**
         * 如果组件状态是{@link LifecycleState#FAILED}，先执行停止方法，
         * 执行完停止方法后，组件的状态会转变成{@link LifecycleState#STOPPED}
         */
        if (LifecycleState.FAILED.equals(state)) {
            try {
                // Triggers clean-up
                // 触发清理
                stop();
            } catch (LifecycleException e) {
                // Just log. Still want to destroy.
                log.error(sm.getString("lifecycleBase.destroyStopFail", toString()), e);
            }
        }

        /**
         * 如果组件正在销毁或者已经销毁，直接退出
         */
        if (LifecycleState.DESTROYING.equals(state) || LifecycleState.DESTROYED.equals(state)) {
            if (log.isDebugEnabled()) {
                Exception e = new LifecycleException();
                log.debug(sm.getString("lifecycleBase.alreadyDestroyed", toString()), e);
            } else if (log.isInfoEnabled() && !(this instanceof Lifecycle.SingleUse)) {
                // Rather than have every component that might need to call
                // destroy() check for SingleUse, don't log an info message if
                // multiple calls are made to destroy()
                /**
                 * 与其让每个可能需要调用destroy()的组件都检查SingleUse，不如在多次调用destroy()时不记录信息消息。
                 */
                log.info(sm.getString("lifecycleBase.alreadyDestroyed", toString()));
            }

            return;
        }

        /**
         * 如果组件正在初始化，正在启动，正在停止或者已经启动，此时组件不能销毁
         */
        if (!state.equals(LifecycleState.STOPPED) && !state.equals(LifecycleState.FAILED) &&
                !state.equals(LifecycleState.NEW) && !state.equals(LifecycleState.INITIALIZED)) {
            invalidTransition(Lifecycle.BEFORE_DESTROY_EVENT);
        }

        try {
            /**
             * 设置组件状态为正在销毁 {@link LifecycleState#DESTROYING}，并触发销毁前的{@link BEFORE_DESTROY_EVENT}事件回调
             */
            setStateInternal(LifecycleState.DESTROYING, null, false);
            /**
             * 调用模板方法完成组件的销毁，由子类实现具体销毁流程
             */
            destroyInternal();
            /**
             * 设置组件状态为已经销毁 {@link LifecycleState#DESTROYED}，并触发销毁后的{@link AFTER_DESTROY_EVENT}事件回调
             */
            setStateInternal(LifecycleState.DESTROYED, null, false);
        } catch (Throwable t) {
            handleSubClassException(t, "lifecycleBase.destroyFail", toString());
        }
    }


    /**
     * Sub-classes implement this method to perform any instance destruction
     * required.
     * 子类实现此方法来执行所需的任何实例销毁。
     *
     * @throws LifecycleException If the destruction fails
     */
    protected abstract void destroyInternal() throws LifecycleException;


    /**
     * {@inheritDoc}
     */
    @Override
    public LifecycleState getState() {
        return state;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getStateName() {
        return getState().toString();
    }


    /**
     * Provides a mechanism for sub-classes to update the component state.
     * 为子类提供更新组件状态的机制。
     *
     * Calling this method will automatically fire any associated
     * {@link Lifecycle} event. It will also check that any attempted state
     * transition is valid for a sub-class.
     * 调用此方法将自动触发任何关联的{@link Lifecycle}事件。它还将检查任何尝试的状态转换是否对子类有效。
     *
     * @param state The new state for this component
     * @throws LifecycleException when attempting to set an invalid state
     */
    protected synchronized void setState(LifecycleState state) throws LifecycleException {
        setStateInternal(state, null, true);
    }


    /**
     * Provides a mechanism for sub-classes to update the component state.
     * Calling this method will automatically fire any associated
     * {@link Lifecycle} event. It will also check that any attempted state
     * transition is valid for a sub-class.
     * 为子类提供更新组件状态的机制。调用此方法将自动触发任何关联的{@link Lifecycle}事件。它还将检查任何尝试的状态转换是否对子类有效。
     *
     * @param state The new state for this component
     * @param data  The data to pass to the associated {@link Lifecycle} event
     * @throws LifecycleException when attempting to set an invalid state
     */
    protected synchronized void setState(LifecycleState state, Object data)
            throws LifecycleException {
        setStateInternal(state, data, true);
    }


    private synchronized void setStateInternal(LifecycleState state, Object data, boolean check)
            throws LifecycleException {

        if (log.isDebugEnabled()) {
            log.debug(sm.getString("lifecycleBase.setState", this, state));
        }

        if (check) {
            // Must have been triggered by one of the abstract methods (assume
            // code in this class is correct)
            // null is never a valid state
            /**
             * 必须由一个抽象方法触发(假设该类中的代码是正确的)null永远不是有效的状态
             * 检查要设置的状态是否为空，null不应是有效的状态
             */
            if (state == null) {
                invalidTransition("null");
                // Unreachable code - here to stop eclipse complaining about
                // a possible NPE further down the method
                /**
                 * 不可到达的代码-在这里停止eclipse抱怨可能的NPE进一步的方法
                 */
                return;
            }

            // Any method can transition to failed
            // startInternal() permits STARTING_PREP to STARTING
            // stopInternal() permits STOPPING_PREP to STOPPING and FAILED to
            // STOPPING
            /**
             * 任何方法都可以过渡到失败的startInternal()允许STARTING_PREP到开始stopInternal()允许STOPPING_PREP到停止和失败停止
             *
             * 检查状态转换是否有效，任何状态都可以转换到{@link LifecycleState#FAILED}状态
             */
            if (!(state == LifecycleState.FAILED ||
                    (this.state == LifecycleState.STARTING_PREP &&
                            state == LifecycleState.STARTING) ||
                    (this.state == LifecycleState.STOPPING_PREP &&
                            state == LifecycleState.STOPPING) ||
                    (this.state == LifecycleState.FAILED &&
                            state == LifecycleState.STOPPING))) {
                // No other transition permitted
                // 不允许其他转换
                invalidTransition(state.name());
            }
        }

        /**
         * 设置当前组件为指定状态，并触发指定状态对应的事件
         */
        this.state = state;
        String lifecycleEvent = state.getLifecycleEvent();
        if (lifecycleEvent != null) {
            fireLifecycleEvent(lifecycleEvent, data);
        }
    }


    private void invalidTransition(String type) throws LifecycleException {
        String msg = sm.getString("lifecycleBase.invalidTransition", type, toString(), state);
        throw new LifecycleException(msg);
    }


    private void handleSubClassException(Throwable t, String key, Object... args) throws LifecycleException {
        setStateInternal(LifecycleState.FAILED, null, false);
        ExceptionUtils.handleThrowable(t);
        String msg = sm.getString(key, args);
        if (getThrowOnFailure()) {
            if (!(t instanceof LifecycleException)) {
                t = new LifecycleException(msg, t);
            }
            throw (LifecycleException) t;
        } else {
            log.error(msg, t);
        }
    }
}
