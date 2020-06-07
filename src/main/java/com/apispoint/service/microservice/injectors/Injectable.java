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
package com.apispoint.service.microservice.injectors;

import java.util.Map;

public interface Injectable {

    public static boolean getBooleanOrDefault(Object o, boolean v) { return o != null ? (Boolean) o : v; }
    public static int     getIntOrDefault(Object o, int v)         { return o != null ? (Integer) o : v; }
    public static String  getStringOrDefault(Object o, String v)   { return o != null ? (String)  o : v; }

    public static String[] splitScalarAttributeType(String s) {
        return s.split("/");
    }
    public static String[] splitAdditionalIndicesTuple(String s) {
        return s.split(":");
    }
    public static String normalizeScalarAttributeType(String scalar) throws IllegalArgumentException {
        if(
                scalar == null
             || scalar.trim().matches("B|N|S") == false
          )
            throw new IllegalArgumentException();

        return scalar.trim().toUpperCase();
    }

    public boolean init(Map<String, Object> map);

}
