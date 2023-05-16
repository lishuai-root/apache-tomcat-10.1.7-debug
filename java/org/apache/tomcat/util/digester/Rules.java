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

import java.util.List;

/**
 * Public interface defining a collection of Rule instances (and corresponding
 * matching patterns) plus an implementation of a matching policy that selects
 * the rules that match a particular pattern of nested elements discovered
 * during parsing.
 *
 * 定义Rule实例(和相应的匹配模式)集合的公共接口，以及匹配策略的实现，该策略选择与解析过程中发现的特定嵌套元素模式匹配的规则。
 */
public interface Rules {

    // ------------------------------------------------------------- Properties

    /**
     * @return the Digester instance with which this Rules instance is
     * associated.
     */
    Digester getDigester();


    /**
     * Set the Digester instance with which this Rules instance is associated.
     *
     * @param digester The newly associated Digester instance
     */
    void setDigester(Digester digester);


    // --------------------------------------------------------- Public Methods

    /**
     * Register a new Rule instance matching the specified pattern.
     *
     * @param pattern Nesting pattern to be matched for this Rule
     * @param rule Rule instance to be registered
     */
    void add(String pattern, Rule rule);


    /**
     * Clear all existing Rule instance registrations.
     */
    void clear();


    /**
     * Return a List of all registered Rule instances that match the specified
     * nesting pattern, or a zero-length List if there are no matches.  If more
     * than one Rule instance matches, they <strong>must</strong> be returned
     * in the order originally registered through the <code>add()</code>
     * method.
     * 返回与指定嵌套模式匹配的所有已注册Rule实例的List，如果没有匹配，则返回零长度List。
     * 如果有多个Rule实例匹配，它们<strong>必须按照最初通过<code>add()<code>方法注册的顺序返回<strong>。
     *
     *
     * @param namespaceURI Namespace URI for which to select matching rules,
     *  or <code>null</code> to match regardless of namespace URI
     * @param pattern Nesting pattern to be matched
     * @return a rules list
     */
    List<Rule> match(String namespaceURI, String pattern);


    /**
     * Return a List of all registered Rule instances, or a zero-length List
     * if there are no registered Rule instances.  If more than one Rule
     * instance has been registered, they <strong>must</strong> be returned
     * in the order originally registered through the <code>add()</code>
     * method.
     * @return a rules list
     */
    List<Rule> rules();
}
