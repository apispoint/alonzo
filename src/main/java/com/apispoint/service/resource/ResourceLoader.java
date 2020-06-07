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
package com.apispoint.service.resource;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.apispoint.service.resource.resources.ByteArrayResource;
import com.apispoint.service.resource.resources.EnvironmentResource;
import com.apispoint.service.resource.resources.FileResource;
import com.apispoint.service.resource.resources.NullResource;

/**
 *
 * URI := <protocol>:<resource>
 *
 */
public final class ResourceLoader {

    private static Map<String, Class<?>> refMap = new HashMap<>();
    static {
        Resource resr = new EnvironmentResource();
        refMap.put(resr.getProtocol(), resr.getClass());
        resr = new FileResource();
        refMap.put(resr.getProtocol(), resr.getClass());
        resr = new ByteArrayResource();
        refMap.put(resr.getProtocol(), resr.getClass());
    }

    private static ResourceLoader rl = new ResourceLoader();

    private ResourceLoader() {}

    public static ResourceLoader getInstance() {
        return rl;
    }

    public Resource getResource(String uri) {
        Resource r = null;
        try {
            Class<?> klass = refMap.get(uri.split(Resource.delimeter)[0]);
            if(klass != null) {
                r = (Resource) klass.getConstructor().newInstance();
                r.setUri(uri);
            }
        } catch (Exception e) { }
        return r != null ? r : new NullResource();
    }

    public Set<String> getProtocols() {
        return refMap.keySet();
    }

}
