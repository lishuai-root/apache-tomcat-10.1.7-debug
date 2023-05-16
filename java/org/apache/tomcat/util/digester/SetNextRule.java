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
package org.apache.tomcat.util.digester;

import org.apache.tomcat.util.IntrospectionUtils;


/**
 * <p>Rule implementation that calls a method on the (top-1) (parent)
 * object, passing the top object (child) as an argument.  It is
 * commonly used to establish parent-child relationships.</p>
 * <p>调用(top-1)(父)对象上的方法的规则实现，将top对象(子)作为参数传递。通常用于建立亲子关系。<p>
 *
 * <p>This rule now supports more flexible method matching by default.
 * It is possible that this may break (some) code
 * written against release 1.1.1 or earlier.
 * <p>默认情况下，该规则现在支持更灵活的方法匹配。这可能会破坏针对1.1.1或更早版本编写的(一些)代码。
 *
 * See {@link #isExactMatch()} for more details.</p>
 */

public class SetNextRule extends Rule {

    // ----------------------------------------------------------- Constructors

    /**
     * Construct a "set next" rule with the specified method name.
     *
     * @param methodName Method name of the parent method to call
     * @param paramType Java class of the parent method's argument
     *  (if you wish to use a primitive type, specify the corresponding
     *  Java wrapper class instead, such as <code>java.lang.Boolean</code>
     *  for a <code>boolean</code> parameter)
     */
    public SetNextRule(String methodName,
                       String paramType) {

        this.methodName = methodName;
        this.paramType = paramType;

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The method name to call on the parent object.
     */
    protected String methodName = null;


    /**
     * The Java class name of the parameter type expected by the method.
     */
    protected String paramType = null;

    /**
     * Should we use exact matching. Default is no.
     */
    protected boolean useExactMatch = false;

    // --------------------------------------------------------- Public Methods


    /**
     * <p>Is exact matching being used.</p>
     * <p>正在使用精确匹配
     *
     * <p>This rule uses <code>org.apache.commons.beanutils.MethodUtils</code>
     * to introspect the relevant objects so that the right method can be called.
     * Originally, <code>MethodUtils.invokeExactMethod</code> was used.
     * 该规则使用<code>org.apache.commons.beanutils.MethodUtils<code>来自检相关对象，
     * 以便可以调用正确的方法。最初,<代码> MethodUtils.invokeExactMethod<code>被使用。
     *
     * This matches methods very strictly
     * and so may not find a matching method when one exists.
     * This is still the behaviour when exact matching is enabled.</p>
     * 这将非常严格地匹配方法，因此在存在匹配方法时可能找不到匹配方法。当精确匹配被启用时，这仍然是行为。<p>
     *
     * <p>When exact matching is disabled, <code>MethodUtils.invokeMethod</code> is used.
     * This method finds more methods but is less precise when there are several methods
     * with correct signatures.
     * <p>当精确匹配被禁用时，使用<code>MethodUtils.invokeMethod<code>。此方法查找更多方法，
     * 但当有多个具有正确签名的方法时，此方法不太精确。
     *
     * So, if you want to choose an exact signature you might need to enable this property.</p>
     *因此，如果您想选择一个精确的签名，您可能需要启用此属性
     *
     * <p>The default setting is to disable exact matches.</p>
     * <p>默认设置为禁用精确匹配
     *
     * @return true iff exact matching is enabled
     * @since Digester Release 1.1.1
     */
    public boolean isExactMatch() {

        return useExactMatch;
    }

    /**
     * <p>Set whether exact matching is enabled.</p>
     *
     * <p>See {@link #isExactMatch()}.</p>
     *
     * @param useExactMatch should this rule use exact method matching
     * @since Digester Release 1.1.1
     */
    public void setExactMatch(boolean useExactMatch) {

        this.useExactMatch = useExactMatch;
    }

    /**
     * Process the end of this element.
     *
     * @param namespace the namespace URI of the matching element, or an
     *   empty string if the parser is not namespace aware or the element has
     *   no namespace
     * @param name the local name if the parser is namespace aware, or just
     *   the element name otherwise
     */
    @Override
    public void end(String namespace, String name) throws Exception {

        // Identify the objects to be used
        Object child = digester.peek(0);
        Object parent = digester.peek(1);
        if (digester.log.isDebugEnabled()) {
            if (parent == null) {
                digester.log.debug("[SetNextRule]{" + digester.match +
                        "} Call [NULL PARENT]." +
                        methodName + "(" + child + ")");
            } else {
                digester.log.debug("[SetNextRule]{" + digester.match +
                        "} Call " + parent.getClass().getName() + "." +
                        methodName + "(" + child + ")");
            }
        }

        // Call the specified method
        IntrospectionUtils.callMethod1(parent, methodName,
                child, paramType, digester.getClassLoader());

        StringBuilder code = digester.getGeneratedCode();
        if (code != null) {
            code.append(digester.toVariableName(parent)).append('.');
            code.append(methodName).append('(').append(digester.toVariableName(child)).append(");");
            code.append(System.lineSeparator());
        }
    }


    /**
     * Render a printable version of this Rule.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SetNextRule[");
        sb.append("methodName=");
        sb.append(methodName);
        sb.append(", paramType=");
        sb.append(paramType);
        sb.append(']');
        return sb.toString();
    }


}
