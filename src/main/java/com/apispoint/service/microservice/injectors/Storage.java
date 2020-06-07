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

import java.io.InputStream;

public abstract class Storage implements Injectable {

    public String bucket;

    public abstract boolean isObjectStorageKey(String key);
    public abstract InputStream getStorageObject(String key);

    public abstract void copyStorageObject(String src_bucket, String src_key, String dst_bucket, String dst_key);

    public abstract void deleteStorageObject(String key);

    public abstract String putObjectStoreDocument(String json);
    public abstract String putObjectStoreDocument(String json, String key);

}
