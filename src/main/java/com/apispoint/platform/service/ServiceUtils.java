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

import java.util.UUID;

public final class ServiceUtils {

    private ServiceUtils() {}

    /**
     * UUID string converted to UPPER CASE characters
     * @return UUID
     */
    public static String getUUID() {
        return getUuid().toUpperCase();
    }

    /**
     * UUID string converted to lower case characters
     * @return uuid
     */
    public static String getUuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * Generate a normalized uuid
     *
     * @return new unique normalized uuid
     */
    public static String generateCustomerID() {
        return normalizeCustomerID(getUuid());
    }

    /**
     * Creates a normalized uuid
     *
     * @param id
     * @return normalized uuid
     */
    public static String normalizeCustomerID(String id) {
        return normalizeID(id);
    }

    /**
     * Creates a normalized username
     *
     * @param id
     * @return normalized username
     */
    public static String normalizeUser(String id) {
        return normalizeID(id);
    }

    public static String normalizeID(String id) {
        return (id == null || id.trim().length() < 1) ? null : id.trim().toUpperCase();
    }

}
