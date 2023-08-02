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
package org.apache.tomcat.util.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;

import javax.management.ObjectName;

/**
 * Properties that can be set in the &lt;Connector&gt; element
 * in server.xml. All properties are prefixed with &quot;socket.&quot;
 * and are currently only working for the Nio connector
 * 可以在Connector&gt;中设置的属性元素。所有属性的前缀都是&quot;socket.&quot;目前只支持Nio连接器
 */
public class SocketProperties {

    /**
     * Enable/disable socket processor cache, this bounded cache stores
     * SocketProcessor objects to reduce GC
     * 启用套接字处理器缓存，此有界缓存存储SocketProcessor对象以减少GC
     *
     * Default is 500
     * -1 is unlimited
     * 0 is disabled
     */
    protected int processorCache = 0;

    /**
     * Enable/disable poller event cache, this bounded cache stores
     * PollerEvent objects to reduce GC for the poller
     * 启用轮询器事件缓存，此有界缓存存储PollerEvent对象以减少轮询器的GC
     *
     * Default is 500
     * -1 is unlimited
     * 0 is disabled
     * &gt;0 the max number of objects to keep in cache.
     */
    protected int eventCache = 0;

    /**
     * Enable/disable direct buffers for the network buffers
     * Default value is disabled
     * Enable/disable网络缓冲区的直接缓冲区默认值为禁用
     */
    protected boolean directBuffer = false;

    /**
     * Enable/disable direct buffers for the network buffers for SSL
     * Default value is disabled
     * Enable/disable用于SSL的网络缓冲区的直接缓冲区默认值为禁用
     */
    protected boolean directSslBuffer = false;

    /**
     * Socket receive buffer size in bytes (SO_RCVBUF).
     * JVM default used if not set.
     * 套接字接收缓冲区大小(SO_RCVBUF)。如果未设置，则使用JVM默认值。
     */
    protected Integer rxBufSize = null;

    /**
     * Socket send buffer size in bytes (SO_SNDBUF).
     * JVM default used if not set.
     * 套接字发送缓冲区大小(SO_SNDBUF)。如果未设置，则使用JVM默认值。
     */
    protected Integer txBufSize = null;

    /**
     * The application read buffer size in bytes.
     * Default value is rxBufSize
     * 应用程序读取缓冲区大小(以字节为单位)。默认值为rxBufSize
     */
    protected int appReadBufSize = 8192;

    /**
     * The application write buffer size in bytes
     * Default value is txBufSize
     * 应用程序写缓冲区大小(以字节为单位)默认值为txBufSize
     */
    protected int appWriteBufSize = 8192;

    /**
     * NioChannel pool size for the endpoint,
     * this value is how many channels
     * NioChannel池大小为端点，此值表示有多少个通道
     *
     * -1 means unlimited cached, 0 means no cache,
     * -2 means bufferPoolSize will be used
     * Default value is -2
     */
    protected int bufferPool = -2;

    /**
     * Buffer pool size in bytes to be cached
     * -1 means unlimited, 0 means no cache
     * Default value is based on the max memory reported by the JVM,
     * if less than 1GB, then 0, else the value divided by 32. This value
     * will then be used to compute bufferPool if its value is -2
     * 要缓存的缓冲池大小(以字节为单位)-1表示无限，0表示没有缓存默认值基于JVM报告的最大内存，
     * 如果小于1GB，则为0，否则将该值除以32。如果该值为-2，则该值将用于计算bufferPool
     *
     */
    protected int bufferPoolSize = -2;

    /**
     * TCP_NO_DELAY option. JVM default used if not set.
     * TCP_NO_DELAY选项。如果未设置，则使用JVM默认值。
     *
     * 禁止小数据报发送延迟(Nagle算法)
     */
    protected Boolean tcpNoDelay = Boolean.TRUE;

    /**
     * SO_KEEPALIVE option. JVM default used if not set.
     * SO_KEEPALIVE选项。如果未设置，则使用JVM默认值。
     */
    protected Boolean soKeepAlive = null;

    /**
     * OOBINLINE option. JVM default used if not set.
     * OOBINLINE选项。如果未设置，则使用JVM默认值。
     */
    protected Boolean ooBInline = null;

    /**
     * SO_REUSEADDR option. JVM default used if not set.
     * SO_REUSEADDR选项。如果未设置，则使用JVM默认值。
     */
    protected Boolean soReuseAddress = null;

    /**
     * SO_LINGER option, paired with the <code>soLingerTime</code> value.
     * JVM defaults used unless both attributes are set.
     * SO_LINGER选项，与<code>soLingerTime<code>值配对。使用JVM默认值，除非设置了这两个属性。

     * 该属性和{@link #soLingerTime}同时使用，用于控制在关闭Socket时的行为，调用{@link Socket#close()}时，
     * 如果{@link #soLingerOn}为true，那个就会等待最多{@link #soLingerTime}秒或者发送完成缓冲区的数据后关闭连接(
     *      如果{@link #soLingerTime}为0，则会立即关闭连接，未发送完成的数据会被丢弃)，
     * 如果{@link #soLingerOn}为false，在调用{@link Socket#close()}后，该方法会立刻返回，但是连接不会马上关闭，而是
     * 等到缓冲区的数据都发送完成后才会管理连接
     *
     */
    protected Boolean soLingerOn = null;

    /**
     * SO_LINGER option, paired with the <code>soLingerOn</code> value.
     * JVM defaults used unless both attributes are set.
     * SO_LINGER选项，与<code>soLingerOn<code>值配对。使用JVM默认值，除非设置了这两个属性。
     */
    protected Integer soLingerTime = null;

    /**
     * SO_TIMEOUT option. default is 20000.
     */
    protected Integer soTimeout = Integer.valueOf(20000);

    /**
     * Performance preferences according to
     * http://docs.oracle.com/javase/1.5.0/docs/api/java/net/Socket.html#setPerformancePreferences(int,%20int,%20int)
     * All three performance attributes must be set or the JVM defaults will be
     * used.
     * 根据http://docs.oracle.com/javase/1.5.0/docs/api/java/net/Socket.html#setPerformancePreferences(int,%20int,%20int)
     * 设置所有三个性能属性，否则将使用JVM默认值。
     */
    protected Integer performanceConnectionTime = null;

    /**
     * Performance preferences according to
     * http://docs.oracle.com/javase/1.5.0/docs/api/java/net/Socket.html#setPerformancePreferences(int,%20int,%20int)
     * All three performance attributes must be set or the JVM defaults will be
     * used.
     */
    protected Integer performanceLatency = null;

    /**
     * Performance preferences according to
     * http://docs.oracle.com/javase/1.5.0/docs/api/java/net/Socket.html#setPerformancePreferences(int,%20int,%20int)
     * All three performance attributes must be set or the JVM defaults will be
     * used.
     */
    protected Integer performanceBandwidth = null;

    /**
     * The minimum frequency of the timeout interval to avoid excess load from
     * the poller during high traffic
     */
    protected long timeoutInterval = 1000;

    /**
     * Timeout in milliseconds for an unlock to take place.
     */
    protected int unlockTimeout = 250;

    private ObjectName oname = null;


    public void setProperties(Socket socket) throws SocketException{
        if (rxBufSize != null) {
            socket.setReceiveBufferSize(rxBufSize.intValue());
        }
        if (txBufSize != null) {
            socket.setSendBufferSize(txBufSize.intValue());
        }
        if (ooBInline !=null) {
            socket.setOOBInline(ooBInline.booleanValue());
        }
        if (soKeepAlive != null) {
            socket.setKeepAlive(soKeepAlive.booleanValue());
        }
        if (performanceConnectionTime != null && performanceLatency != null &&
                performanceBandwidth != null) {
            socket.setPerformancePreferences(
                    performanceConnectionTime.intValue(),
                    performanceLatency.intValue(),
                    performanceBandwidth.intValue());
        }
        if (soReuseAddress != null) {
            socket.setReuseAddress(soReuseAddress.booleanValue());
        }
        if (soLingerOn != null && soLingerTime != null) {
            socket.setSoLinger(soLingerOn.booleanValue(),
                    soLingerTime.intValue());
        }
        if (soTimeout != null && soTimeout.intValue() >= 0) {
            socket.setSoTimeout(soTimeout.intValue());
        }
        if (tcpNoDelay != null) {
            try {
                socket.setTcpNoDelay(tcpNoDelay.booleanValue());
            } catch (SocketException e) {
                // Some socket types may not support this option which is set by default
            }
        }
    }

    /**
     * 设置socket属性
     * 1. 设置自定义socket缓冲区大小 - {@link ServerSocket#setReceiveBufferSize(int)}
     * 2. 设置性能首选项，通过数值的相对大小取决倾向 - {@link ServerSocket#setPerformancePreferences(int, int, int)}
     * 3. 设置是否重用套接字端口 - {@link ServerSocket#setReuseAddress(boolean)}
     * 4. 设置{@link ServerSocket#accept()} 超时时常 - {@link ServerSocket#setSoTimeout(int)}
     *
     * @param socket
     * @throws SocketException
     */
    public void setProperties(ServerSocket socket) throws SocketException{
        /**
         * 设置自定义缓冲区大小
         */
        if (rxBufSize != null) {
            socket.setReceiveBufferSize(rxBufSize.intValue());
        }
        /**
         * 设置性能选项
         * 性能首选项由三个整数描述，它们的值表示短连接时间、低延迟和高带宽的相对重要性。整数的绝对值是不相关的;
         * 为了选择协议，只需比较这些值，值越大表示偏好越强。
         */
        if (performanceConnectionTime != null && performanceLatency != null &&
                performanceBandwidth != null) {
            socket.setPerformancePreferences(
                    performanceConnectionTime.intValue(),
                    performanceLatency.intValue(),
                    performanceBandwidth.intValue());
        }
        /**
         * 设置是否重用套接字
         * 如果一个监听端口的程序关闭后，os不会立刻关闭端口，而是等待一会，避免后续使用该端口的程序接收到脏数据
         * {@link ServerSocket#setReuseAddress(boolean)}设置是否重用端口，表示上一个程序结束后端口未关闭时是否可以重复使用该端口
         *
         * eg:
         *  现有ServerSocket : A, B和Socket: C
         *  1.先启动A，后启动C，C不停的发送数据，此时关闭A，C会收到链接断开的异常
         *  2.先启动A，后启动C，C不停的发送数据，此时关闭A并快速启动B，此时B会重用A使用的端口，C不会收到链接断开的异常，但是B不是处理C发送的数据
         *  {@link SocketTest}
         */
        if (soReuseAddress != null) {
            socket.setReuseAddress(soReuseAddress.booleanValue());
        }
        /**
         * 设置{@link ServerSocket#accept()} 方法的超时时间
         */
        if (soTimeout != null && soTimeout.intValue() >= 0) {
            socket.setSoTimeout(soTimeout.intValue());
        }
    }

    public void setProperties(AsynchronousSocketChannel socket) throws IOException {
        if (rxBufSize != null) {
            socket.setOption(StandardSocketOptions.SO_RCVBUF, rxBufSize);
        }
        if (txBufSize != null) {
            socket.setOption(StandardSocketOptions.SO_SNDBUF, txBufSize);
        }
        if (soKeepAlive != null) {
            socket.setOption(StandardSocketOptions.SO_KEEPALIVE, soKeepAlive);
        }
        if (soReuseAddress != null) {
            socket.setOption(StandardSocketOptions.SO_REUSEADDR, soReuseAddress);
        }
        if (soLingerOn != null && soLingerOn.booleanValue() && soLingerTime != null) {
            socket.setOption(StandardSocketOptions.SO_LINGER, soLingerTime);
        }
        if (tcpNoDelay != null) {
            socket.setOption(StandardSocketOptions.TCP_NODELAY, tcpNoDelay);
        }
    }

    public void setProperties(AsynchronousServerSocketChannel socket) throws IOException {
        if (rxBufSize != null) {
            socket.setOption(StandardSocketOptions.SO_RCVBUF, rxBufSize);
        }
        if (soReuseAddress != null) {
            socket.setOption(StandardSocketOptions.SO_REUSEADDR, soReuseAddress);
        }
    }

    public boolean getDirectBuffer() {
        return directBuffer;
    }

    public boolean getDirectSslBuffer() {
        return directSslBuffer;
    }

    public boolean getOoBInline() {
        return ooBInline.booleanValue();
    }

    public int getPerformanceBandwidth() {
        return performanceBandwidth.intValue();
    }

    public int getPerformanceConnectionTime() {
        return performanceConnectionTime.intValue();
    }

    public int getPerformanceLatency() {
        return performanceLatency.intValue();
    }

    public int getRxBufSize() {
        return rxBufSize.intValue();
    }

    public boolean getSoKeepAlive() {
        return soKeepAlive.booleanValue();
    }

    public boolean getSoLingerOn() {
        return soLingerOn.booleanValue();
    }

    public int getSoLingerTime() {
        return soLingerTime.intValue();
    }

    public boolean getSoReuseAddress() {
        return soReuseAddress.booleanValue();
    }

    public int getSoTimeout() {
        return soTimeout.intValue();
    }

    public boolean getTcpNoDelay() {
        return tcpNoDelay.booleanValue();
    }

    public int getTxBufSize() {
        return txBufSize.intValue();
    }

    public int getBufferPool() {
        return bufferPool;
    }

    public int getBufferPoolSize() {
        return bufferPoolSize;
    }

    public int getEventCache() {
        return eventCache;
    }

    public int getAppReadBufSize() {
        return appReadBufSize;
    }

    public int getAppWriteBufSize() {
        return appWriteBufSize;
    }

    public int getProcessorCache() {
        return processorCache;
    }

    public long getTimeoutInterval() {
        return timeoutInterval;
    }

    public int getDirectBufferPool() {
        return bufferPool;
    }

    public void setPerformanceConnectionTime(int performanceConnectionTime) {
        this.performanceConnectionTime =
            Integer.valueOf(performanceConnectionTime);
    }

    public void setTxBufSize(int txBufSize) {
        this.txBufSize = Integer.valueOf(txBufSize);
    }

    public void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = Boolean.valueOf(tcpNoDelay);
    }

    public void setSoTimeout(int soTimeout) {
        this.soTimeout = Integer.valueOf(soTimeout);
    }

    public void setSoReuseAddress(boolean soReuseAddress) {
        this.soReuseAddress = Boolean.valueOf(soReuseAddress);
    }

    public void setSoLingerTime(int soLingerTime) {
        this.soLingerTime = Integer.valueOf(soLingerTime);
    }

    public void setSoKeepAlive(boolean soKeepAlive) {
        this.soKeepAlive = Boolean.valueOf(soKeepAlive);
    }

    public void setRxBufSize(int rxBufSize) {
        this.rxBufSize = Integer.valueOf(rxBufSize);
    }

    public void setPerformanceLatency(int performanceLatency) {
        this.performanceLatency = Integer.valueOf(performanceLatency);
    }

    public void setPerformanceBandwidth(int performanceBandwidth) {
        this.performanceBandwidth = Integer.valueOf(performanceBandwidth);
    }

    public void setOoBInline(boolean ooBInline) {
        this.ooBInline = Boolean.valueOf(ooBInline);
    }

    public void setDirectBuffer(boolean directBuffer) {
        this.directBuffer = directBuffer;
    }

    public void setDirectSslBuffer(boolean directSslBuffer) {
        this.directSslBuffer = directSslBuffer;
    }

    public void setSoLingerOn(boolean soLingerOn) {
        this.soLingerOn = Boolean.valueOf(soLingerOn);
    }

    public void setBufferPool(int bufferPool) {
        this.bufferPool = bufferPool;
    }

    public void setBufferPoolSize(int bufferPoolSize) {
        this.bufferPoolSize = bufferPoolSize;
    }

    public void setEventCache(int eventCache) {
        this.eventCache = eventCache;
    }

    public void setAppReadBufSize(int appReadBufSize) {
        this.appReadBufSize = appReadBufSize;
    }

    public void setAppWriteBufSize(int appWriteBufSize) {
        this.appWriteBufSize = appWriteBufSize;
    }

    public void setProcessorCache(int processorCache) {
        this.processorCache = processorCache;
    }

    public void setTimeoutInterval(long timeoutInterval) {
        this.timeoutInterval = timeoutInterval;
    }

    public void setDirectBufferPool(int directBufferPool) {
        this.bufferPool = directBufferPool;
    }

    public int getUnlockTimeout() {
        return unlockTimeout;
    }

    public void setUnlockTimeout(int unlockTimeout) {
        this.unlockTimeout = unlockTimeout;
    }

    /**
     * Get the actual buffer pool size to use.
     * @param bufferOverhead When TLS is enabled, additional network buffers
     *   are needed and will be added to the application buffer size
     * @return the actual buffer pool size that will be used
     */
    public int getActualBufferPool(int bufferOverhead) {
        if (bufferPool != -2) {
            return bufferPool;
        } else {
            if (bufferPoolSize == -1) {
                return -1;
            } else if (bufferPoolSize == 0) {
                return 0;
            } else {
                long actualBufferPoolSize = bufferPoolSize;
                long poolSize = 0;
                if (actualBufferPoolSize == -2) {
                    long maxMemory = Runtime.getRuntime().maxMemory();
                    if (maxMemory > Integer.MAX_VALUE) {
                        actualBufferPoolSize = maxMemory / 32;
                    } else {
                        return 0;
                    }
                }
                int bufSize = appReadBufSize + appWriteBufSize + bufferOverhead;
                if (bufSize == 0) {
                    return 0;
                }
                poolSize = actualBufferPoolSize / (bufSize);
                if (poolSize > Integer.MAX_VALUE) {
                    return Integer.MAX_VALUE;
                } else {
                    return (int) poolSize;
                }
            }
        }
    }

    void setObjectName(ObjectName oname) {
        this.oname = oname;
    }

    ObjectName getObjectName() {
        return oname;
    }
}
