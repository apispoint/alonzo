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

import java.util.Collection;
import java.util.Map;

public abstract class Database implements Injectable {

    public enum TABLE_ACTION {CREATE, WAIT}

    public String index = null;
    public String range = null;

    //
    // Create / Update
    //
    public abstract void putCollectionDocument(String json);
    public abstract void updateCollectionDocument(String keyValue, Object rangeValue, String expression, String condition, Map<String, String> names, Map<String, Object> values);

    //
    // Read by Query
    //
    public abstract Collection<String> queryCollectionDocument(String value);
    public abstract Collection<String> queryCollectionDocument(String index, String key, String value);
    public abstract Collection<String> queryCollectionDocument(String expression, Map<String, String> names, Map<String, Object> values);
    public abstract Collection<String> queryCollectionDocument(String index, String expression, String projection, Map<String, String> names, Map<String, Object> values, int workers);
    public abstract Collection<String> queryCollectionDocument(String index, String keyexpression, String expression, String projection, Map<String, String> names, Map<String, Object> values);

    //
    // Read by specific ID
    //
    public abstract String getCollectionDocument(String keyValue);
    public abstract String getCollectionDocument(String keyValue, Object rangeValue);
    public abstract String getCollectionDocument(String keyValue, Object rangeValue, String projection, Map<String, String> names);

    //
    // Delete by Specific ID
    //
    public abstract void deleteCollectionDocument(String keyValue);
    public abstract void deleteCollectionDocument(String keyValue, Object rangeValue);

    //
    // Count by Query
    //
    public abstract int countCollectionDocument(String expression, Map<String, String> names, Map<String, Object> values);
    public abstract int countCollectionDocument(String index, String expression, Map<String, String> names, Map<String, Object> values, int workers);

}
