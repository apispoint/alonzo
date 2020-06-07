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
package com.apispoint.platform.service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import com.google.gson.Gson;

public class JsonUtils {

    private static Gson gson = new Gson();

    public static String toJson(Object o) {
        return gson.toJson(o);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> fromJson(String json) {
        return (Map<String, Object>) gson.fromJson(json, Map.class);
    }

    public static Object fromJsonAsUntyped(String json) {
        return gson.fromJson(json, Object.class);
    }

    public static <T> T fromJson(String json, Class<T> classT) {
        return gson.fromJson(json, classT);
    }

    public static <T> T fromJson(InputStream in, Class<T> classT) {
        return gson.fromJson(new InputStreamReader(in), classT);
    }

}
