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

/**
 * A <b>Loader</b> represents a Java ClassLoader implementation that can
 * be used by a Container to load class files (within a repository associated
 * with the Loader) that are designed to be reloaded upon request, as well as
 * a mechanism to detect whether changes have occurred in the underlying
 * repository.
 * A <b>Loader<b>表示一个Java ClassLoader实现，容器可以使用它来加载类文件(在与Loader关联的存储库中)，
 * 这些文件被设计为根据请求重新加载，以及一种检测底层存储库中是否发生了更改的机制。
 *
 * <p>
 * In order for a <code>Loader</code> implementation to successfully operate
 * with a <code>Context</code> implementation that implements reloading, it
 * must obey the following constraints:
 * 为了使<code>Loader<code>实现成功地与实现重载的<code>Context<code>实现操作，它必须遵守以下约束:
 *
 * <ul>
 * <li>Must implement <code>Lifecycle</code> so that the Context can indicate
 *     that a new class loader is required.
 *     必须实现<code>Lifecycle<code>，以便Context可以指示需要一个新的类装入器。
 *
 * <li>The <code>start()</code> method must unconditionally create a new
 *     <code>ClassLoader</code> implementation.
 *     <code>start()<code>方法必须无条件地创建一个新的<code>ClassLoader<code>实现。
 *
 * <li>The <code>stop()</code> method must throw away its reference to the
 *     <code>ClassLoader</code> previously utilized, so that the class loader,
 *     all classes loaded by it, and all objects of those classes, can be
 *     garbage collected.
 *     <code>stop()<code>方法必须丢弃它对先前使用的<code>ClassLoader<code>的引用，
 *     这样类装入器、它装入的所有类以及这些类的所有对象都可以被垃圾收集。
 *
 * <li>Must allow a call to <code>stop()</code> to be followed by a call to
 *     <code>start()</code> on the same <code>Loader</code> instance.
 *     必须允许调用<code>stop()<code>之后，在同一个<code>Loader<code>实例上调用<code>start()<code>。
 *
 * <li>Based on a policy chosen by the implementation, must call the
 *     <code>Context.reload()</code> method on the owning <code>Context</code>
 *     when a change to one or more of the class files loaded by this class
 *     loader is detected.
 *     根据实现选择的策略，当检测到该类装入器装入的一个或多个类文件发生更改时，
 *     必须在所属的<code>Context<code>上调用<code>Context.reload()<code>方法。
 *
 * </ul>
 *
 * @author Craig R. McClanahan
 */
public interface Loader {


    /**
     * Execute a periodic task, such as reloading, etc. This method will be
     * invoked inside the classloading context of this container. Unexpected
     * throwables will be caught and logged.
     * 执行周期性任务，如重新加载等。此方法将在此容器的类加载上下文中调用。意外的抛出将被捕获并记录。
     */
    void backgroundProcess();


    /**
     * @return the Java class loader to be used by this Container.
     */
    ClassLoader getClassLoader();


    /**
     * @return the Context with which this Loader has been associated.
     */
    Context getContext();


    /**
     * Set the Context with which this Loader has been associated.
     *
     * @param context The associated Context
     */
    void setContext(Context context);


    /**
     * @return the "follow standard delegation model" flag used to configure
     * our ClassLoader.
     */
    boolean getDelegate();


    /**
     * Set the "follow standard delegation model" flag used to configure
     * our ClassLoader.
     *
     * @param delegate The new flag
     */
    void setDelegate(boolean delegate);


    /**
     * Add a property change listener to this component.
     *
     * @param listener The listener to add
     */
    void addPropertyChangeListener(PropertyChangeListener listener);


    /**
     * Has the internal repository associated with this Loader been modified,
     * such that the loaded classes should be reloaded?
     *
     * @return <code>true</code> when the repository has been modified,
     *         <code>false</code> otherwise
     */
    boolean modified();


    /**
     * Remove a property change listener from this component.
     *
     * @param listener The listener to remove
     */
    void removePropertyChangeListener(PropertyChangeListener listener);
}
