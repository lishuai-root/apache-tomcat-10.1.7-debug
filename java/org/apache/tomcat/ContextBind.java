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
package org.apache.tomcat;

public interface ContextBind {

    /**
     * Change the current thread context class loader to the web application
     * class loader. If no web application class loader is defined, or if the
     * current thread is already using the web application class loader then no
     * change will be made. If the class loader is changed and a
     * {@link org.apache.catalina.ThreadBindingListener} is configured then
     * {@link org.apache.catalina.ThreadBindingListener#bind()} will be called
     * after the change has been made.
     *
     * 将当前线程上下文类装入器更改为web应用程序类装入器。
     * 如果没有定义web应用程序类装入器，或者当前线程已经在使用web应用程序类装入器，则不会进行任何更改。
     * 如果类装入器被更改，并且如果配置了{@link org.apache.catalina.ThreadBindingListener}，
     * 那么{@link org.apache.catalina.ThreadBindingListener#bind()}将在更改完成后被调用。
     *
     *
     * @param usePrivilegedAction
     *          Should a {@link java.security.PrivilegedAction} be used when
     *          obtaining the current thread context class loader and setting
     *          the new one?
     * @param originalClassLoader
     *          The current class loader if known to save this method having to
     *          look it up
     *
     * @return If the class loader has been changed by the method it will return
     *         the thread context class loader in use when the method was
     *         called. If no change was made then this method returns null.
     */
    ClassLoader bind(boolean usePrivilegedAction, ClassLoader originalClassLoader);

    /**
     * Restore the current thread context class loader to the original class
     * loader in used before {@link #bind(boolean, ClassLoader)} was called. If
     * no original class loader is passed to this method then no change will be
     * made. If the class loader is changed and a
     * {@link org.apache.catalina.ThreadBindingListener} is configured then
     * {@link org.apache.catalina.ThreadBindingListener#unbind()} will be called
     * before the change is made.
     *
     * 将当前线程上下文类装入器恢复为调用{@link #bind(boolean, ClassLoader)}之前使用的原始类装入器。
     * 如果没有将原始类装入器传递给此方法，则不会进行任何更改。
     * 如果类装入器被更改，并且如果配置了{@link org.apache.catalina.ThreadBindingListener}，
     * 那么在进行更改之前将调用{@link org.apache.catalina.ThreadBindingListener#unbind()}。
     *
     *
     * @param usePrivilegedAction
     *          Should a {@link java.security.PrivilegedAction} be used when
     *          setting the current thread context class loader?
     * @param originalClassLoader
     *          The class loader to restore as the thread context class loader
     */
    void unbind(boolean usePrivilegedAction, ClassLoader originalClassLoader);
}
