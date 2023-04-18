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


/**
 * Interface defining a listener for significant Container generated events.
 * 接口，为重要容器生成的事件定义侦听器。
 *
 * Note that "container start" and "container stop" events are normally
 * LifecycleEvents, not ContainerEvents.
 * 注意"container start"和"container stop"事件通常是LifecycleEvents，而不是ContainerEvents。
 *
 * @author Craig R. McClanahan
 */
public interface ContainerListener {


    /**
     * Acknowledge the occurrence of the specified event.
     * 确认指定事件的发生。
     *
     * @param event ContainerEvent that has occurred
     */
    void containerEvent(ContainerEvent event);


}
