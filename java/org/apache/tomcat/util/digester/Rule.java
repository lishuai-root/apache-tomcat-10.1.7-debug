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

import org.apache.tomcat.util.res.StringManager;
import org.xml.sax.Attributes;

/**
 * Concrete implementations of this class implement actions to be taken when
 * a corresponding nested pattern of XML elements has been matched.

 * 该类的具体实现实现了在匹配相应的XML元素嵌套模式时所采取的操作。
 */
public abstract class Rule {

    protected static final StringManager sm = StringManager.getManager(Rule.class);

    // ----------------------------------------------------------- Constructors

    /**
     * <p>Base constructor.
     * Now the digester will be set when the rule is added.</p>
     */
    public Rule() {}


    // ----------------------------------------------------- Instance Variables


    /**
     * The Digester with which this Rule is associated.
     */
    protected Digester digester = null;


    /**
     * The namespace URI for which this Rule is relevant, if any.
     * 与此规则相关的名称空间URI(如果有的话)。
     */
    protected String namespaceURI = null;


    // ------------------------------------------------------------- Properties

    /**
     * Identify the Digester with which this Rule is associated.
     *
     * @return the Digester with which this Rule is associated.
     */
    public Digester getDigester() {
        return digester;
    }


    /**
     * Set the <code>Digester</code> with which this <code>Rule</code> is
     * associated.
     * 设置与该<code>规则<code>相关联的<code>消化器<code>。
     *
     * @param digester The digester with which to associate this rule
     */
    public void setDigester(Digester digester) {
        this.digester = digester;
    }


    /**
     * Return the namespace URI for which this Rule is relevant, if any.
     *
     * @return The namespace URI for which this rule is relevant or
     *         <code>null</code> if none.
     */
    public String getNamespaceURI() {
        return namespaceURI;
    }


    /**
     * Set the namespace URI for which this Rule is relevant, if any.
     *
     * @param namespaceURI Namespace URI for which this Rule is relevant,
     *  or <code>null</code> to match independent of namespace.
     */
    public void setNamespaceURI(String namespaceURI) {
        this.namespaceURI = namespaceURI;
    }


    // --------------------------------------------------------- Public Methods

    /**
     * This method is called when the beginning of a matching XML element
     * is encountered. The default implementation is a NO-OP.
     * 当遇到匹配的XML元素的开头时调用此方法。默认实现是NO-OP。
     *
     * @param namespace the namespace URI of the matching element, or an
     *                  empty string if the parser is not namespace aware or the
     *                  element has no namespace
     * @param name the local name if the parser is namespace aware, or just
     *             the element name otherwise
     * @param attributes The attribute list of this element
     *
     * @throws Exception if an error occurs while processing the event
     */
    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        // NO-OP by default.
    }


    /**
     * This method is called when the body of a matching XML element is
     * encountered.  If the element has no body, this method is not called at
     * all. The default implementation is a NO-OP.
     * 当遇到匹配的XML元素体时调用此方法。如果元素没有主体，则根本不调用此方法。默认实现是NO-OP。
     *
     * @param namespace the namespace URI of the matching element, or an empty
     *                  string if the parser is not namespace aware or the
     *                  element has no namespace
     * @param name the local name if the parser is namespace aware, or just the
     *             element name otherwise
     * @param text The text of the body of this element
     *
     * @throws Exception if an error occurs while processing the event
     */
    public void body(String namespace, String name, String text) throws Exception {
        // NO-OP by default.
    }


    /**
     * This method is called when the end of a matching XML element
     * is encountered. The default implementation is a NO-OP.
     * 当遇到匹配的XML元素结束时调用此方法。默认实现是NO-OP。
     *
     * @param namespace the namespace URI of the matching element, or an empty
     *                  string if the parser is not namespace aware or the
     *                  element has no namespace
     * @param name the local name if the parser is namespace aware, or just the
     *             element name otherwise
     *
     * @throws Exception if an error occurs while processing the event
     */
    public void end(String namespace, String name) throws Exception {
        // NO-OP by default.
    }


    /**
     * This method is called after all parsing methods have been
     * called, to allow Rules to remove temporary data.
     * 在调用所有解析方法之后调用此方法，以允许Rules删除临时数据。
     *
     * @throws Exception if an error occurs while processing the event
     */
    public void finish() throws Exception {
        // NO-OP by default.
    }
}
