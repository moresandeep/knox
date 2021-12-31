/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.knox.gateway.webshell;

import org.apache.knox.gateway.audit.api.Auditor;
import org.apache.knox.gateway.websockets.WebsocketLogMessages;

import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionInfoFactory {
    public ConnectionInfo create(String username, String gatewayPIDDir, AtomicInteger concurrentWebshells, Auditor auditor, WebsocketLogMessages LOG){
        return new ConnectionInfo(username, gatewayPIDDir, concurrentWebshells, auditor, LOG);
    }
}