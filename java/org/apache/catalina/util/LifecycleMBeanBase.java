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

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.catalina.Globals;
import org.apache.catalina.JmxEnabled;
import org.apache.catalina.LifecycleException;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.modeler.Registry;
import org.apache.tomcat.util.res.StringManager;

public abstract class LifecycleMBeanBase extends LifecycleBase
        implements JmxEnabled {

    private static final Log log = LogFactory.getLog(LifecycleMBeanBase.class);

    private static final StringManager sm =
        StringManager.getManager("org.apache.catalina.util");


    /* Cache components of the MBean registration. */
    private String domain = null;
    private ObjectName oname = null;

    /**
     * Sub-classes wishing to perform additional initialization should override
     * this method, ensuring that super.initInternal() is the first call in the
     * overriding method.
     * 希望执行额外初始化的子类应该重写此方法，确保super.initInternal()是重写方法中的第一个调用。
     */
    @Override
    protected void initInternal() throws LifecycleException {
        // If oname is not null then registration has already happened via
        // preRegister().
        /**
         * 如果oname不为空，那么注册已经通过preRegister()发生了。
         */
        if (oname == null) {
            oname = register(this, getObjectNameKeyProperties());
        }
    }


    /**
     * Sub-classes wishing to perform additional clean-up should override this
     * method, ensuring that super.destroyInternal() is the last call in the
     * overriding method.
     * 希望执行额外清理的子类应该重写此方法，确保super.destroyInternal()是重写方法中的最后一个调用。
     */
    @Override
    protected void destroyInternal() throws LifecycleException {
        unregister(oname);
    }


    /**
     * Specify the domain under which this component should be registered. Used
     * with components that cannot (easily) navigate the component hierarchy to
     * determine the correct domain to use.
     * 指定应该在其中注册此组件的域。用于不能(容易地)导航组件层次结构以确定要使用的正确域的组件。
     */
    @Override
    public final void setDomain(String domain) {
        this.domain = domain;
    }


    /**
     * Obtain the domain under which this component will be / has been
     * registered.
     * 获取将在其中注册该组件的域。
     */
    @Override
    public final String getDomain() {
        if (domain == null) {
            domain = getDomainInternal();
        }

        if (domain == null) {
            domain = Globals.DEFAULT_MBEAN_DOMAIN;
        }

        return domain;
    }


    /**
     * Method implemented by sub-classes to identify the domain in which MBeans
     * should be registered.
     * 由子类实现的方法，用于标识应该在其中注册mbean的域。
     *
     * @return  The name of the domain to use to register MBeans.
     */
    protected abstract String getDomainInternal();


    /**
     * Obtain the name under which this component has been registered with JMX.
     */
    @Override
    public final ObjectName getObjectName() {
        return oname;
    }


    /**
     * Allow sub-classes to specify the key properties component of the
     * {@link ObjectName} that will be used to register this component.
     * 允许子类指定{@link ObjectName}的关键属性组件，用于注册该组件。
     *
     * @return  The string representation of the key properties component of the
     *          desired {@link ObjectName}
     */
    protected abstract String getObjectNameKeyProperties();


    /**
     * Utility method to enable sub-classes to easily register additional
     * components that don't implement {@link JmxEnabled} with an MBean server.
     * 实用程序方法，使子类能够轻松地注册没有在MBean服务器上实现{@link JmxEnabled}的附加组件。
     *
     * <br>
     * Note: This method should only be used once {@link #initInternal()} has
     * been called and before {@link #destroyInternal()} has been called.
     * 注意:此方法只能在{@link #initInternal()}被调用后和{@link #destroyInternal()}被调用前使用。
     *
     * @param obj                       The object the register
     * @param objectNameKeyProperties   The key properties component of the
     *                                  object name to use to register the
     *                                  object
     *
     * @return  The name used to register the object
     */
    protected final ObjectName register(Object obj,
            String objectNameKeyProperties) {

        // Construct an object name with the right domain
        StringBuilder name = new StringBuilder(getDomain());
        name.append(':');
        name.append(objectNameKeyProperties);

        ObjectName on = null;

        try {
            on = new ObjectName(name.toString());
            Registry.getRegistry(null, null).registerComponent(obj, on, null);
        } catch (Exception e) {
            log.warn(sm.getString("lifecycleMBeanBase.registerFail", obj, name), e);
        }

        return on;
    }


    /**
     * Utility method to enable sub-classes to easily unregister additional
     * components that don't implement {@link JmxEnabled} with an MBean server.
     * 实用程序方法，使子类能够轻松地注销未在MBean服务器上实现{@link JmxEnabled}的附加组件。
     *
     * <br>
     * Note: This method should only be used once {@link #initInternal()} has
     * been called and before {@link #destroyInternal()} has been called.
     * 注意:此方法只能在{@link #initInternal()}被调用后和{@link #destroyInternal()}被调用前使用。
     *
     * @param objectNameKeyProperties   The key properties component of the
     *                                  object name to use to unregister the
     *                                  object
     */
    protected final void unregister(String objectNameKeyProperties) {
        // Construct an object name with the right domain
        StringBuilder name = new StringBuilder(getDomain());
        name.append(':');
        name.append(objectNameKeyProperties);
        Registry.getRegistry(null, null).unregisterComponent(name.toString());
    }


    /**
     * Utility method to enable sub-classes to easily unregister additional
     * components that don't implement {@link JmxEnabled} with an MBean server.
     * 实用程序方法，使子类能够轻松地注销未在MBean服务器上实现{@link JmxEnabled}的附加组件。
     *
     * <br>
     * Note: This method should only be used once {@link #initInternal()} has
     * been called and before {@link #destroyInternal()} has been called.
     * 注意:此方法只能在{@link #initInternal()}被调用后和{@link #destroyInternal()}被调用前使用。
     *
     * @param on    The name of the component to unregister
     */
    protected final void unregister(ObjectName on) {
        Registry.getRegistry(null, null).unregisterComponent(on);
    }


    /**
     * Not used - NOOP.
     */
    @Override
    public final void postDeregister() {
        // NOOP
    }


    /**
     * Not used - NOOP.
     */
    @Override
    public final void postRegister(Boolean registrationDone) {
        // NOOP
    }


    /**
     * Not used - NOOP.
     */
    @Override
    public final void preDeregister() throws Exception {
        // NOOP
    }


    /**
     * Allows the object to be registered with an alternative
     * {@link MBeanServer} and/or {@link ObjectName}.
     * 允许对象注册为{@link MBeanServer}或{@link ObjectName}。
     */
    @Override
    public final ObjectName preRegister(MBeanServer server, ObjectName name)
            throws Exception {

        this.oname = name;
        this.domain = name.getDomain().intern();

        return oname;
    }

}
