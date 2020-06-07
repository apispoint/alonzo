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
package com.apispoint.service.resource.resources;

import com.apispoint.service.resource.Resource;

public class EnvironmentResource extends Resource {

    public EnvironmentResource()           { this(null); }
    public EnvironmentResource(String uri) { super(uri); }

    public String getProtocol() { return "env"; }

    public byte[] getBytes() {
        return System.getenv(res).getBytes();
    }

}
