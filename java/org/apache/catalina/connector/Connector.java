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
package org.apache.catalina.connector;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;

import javax.management.ObjectName;

import org.apache.catalina.Globals;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Service;
import org.apache.catalina.core.AprStatus;
import org.apache.catalina.util.LifecycleMBeanBase;
import org.apache.coyote.AbstractProtocol;
import org.apache.coyote.Adapter;
import org.apache.coyote.ProtocolHandler;
import org.apache.coyote.UpgradeProtocol;
import org.apache.coyote.http11.AbstractHttp11JsseProtocol;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.tomcat.util.buf.B2CConverter;
import org.apache.tomcat.util.buf.CharsetUtil;
import org.apache.tomcat.util.buf.EncodedSolidusHandling;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.openssl.OpenSSLImplementation;
import org.apache.tomcat.util.res.StringManager;


/**
 * Connector节点代表与客户处理连接的组件，负责接收客户的请求，并回复客户响应。
 * port: 监听的端口
 * protocol: 连接使用的协议
 * connectionTimeout: 连接超时时间
 * redirectPort: 当用户用http请求某个资源，而该资源本身又被设置了必须要https方式访问，此时Tomcat会自动重定向到这个redirectPort设置的https端口
 * acceptCount: 设定在监听端口队列中的最大客户请求数,默认值为10，如果队列已满,客户请求将被拒绝
 * address: 如果服务器有两个以上IP地址,该属性可以设定端口监听的IP地址,默认情况下,端口会监听服务器上所有IP地址
 * enableLookups: 如果设为true,表示支持域名解析,可以把IP地址解析为主机名.Web应用调用request.getRemoteHost方法将返回客户的主机名.该属性默认值为true
 * executor: 配置tomcat的线程池，在此配置的名称与server.xml文件中Service节点下的Executor节点name属性相对应
 * compression: 如果带宽有限的话，可以用GZIP压缩，off：表示禁止压缩、on：表示允许压缩（文本将被压缩）、force：表示所有情况下都进行压缩，默认值为off
 * URIEncoding: 用于解码URL的字符编码，没有指定默认值为ISO-8859-1
 *
 * Implementation of a Coyote connector.
 * Coyote连接器的实现。
 *
 * @author Craig R. McClanahan
 * @author Remy Maucherat
 */
public class Connector extends LifecycleMBeanBase {

    private static final Log log = LogFactory.getLog(Connector.class);


    public static final String INTERNAL_EXECUTOR_NAME = "Internal";


    // ------------------------------------------------------------ Constructor

    /**
     * Defaults to using HTTP/1.1 NIO implementation.
     * 默认使用HTTP1.1 NIO实现。
     */
    public Connector() {
        this("HTTP/1.1");
    }


    /**
     * 根据协议创建连接
     *
     * @param protocol
     */
    public Connector(String protocol) {
        configuredProtocol = protocol;
        ProtocolHandler p = null;
        try {
            p = ProtocolHandler.create(protocol);
        } catch (Exception e) {
            log.error(sm.getString("coyoteConnector.protocolHandlerInstantiationFailed"), e);
        }
        if (p != null) {
            protocolHandler = p;
            protocolHandlerClassName = protocolHandler.getClass().getName();
        } else {
            protocolHandler = null;
            protocolHandlerClassName = protocol;
        }
        // Default for Connector depends on this system property
        /**
         * Connector的默认值取决于此系统属性，默认为false
         */
        setThrowOnFailure(Boolean.getBoolean("org.apache.catalina.startup.EXIT_ON_INIT_FAILURE"));
    }


    public Connector(ProtocolHandler protocolHandler) {
        protocolHandlerClassName = protocolHandler.getClass().getName();
        configuredProtocol = protocolHandlerClassName;
        this.protocolHandler = protocolHandler;
        // Default for Connector depends on this system property
        setThrowOnFailure(Boolean.getBoolean("org.apache.catalina.startup.EXIT_ON_INIT_FAILURE"));
    }


    // ----------------------------------------------------- Instance Variables

    /**
     * The <code>Service</code> we are associated with (if any).
     * 与我们关联的<code>服务<code>(如果有的话)。
     */
    protected Service service = null;


    /**
     * If this is <code>true</code> the '\' character will be permitted as a path delimiter. If not specified, the
     * default value of <code>false</code> will be used.
     * 如果<code>为true， <code>将允许'\'字符作为路径分隔符。如果未指定，则使用默认值false<code>。
     */
    protected boolean allowBackslash = false;


    /**
     * Do we allow TRACE ?
     */
    protected boolean allowTrace = false;


    /**
     * Default timeout for asynchronous requests (ms).
     * 异步请求的默认超时时间(毫秒)。
     */
    protected long asyncTimeout = 30000;


    /**
     * The "enable DNS lookups" flag for this Connector.
     * 此连接器的“启用DNS查找”标志。
     */
    protected boolean enableLookups = false;


    /**
     * If this is <code>true</code> then a call to <code>Response.getWriter()</code> if no character encoding has been
     * specified will result in subsequent calls to <code>Response.getCharacterEncoding()</code> returning
     * <code>ISO-8859-1</code> and the <code>Content-Type</code> response header will include a
     * <code>charset=ISO-8859-1</code> component. (SRV.15.2.22.1) If not specified, the default specification compliant
     * value of <code>true</code> will be used.
     *
     * 如果这是<code>true<code>，那么调用<code> response.getWriter()<code>，如果没有指定字符编码，
     * 将导致后续调用<code> response.getCharacterEncoding()<code>返回<code>ISO-8859-1<code>，
     * 并且<code>Content-Type<code>响应头将包含<code>charset=ISO-8859-1<code>组件。(SRV.15.2.22.1)如果未指定，则使用<code>true<code>的默认规范兼容值。
     */
    protected boolean enforceEncodingInGetWriter = true;


    /**
     * Is generation of X-Powered-By response header enabled/disabled?
     * 生成X-Powered-By响应头是否启用或禁用?
     */
    protected boolean xpoweredBy = false;


    /**
     * The server name to which we should pretend requests to this Connector were directed. This is useful when
     * operating Tomcat behind a proxy server, so that redirects get constructed accurately. If not specified, the
     * server name included in the <code>Host</code> header is used.
     * 我们应该假装向此连接器发出的请求被定向到的服务器名称。
     * 当在代理服务器后面操作Tomcat时，这很有用，这样可以准确地构建重定向。如果未指定，则使用<code>主机<code>报头中包含的服务器名。
     */
    protected String proxyName = null;


    /**
     * The server port to which we should pretend requests to this Connector were directed. This is useful when
     * operating Tomcat behind a proxy server, so that redirects get constructed accurately. If not specified, the port
     * number specified by the <code>port</code> property is used.
     * 我们应该假装向该连接器发送请求的服务器端口。
     * 当在代理服务器后面操作Tomcat时，这很有用，这样可以准确地构建重定向。如果未指定，则使用<code>port<code>属性指定的端口号。
     */
    protected int proxyPort = 0;


    /**
     * The flag that controls recycling of the facades of the request processing objects. If set to <code>true</code>
     * the object facades will be discarded when the request is recycled. If the security manager is enabled, this
     * setting is ignored and object facades are always discarded.
     * 控制请求处理对象的外观回收的标志。
     * 如果设置为<code>true<code>，则对象facade将在回收请求时被丢弃。如果启用了安全管理器，则忽略此设置，并且始终丢弃对象外观。
     */
    protected boolean discardFacades = true;


    /**
     * The redirect port for non-SSL to SSL redirects.
     * 非SSL到SSL重定向的重定向端口。
     */
    protected int redirectPort = 443;


    /**
     * The request scheme that will be set on all requests received through this connector.
     * 将在通过该连接器接收的所有请求上设置的请求方案。
     */
    protected String scheme = "http";


    /**
     * The secure connection flag that will be set on all requests received through this connector.
     * 将在通过该连接器接收的所有请求上设置的安全连接标志。
     */
    protected boolean secure = false;


    /**
     * The string manager for this package.
     */
    protected static final StringManager sm = StringManager.getManager(Connector.class);


    /**
     * The maximum number of cookies permitted for a request. Use a value less than zero for no limit. Defaults to 200.
     * 一个请求允许的最大cookie数。如果没有限制，则使用小于零的值。默认为200。
     */
    private int maxCookieCount = 200;

    /**
     * The maximum number of parameters (GET plus POST) which will be automatically parsed by the container. 10000 by
     * default. A value of less than 0 means no limit.
     * 容器将自动解析的参数的最大数目(GET加POST)。缺省值为10000。小于0的值表示没有限制。
     */
    protected int maxParameterCount = 10000;

    /**
     * Maximum size of a POST which will be automatically parsed by the container. 2MB by default.
     * POST的最大大小，它将被容器自动解析。默认为2MB。
     */
    protected int maxPostSize = 2 * 1024 * 1024;


    /**
     * Maximum size of a POST which will be saved by the container during authentication. 4kB by default
     * 在身份验证期间容器将保存的POST的最大大小。默认为4kB
     */
    protected int maxSavePostSize = 4 * 1024;

    /**
     * Comma-separated list of HTTP methods that will be parsed according to POST-style rules for
     * application/x-www-form-urlencoded request bodies.
     * 以逗号分隔的HTTP方法列表，这些方法将根据application/x-www-form-urlencoded请求体的post样式规则进行解析。
     */
    protected String parseBodyMethods = "POST";

    /**
     * A Set of methods determined by {@link #parseBodyMethods}.
     * 一组由{@link #parseBodyMethods}决定的方法。
     */
    protected HashSet<String> parseBodyMethodsSet;


    /**
     * Flag to use IP-based virtual hosting.
     * 使用基于ip的虚拟主机的标志。
     */
    protected boolean useIPVHosts = false;


    /**
     * Coyote Protocol handler class name. See {@link #Connector()} for current default.
     * Coyote协议处理程序类名。请参阅{@link #Connector()}获取当前默认值。
     */
    protected final String protocolHandlerClassName;


    /**
     * Name of the protocol that was configured.
     * 配置的协议名称。 默认是"HTTP/1.1"，在conf/server.xml中配置
     */
    protected final String configuredProtocol;


    /**
     * Coyote protocol handler.
     */
    protected final ProtocolHandler protocolHandler;


    /**
     * Coyote adapter.
     */
    protected Adapter adapter = null;


    /**
     * The URI encoding in use.
     */
    private Charset uriCharset = StandardCharsets.UTF_8;


    /**
     * The behavior when an encoded solidus (slash) is submitted.
     */
    private EncodedSolidusHandling encodedSolidusHandling = EncodedSolidusHandling.REJECT;


    /**
     * URI encoding as body.
     */
    protected boolean useBodyEncodingForURI = false;


    private boolean rejectSuspiciousURIs;


    // ------------------------------------------------------------- Properties

    /**
     * Return a property from the protocol handler.
     *
     * @param name the property name
     *
     * @return the property value
     */
    public Object getProperty(String name) {
        if (protocolHandler == null) {
            return null;
        }
        return IntrospectionUtils.getProperty(protocolHandler, name);
    }


    /**
     * Set a property on the protocol handler.
     *
     * @param name  the property name
     * @param value the property value
     *
     * @return <code>true</code> if the property was successfully set
     */
    public boolean setProperty(String name, String value) {
        if (protocolHandler == null) {
            return false;
        }
        return IntrospectionUtils.setProperty(protocolHandler, name, value);
    }


    /**
     * @return the <code>Service</code> with which we are associated (if any).
     */
    public Service getService() {
        return this.service;
    }


    /**
     * Set the <code>Service</code> with which we are associated (if any).
     * 设置与我们关联的<code>Service<code>(如果有的话)。
     *
     * @param service The service that owns this Engine
     */
    public void setService(Service service) {
        this.service = service;
    }


    /**
     * @return <code>true</code> if backslash characters are allowed in URLs. Default value is <code>false</code>.
     */
    public boolean getAllowBackslash() {
        return allowBackslash;
    }


    /**
     * Set the allowBackslash flag.
     *
     * @param allowBackslash the new flag value
     */
    public void setAllowBackslash(boolean allowBackslash) {
        this.allowBackslash = allowBackslash;
    }


    /**
     * @return <code>true</code> if the TRACE method is allowed. Default value is <code>false</code>.
     */
    public boolean getAllowTrace() {
        return this.allowTrace;
    }


    /**
     * Set the allowTrace flag, to disable or enable the TRACE HTTP method.
     *
     * @param allowTrace The new allowTrace flag
     */
    public void setAllowTrace(boolean allowTrace) {
        this.allowTrace = allowTrace;
    }


    /**
     * @return the default timeout for async requests in ms.
     */
    public long getAsyncTimeout() {
        return asyncTimeout;
    }


    /**
     * Set the default timeout for async requests.
     *
     * @param asyncTimeout The new timeout in ms.
     */
    public void setAsyncTimeout(long asyncTimeout) {
        this.asyncTimeout = asyncTimeout;
    }


    /**
     * @return <code>true</code> if the object facades are discarded, either when the discardFacades value is
     *             <code>true</code> or when the security manager is enabled.
     */
    public boolean getDiscardFacades() {
        return discardFacades || Globals.IS_SECURITY_ENABLED;
    }


    /**
     * Set the recycling strategy for the object facades.
     *
     * @param discardFacades the new value of the flag
     */
    public void setDiscardFacades(boolean discardFacades) {
        this.discardFacades = discardFacades;
    }


    /**
     * @return the "enable DNS lookups" flag.
     */
    public boolean getEnableLookups() {
        return this.enableLookups;
    }


    /**
     * Set the "enable DNS lookups" flag.
     *
     * @param enableLookups The new "enable DNS lookups" flag value
     */
    public void setEnableLookups(boolean enableLookups) {
        this.enableLookups = enableLookups;
    }


    /**
     * @return <code>true</code> if a default character encoding will be set when calling Response.getWriter()
     */
    public boolean getEnforceEncodingInGetWriter() {
        return enforceEncodingInGetWriter;
    }


    /**
     * Set the enforceEncodingInGetWriter flag.
     *
     * @param enforceEncodingInGetWriter the new flag value
     */
    public void setEnforceEncodingInGetWriter(boolean enforceEncodingInGetWriter) {
        this.enforceEncodingInGetWriter = enforceEncodingInGetWriter;
    }


    public int getMaxCookieCount() {
        return maxCookieCount;
    }


    public void setMaxCookieCount(int maxCookieCount) {
        this.maxCookieCount = maxCookieCount;
    }


    /**
     * @return the maximum number of parameters (GET plus POST) that will be automatically parsed by the container. A
     *             value of less than 0 means no limit.
     */
    public int getMaxParameterCount() {
        return maxParameterCount;
    }


    /**
     * Set the maximum number of parameters (GET plus POST) that will be automatically parsed by the container. A value
     * of less than 0 means no limit.
     *
     * @param maxParameterCount The new setting
     */
    public void setMaxParameterCount(int maxParameterCount) {
        this.maxParameterCount = maxParameterCount;
    }


    /**
     * @return the maximum size of a POST which will be automatically parsed by the container.
     */
    public int getMaxPostSize() {
        return maxPostSize;
    }


    /**
     * Set the maximum size of a POST which will be automatically parsed by the container.
     *
     * @param maxPostSize The new maximum size in bytes of a POST which will be automatically parsed by the container
     */
    public void setMaxPostSize(int maxPostSize) {
        this.maxPostSize = maxPostSize;
    }


    /**
     * @return the maximum size of a POST which will be saved by the container during authentication.
     */
    public int getMaxSavePostSize() {
        return maxSavePostSize;
    }


    /**
     * Set the maximum size of a POST which will be saved by the container during authentication.
     *
     * @param maxSavePostSize The new maximum size in bytes of a POST which will be saved by the container during
     *                            authentication.
     */
    public void setMaxSavePostSize(int maxSavePostSize) {
        this.maxSavePostSize = maxSavePostSize;
        setProperty("maxSavePostSize", String.valueOf(maxSavePostSize));
    }


    /**
     * @return the HTTP methods which will support body parameters parsing
     */
    public String getParseBodyMethods() {
        return this.parseBodyMethods;
    }


    /**
     * Set list of HTTP methods which should allow body parameter parsing. This defaults to <code>POST</code>.
     * 设置允许体参数解析的HTTP方法列表。默认为<code>POST<code>。
     *
     * @param methods Comma separated list of HTTP method names
     */
    public void setParseBodyMethods(String methods) {

        HashSet<String> methodSet = new HashSet<>();

        if (null != methods) {
            methodSet.addAll(Arrays.asList(methods.split("\\s*,\\s*")));
        }

        if (methodSet.contains("TRACE")) {
            throw new IllegalArgumentException(sm.getString("coyoteConnector.parseBodyMethodNoTrace"));
        }

        this.parseBodyMethods = methods;
        this.parseBodyMethodsSet = methodSet;
    }


    protected boolean isParseBodyMethod(String method) {
        return parseBodyMethodsSet.contains(method);
    }


    /**
     * @return the port number on which this connector is configured to listen for requests. The special value of 0
     *             means select a random free port when the socket is bound.
     */
    public int getPort() {
        // Try shortcut that should work for nearly all uses first as it does
        // not use reflection and is therefore faster.
        if (protocolHandler instanceof AbstractProtocol<?>) {
            return ((AbstractProtocol<?>) protocolHandler).getPort();
        }
        // Fall back for custom protocol handlers not based on AbstractProtocol
        Object port = getProperty("port");
        if (port instanceof Integer) {
            return ((Integer) port).intValue();
        }
        // Usually means an invalid protocol has been configured
        return -1;
    }


    /**
     * Set the port number on which we listen for requests.
     *
     * @param port The new port number
     */
    public void setPort(int port) {
        setProperty("port", String.valueOf(port));
    }


    public int getPortOffset() {
        // Try shortcut that should work for nearly all uses first as it does
        // not use reflection and is therefore faster.
        if (protocolHandler instanceof AbstractProtocol<?>) {
            return ((AbstractProtocol<?>) protocolHandler).getPortOffset();
        }
        // Fall back for custom protocol handlers not based on AbstractProtocol
        Object port = getProperty("portOffset");
        if (port instanceof Integer) {
            return ((Integer) port).intValue();
        }
        // Usually means an invalid protocol has been configured.
        return 0;
    }


    public void setPortOffset(int portOffset) {
        setProperty("portOffset", String.valueOf(portOffset));
    }


    public int getPortWithOffset() {
        int port = getPort();
        // Zero is a special case and negative values are invalid
        if (port > 0) {
            return port + getPortOffset();
        }
        return port;
    }


    /**
     * @return the port number on which this connector is listening to requests. If the special value for
     *             {@link #getPort} of zero is used then this method will report the actual port bound.
     */
    public int getLocalPort() {
        return ((Integer) getProperty("localPort")).intValue();
    }


    /**
     * @return the Coyote protocol handler in use.
     */
    public String getProtocol() {
        return configuredProtocol;
    }


    /**
     * @return the class name of the Coyote protocol handler in use.
     */
    public String getProtocolHandlerClassName() {
        return this.protocolHandlerClassName;
    }


    /**
     * @return the protocol handler associated with the connector.
     */
    public ProtocolHandler getProtocolHandler() {
        return this.protocolHandler;
    }


    /**
     * @return the proxy server name for this Connector.
     */
    public String getProxyName() {
        return this.proxyName;
    }


    /**
     * Set the proxy server name for this Connector.
     *
     * @param proxyName The new proxy server name
     */
    public void setProxyName(String proxyName) {

        if (proxyName != null && proxyName.length() > 0) {
            this.proxyName = proxyName;
        } else {
            this.proxyName = null;
        }
    }


    /**
     * @return the proxy server port for this Connector.
     */
    public int getProxyPort() {
        return this.proxyPort;
    }


    /**
     * Set the proxy server port for this Connector.
     *
     * @param proxyPort The new proxy server port
     */
    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }


    /**
     * @return the port number to which a request should be redirected if it comes in on a non-SSL port and is subject
     *             to a security constraint with a transport guarantee that requires SSL.
     */
    public int getRedirectPort() {
        return this.redirectPort;
    }


    /**
     * Set the redirect port number.
     *
     * @param redirectPort The redirect port number (non-SSL to SSL)
     */
    public void setRedirectPort(int redirectPort) {
        this.redirectPort = redirectPort;
    }


    public int getRedirectPortWithOffset() {
        return getRedirectPort() + getPortOffset();
    }


    /**
     * @return the scheme that will be assigned to requests received through this connector. Default value is "http".
     */
    public String getScheme() {
        return this.scheme;
    }


    /**
     * Set the scheme that will be assigned to requests received through this connector.
     *
     * @param scheme The new scheme
     */
    public void setScheme(String scheme) {
        this.scheme = scheme;
    }


    /**
     * @return the secure connection flag that will be assigned to requests received through this connector. Default
     *             value is "false".
     */
    public boolean getSecure() {
        return this.secure;
    }


    /**
     * Set the secure connection flag that will be assigned to requests received through this connector.
     *
     * @param secure The new secure connection flag
     */
    public void setSecure(boolean secure) {
        this.secure = secure;
        setProperty("secure", Boolean.toString(secure));
    }


    /**
     * @return the name of character encoding to be used for the URI using the original case.
     */
    public String getURIEncoding() {
        return uriCharset.name();
    }


    /**
     * @return The Charset to use to convert raw URI bytes (after %nn decoding) to characters. This will never be null
     */
    public Charset getURICharset() {
        return uriCharset;
    }

    /**
     * Set the URI encoding to be used for the URI.
     *
     * @param URIEncoding The new URI character encoding.
     */
    public void setURIEncoding(String URIEncoding) {
        try {
            Charset charset = B2CConverter.getCharset(URIEncoding);
            if (!CharsetUtil.isAsciiSuperset(charset)) {
                log.error(sm.getString("coyoteConnector.notAsciiSuperset", URIEncoding, uriCharset.name()));
                return;
            }
            uriCharset = charset;
        } catch (UnsupportedEncodingException e) {
            log.error(sm.getString("coyoteConnector.invalidEncoding", URIEncoding, uriCharset.name()), e);
        }
    }


    /**
     * @return the true if the entity body encoding should be used for the URI.
     */
    public boolean getUseBodyEncodingForURI() {
        return this.useBodyEncodingForURI;
    }


    /**
     * Set if the entity body encoding should be used for the URI.
     *
     * @param useBodyEncodingForURI The new value for the flag.
     */
    public void setUseBodyEncodingForURI(boolean useBodyEncodingForURI) {
        this.useBodyEncodingForURI = useBodyEncodingForURI;
    }

    /**
     * Indicates whether the generation of an X-Powered-By response header for Servlet-generated responses is enabled or
     * disabled for this Connector.
     *
     * @return <code>true</code> if generation of X-Powered-By response header is enabled, false otherwise
     */
    public boolean getXpoweredBy() {
        return xpoweredBy;
    }


    /**
     * Enables or disables the generation of an X-Powered-By header (with value Servlet/2.5) for all servlet-generated
     * responses returned by this Connector.
     *
     * @param xpoweredBy true if generation of X-Powered-By response header is to be enabled, false otherwise
     */
    public void setXpoweredBy(boolean xpoweredBy) {
        this.xpoweredBy = xpoweredBy;
    }


    /**
     * Enable the use of IP-based virtual hosting.
     *
     * @param useIPVHosts <code>true</code> if Hosts are identified by IP, <code>false</code> if Hosts are identified by
     *                        name.
     */
    public void setUseIPVHosts(boolean useIPVHosts) {
        this.useIPVHosts = useIPVHosts;
    }


    /**
     * Test if IP-based virtual hosting is enabled.
     *
     * @return <code>true</code> if IP vhosts are enabled
     */
    public boolean getUseIPVHosts() {
        return useIPVHosts;
    }


    public String getExecutorName() {
        Object obj = protocolHandler.getExecutor();
        if (obj instanceof org.apache.catalina.Executor) {
            return ((org.apache.catalina.Executor) obj).getName();
        }
        return INTERNAL_EXECUTOR_NAME;
    }


    public void addSslHostConfig(SSLHostConfig sslHostConfig) {
        protocolHandler.addSslHostConfig(sslHostConfig);
    }


    public SSLHostConfig[] findSslHostConfigs() {
        return protocolHandler.findSslHostConfigs();
    }


    public void addUpgradeProtocol(UpgradeProtocol upgradeProtocol) {
        protocolHandler.addUpgradeProtocol(upgradeProtocol);
    }


    public UpgradeProtocol[] findUpgradeProtocols() {
        return protocolHandler.findUpgradeProtocols();
    }


    public String getEncodedSolidusHandling() {
        return encodedSolidusHandling.getValue();
    }


    public void setEncodedSolidusHandling(String encodedSolidusHandling) {
        this.encodedSolidusHandling = EncodedSolidusHandling.fromString(encodedSolidusHandling);
    }


    public EncodedSolidusHandling getEncodedSolidusHandlingInternal() {
        return encodedSolidusHandling;
    }


    public boolean getRejectSuspiciousURIs() {
        return rejectSuspiciousURIs;
    }


    public void setRejectSuspiciousURIs(boolean rejectSuspiciousURIs) {
        this.rejectSuspiciousURIs = rejectSuspiciousURIs;
    }


    // --------------------------------------------------------- Public Methods

    /**
     * Create (or allocate) and return a Request object suitable for specifying the contents of a Request to the
     * responsible Container.
     *
     * @return a new Servlet request object
     */
    public Request createRequest() {
        return new Request(this);
    }


    /**
     * Create (or allocate) and return a Response object suitable for receiving the contents of a Response from the
     * responsible Container.
     *
     * @return a new Servlet response object
     */
    public Response createResponse() {
        int size = protocolHandler.getDesiredBufferSize();
        if (size > 0) {
            return new Response(size);
        } else {
            return new Response();
        }
    }


    protected String createObjectNameKeyProperties(String type) {

        Object addressObj = getProperty("address");

        StringBuilder sb = new StringBuilder("type=");
        sb.append(type);
        String id = (protocolHandler != null) ? protocolHandler.getId() : null;
        if (id != null) {
            // Maintain MBean name compatibility, even if not accurate
            sb.append(",port=0,address=");
            sb.append(ObjectName.quote(id));
        } else {
            sb.append(",port=");
            int port = getPortWithOffset();
            if (port > 0) {
                sb.append(port);
            } else {
                sb.append("auto-");
                sb.append(getProperty("nameIndex"));
            }
            String address = "";
            if (addressObj instanceof InetAddress) {
                address = ((InetAddress) addressObj).getHostAddress();
            } else if (addressObj != null) {
                address = addressObj.toString();
            }
            if (address.length() > 0) {
                sb.append(",address=");
                sb.append(ObjectName.quote(address));
            }
        }
        return sb.toString();
    }


    /**
     * Pause the connector.
     */
    public void pause() {
        try {
            if (protocolHandler != null) {
                protocolHandler.pause();
            }
        } catch (Exception e) {
            log.error(sm.getString("coyoteConnector.protocolHandlerPauseFailed"), e);
        }
    }


    /**
     * Resume the connector.
     */
    public void resume() {
        try {
            if (protocolHandler != null) {
                protocolHandler.resume();
            }
        } catch (Exception e) {
            log.error(sm.getString("coyoteConnector.protocolHandlerResumeFailed"), e);
        }
    }


    @Override
    protected void initInternal() throws LifecycleException {

        super.initInternal();

        /**
         * 协议句柄，final修饰，创建对象时构造传参
         */
        if (protocolHandler == null) {
            throw new LifecycleException(sm.getString("coyoteConnector.protocolHandlerInstantiationFailed"));
        }

        // Initialize adapter
        /**
         * 初始化适配器，用于处理请求
         */
        adapter = new CoyoteAdapter(this);
        protocolHandler.setAdapter(adapter);
        if (service != null) {
            protocolHandler.setUtilityExecutor(service.getServer().getUtilityExecutor());
        }

        // Make sure parseBodyMethodsSet has a default
        /**
         * 设置允许体参数解析的HTTP方法列表。默认为<code>POST<code>。
         * 确保parseBodyMethodsSet有一个默认值
         */
        if (null == parseBodyMethodsSet) {
            setParseBodyMethods(getParseBodyMethods());
        }

        /**
         * SSL相关
         */
        if (AprStatus.isAprAvailable() && AprStatus.getUseOpenSSL() &&
                protocolHandler instanceof AbstractHttp11JsseProtocol) {
            AbstractHttp11JsseProtocol<?> jsseProtocolHandler = (AbstractHttp11JsseProtocol<?>) protocolHandler;
            if (jsseProtocolHandler.isSSLEnabled() && jsseProtocolHandler.getSslImplementationName() == null) {
                // OpenSSL is compatible with the JSSE configuration, so use it if APR is available
                /**
                 * OpenSSL与JSSE配置兼容，所以如果APR可用，请使用它
                 */
                jsseProtocolHandler.setSslImplementationName(OpenSSLImplementation.class.getName());
            }
        }

        try {
            protocolHandler.init();
        } catch (Exception e) {
            throw new LifecycleException(sm.getString("coyoteConnector.protocolHandlerInitializationFailed"), e);
        }
    }


    /**
     * Begin processing requests via this Connector.
     *
     * @exception LifecycleException if a fatal startup error occurs
     */
    @Override
    protected void startInternal() throws LifecycleException {

        // Validate settings before starting
        String id = (protocolHandler != null) ? protocolHandler.getId() : null;
        if (id == null && getPortWithOffset() < 0) {
            throw new LifecycleException(
                    sm.getString("coyoteConnector.invalidPort", Integer.valueOf(getPortWithOffset())));
        }

        setState(LifecycleState.STARTING);

        try {
            protocolHandler.start();
        } catch (Exception e) {
            throw new LifecycleException(sm.getString("coyoteConnector.protocolHandlerStartFailed"), e);
        }
    }


    /**
     * Terminate processing requests via this Connector.
     *
     * @exception LifecycleException if a fatal shutdown error occurs
     */
    @Override
    protected void stopInternal() throws LifecycleException {

        setState(LifecycleState.STOPPING);

        try {
            if (protocolHandler != null) {
                protocolHandler.stop();
            }
        } catch (Exception e) {
            throw new LifecycleException(sm.getString("coyoteConnector.protocolHandlerStopFailed"), e);
        }
    }


    @Override
    protected void destroyInternal() throws LifecycleException {
        try {
            if (protocolHandler != null) {
                protocolHandler.destroy();
            }
        } catch (Exception e) {
            throw new LifecycleException(sm.getString("coyoteConnector.protocolHandlerDestroyFailed"), e);
        }

        if (getService() != null) {
            getService().removeConnector(this);
        }

        super.destroyInternal();
    }


    /**
     * Provide a useful toString() implementation as it may be used when logging Lifecycle errors to identify the
     * component.
     */
    @Override
    public String toString() {
        // Not worth caching this right now
        StringBuilder sb = new StringBuilder("Connector[");
        sb.append(getProtocol());
        sb.append('-');
        String id = (protocolHandler != null) ? protocolHandler.getId() : null;
        if (id != null) {
            sb.append(id);
        } else {
            int port = getPortWithOffset();
            if (port > 0) {
                sb.append(port);
            } else {
                sb.append("auto-");
                sb.append(getProperty("nameIndex"));
            }
        }
        sb.append(']');
        return sb.toString();
    }


    // -------------------- JMX registration --------------------

    @Override
    protected String getDomainInternal() {
        Service s = getService();
        if (s == null) {
            return null;
        } else {
            return service.getDomain();
        }
    }

    @Override
    protected String getObjectNameKeyProperties() {
        return createObjectNameKeyProperties("Connector");
    }

}
