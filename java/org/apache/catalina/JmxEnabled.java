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

import javax.management.MBeanRegistration;
import javax.management.ObjectName;

/**
 * 该接口是tomcat中需要实现JMX组件的公共接口
 *
 * JMX : {@link main.jmx.Register}
 * JMX是一个为应用程序植入管理功能的框架，可以用于监控程序运行状态和统计信息，无需重启修改应用程序配置，状态变化或者出错时通知
 *
 *    基于浏览器的管理应用       基于SNMP的管理应用         基于JXM的管理应用
 *            |                       |                       |
 *           \|/                     \|/                     \|/
 *      HTML Adaptor              SNMP Adaptor             JXM Adaptor
 *           |                        |                        |
 *           |                       \|/                       |
 *           -------------------> MBean Server <---------------
 *                                   /|\
 *          -------------->-----------|-----------<-------------
 *         /|\                        |                       /|\
 *         MBean                     MBean                   MBean
 *
 * tomcat中的内部组件通过实现当前接口通过JMX将组件暴漏给外部，使得外部可以直接访问tomcat内部组件
 *
 * This interface is implemented by components that will be registered with an
 * MBean server when they are created and unregistered when they are destroyed.
 * 该接口由组件实现，这些组件在创建时向MBean服务器注册，在销毁时取消注册。
 *
 * It is primarily intended to be implemented by components that implement
 * {@link Lifecycle} but is not exclusively for them.
 * 它主要是由实现{@link Lifecycle}的组件实现的，但不是专门为它们实现的。
 */
public interface JmxEnabled extends MBeanRegistration {

    /**
     * @return the domain under which this component will be / has been
     * registered.
     */
    String getDomain();


    /**
     * Specify the domain under which this component should be registered. Used
     * with components that cannot (easily) navigate the component hierarchy to
     * determine the correct domain to use.
     * 指定应该在其中注册此组件的域。用于不能(容易地)导航组件层次结构以确定要使用的正确域的组件。
     *
     * @param domain The name of the domain under which this component should be
     *               registered
     */
    void setDomain(String domain);


    /**
     * @return the name under which this component has been registered with JMX.
     */
    ObjectName getObjectName();
}
