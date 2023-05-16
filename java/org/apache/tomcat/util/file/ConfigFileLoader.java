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
 *
 */
package org.apache.tomcat.util.file;

import java.io.InputStream;

/**
 * This class is used to obtain {@link InputStream}s for configuration files
 * from a given location String. This allows greater flexibility than these
 * files having to be loaded directly from a file system.
 * 这个类用于从给定的位置字符串中获取配置文件的{@link InputStream}。这比必须直接从文件系统加载这些文件提供了更大的灵活性。
 */
public class ConfigFileLoader {

    private static ConfigurationSource source;

    /**
     * Get the configured configuration source. If none has been configured,
     * a default source based on the calling directory will be used.
     * 获取已配置的配置源。如果没有配置，则使用基于调用目录的默认源。
     *
     * @return the configuration source in use
     */
    public static final ConfigurationSource getSource() {
        if (ConfigFileLoader.source == null) {
            return ConfigurationSource.DEFAULT;
        }
        return source;
    }

    /**
     * Set the configuration source used by Tomcat to locate various
     * configuration resources.
     * 设置Tomcat用来定位各种配置资源的配置源。
     * @param source The source
     */
    public static final void setSource(ConfigurationSource source) {
        if (ConfigFileLoader.source == null) {
            ConfigFileLoader.source = source;
        }
    }

    private ConfigFileLoader() {
        // Hide the constructor
    }


}
