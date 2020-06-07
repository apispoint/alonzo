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

import java.nio.ByteBuffer;

public abstract class KeyManagement implements Injectable {

    public static class Envelope {
        public transient ByteBuffer plaintextKey;
        public           ByteBuffer encryptedKey;
        public           String     hmac; // Key integrity hash
    }

    public abstract Envelope getEnvelope();
    public abstract Envelope getEnvelopePlainText(Envelope env);

    public abstract ByteBuffer encryptData(ByteBuffer data);
    public abstract ByteBuffer decryptData(ByteBuffer data);

}
