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

public abstract class Resource {

    public static String delimeter = ":";

    protected String uri = null;
    protected String res = null;

    public Resource() {}
    public Resource(String uri) { setUri(uri); }

    public boolean setUri(String uri) {
        boolean ret = false;
        if(uri != null && uri.startsWith(getProtocol())) {
            this.uri = uri;
            this.res = this.uri.split(delimeter)[1];
            ret = true;
        }
        return ret;
    }

    public String getUri()      { return uri; }
    public String getResource() { return res; }

    public String toString()    {
        byte[] bytes = getBytes();
        return bytes != null ? new String(bytes) : null;
    }

    public abstract String getProtocol();
    public abstract byte[] getBytes();

}
