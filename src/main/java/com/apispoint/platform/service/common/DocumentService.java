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
package com.apispoint.platform.service.common;

import java.nio.ByteBuffer;
import java.util.Map;

import com.apispoint.platform.service.JsonUtils;
import com.apispoint.service.microservice.MicroService;

public class DocumentService extends MicroService {

    protected static final String ERR_EMPTY_DOCUMENT = "{}";

    private Map<String, Object> getUser(String uuid) {
        Map<String, Object> user = null;

        try {
            user = JsonUtils.fromJson(collection.getCollectionDocument(uuid));
        } catch(Exception e) { e.printStackTrace(); }

        return user;
    }

    public void get(Map<String, String> params, Event evt) {
        Map<String, Object> user = getUser(params.get("_sub"));

        try {
            evt.status.body = user != null ? JsonUtils.toJson(user) : ERR_EMPTY_DOCUMENT;
            evt.status.code = 200;
        } catch(Exception e) { e.printStackTrace(); }

        evt.end();
    }

    public void put(Map<String, String> params, Event evt, ByteBuffer body) {
        try {
            Map<String, Object> doc = JsonUtils.fromJson(new String(body.array()));
            doc.put(collection.index, params.get("_sub"));
            collection.putCollectionDocument(JsonUtils.toJson(doc));
            evt.status.code = 200;
        } catch(Exception e) { e.printStackTrace(); }
        evt.end();
    }

}
