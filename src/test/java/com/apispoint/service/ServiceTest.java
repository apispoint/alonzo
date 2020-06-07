/*
 * Copyright APIS Point, LLC or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
 package com.apispoint.service;

import java.nio.ByteBuffer;
import java.util.Map;

public class ServiceTest extends Service {

    public void put(Map<String, String> params, Event evt, ByteBuffer body) {
        evt.status.body = "ServiceTest is A-OK (PUT)";
        evt.status.code = 200;
        evt.end();
    }

    public void get(Map<String, String> params, Event evt) {
        evt.status.body = "ServiceTest is A-OK (GET)";
        evt.status.code = 200;
        evt.end();
    }

}
