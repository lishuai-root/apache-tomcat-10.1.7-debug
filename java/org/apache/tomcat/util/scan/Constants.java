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
package org.apache.tomcat.util.scan;

/**
 * String constants for the scan package.
 * 扫描包的字符串常量。
 */
public final class Constants {

    public static final String Package = "org.apache.tomcat.util.scan";

    /* System properties */
    public static final String SKIP_JARS_PROPERTY =
            "tomcat.util.scan.StandardJarScanFilter.jarsToSkip";
    public static final String SCAN_JARS_PROPERTY =
            "tomcat.util.scan.StandardJarScanFilter.jarsToScan";

    /* Commons strings */
    public static final String JAR_EXT = ".jar";
    public static final String WEB_INF_LIB = "/WEB-INF/lib/";
    public static final String WEB_INF_CLASSES = "/WEB-INF/classes";
}
